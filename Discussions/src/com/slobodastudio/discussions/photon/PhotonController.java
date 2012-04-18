package com.slobodastudio.discussions.photon;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.photon.constants.ActorPropertiesKey;
import com.slobodastudio.discussions.photon.constants.DeviceType;
import com.slobodastudio.discussions.photon.constants.DiscussionEventCode;
import com.slobodastudio.discussions.photon.constants.DiscussionOperationCode;
import com.slobodastudio.discussions.photon.constants.DiscussionParameterKey;
import com.slobodastudio.discussions.photon.constants.LiteLobbyOpKey;
import com.slobodastudio.discussions.photon.constants.LiteOpParameterKey;
import com.slobodastudio.discussions.photon.constants.LiteOpPropertyType;
import com.slobodastudio.discussions.photon.constants.PhotonConstants;
import com.slobodastudio.discussions.utils.MyLog;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import de.exitgames.client.photon.EventData;
import de.exitgames.client.photon.IPhotonPeerListener;
import de.exitgames.client.photon.LiteEventKey;
import de.exitgames.client.photon.LiteOpCode;
import de.exitgames.client.photon.LiteOpKey;
import de.exitgames.client.photon.LitePeer;
import de.exitgames.client.photon.OperationResponse;
import de.exitgames.client.photon.StatusCode;
import de.exitgames.client.photon.TypedHashMap;
import de.exitgames.client.photon.enums.DebugLevel;
import de.exitgames.client.photon.enums.PeerStateValue;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

public class PhotonController implements IPhotonPeerListener {

	static final String TAG = PhotonController.class.getSimpleName();
	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final int INVALID_POINT_ID = -1;
	private static final int INVALID_TOPIC_ID = -1;
	LitePeer peer;
	Timer timer;
	private final PhotonServiceCallbackHandler callbackHandler = new PhotonServiceCallbackHandler();
	private String gameLobbyName;
	private DiscussionUser localUser;
	private final SyncResultReceiver mSyncResultReceiver = new SyncResultReceiver(new Handler());
	private final Hashtable<Integer, DiscussionUser> onlineUsers = new Hashtable<Integer, DiscussionUser>();

	private static Integer[] toIntArray(final List<Integer> integerList) {

		Integer[] intArray = new Integer[integerList.size()];
		for (int i = 0; i < integerList.size(); i++) {
			intArray[i] = integerList.get(i);
		}
		return intArray;
	}

	public void connect(final int discussionId, final String dbSrvAddr, final String UsrName,
			final int usrDbId) {

		gameLobbyName = dbSrvAddr + "discussion#" + discussionId;
		localUser = new DiscussionUser();
		localUser.setUserName(UsrName);
		localUser.setUserId(usrDbId);
		peer = new LitePeer(this, PhotonConstants.USE_TCP);
		peer.setSentCountAllowance(5);
		if (!peer.connect(PhotonConstants.SERVER_URL, PhotonConstants.APPLICATION_NAME)) {
			throw new IllegalArgumentException("Can't connect to the server. Server address: "
					+ PhotonConstants.SERVER_URL + " ; Application name: " + PhotonConstants.APPLICATION_NAME);
		}
		startPeerUpdateTimer();
	}

	@Override
	public void debugReturn(final DebugLevel level, final String message) {

		if (DebugLevel.ERROR.equals(level)) {
			Log.e(TAG, message);
			callbackHandler.onErrorOccured(message);
		} else if (DebugLevel.WARNING.equals(level)) {
			Log.w(TAG, message);
		} else {
			if (DEBUG) {
				Log.v(TAG, level.name() + " : " + message);
			}
		}
	}

	public void disconnect() {

		if (isConnected()) {
			if (timer == null) {
				throw new IllegalStateException("Timer was null at the disconnect point");
			}
			if (peer == null) {
				throw new IllegalStateException("Peer was null at the disconnect point");
			}
			peer.opCustom(DiscussionOperationCode.NOTIFY_LEAVE_USER, null, true);
			// run this method off the ui thread
			peer.opLeave();
		}
	}

	public PhotonServiceCallbackHandler getCallbackHandler() {

		return callbackHandler;
	}

	public ResultReceiver getResultReceiver() {

		return mSyncResultReceiver;
	}

	public boolean isConnected() {

		return (peer != null) && (peer.getPeerState() == PeerStateValue.Connected);
	}

	@Override
	public void onEvent(final EventData event) {

		// most events will contain the actorNumber of the player who sent the event, so check if the event
		// origin is known
		if (DEBUG) {
			Log.d(TAG, "[onEvent] " + DiscussionEventCode.asString(event.Code) + ", parameters: "
					+ event.Parameters.values().toString());
		}
		switch (event.Code.byteValue()) {
			case DiscussionEventCode.JOIN:
				// Event is defined by Lite. A peer entered the room. It could be this peer!
				// This event provides the current list of actors and a actorNumber of the player who is new.
				// get the list of current players and check it against local list - create any that's not yet
				// there
				Integer[] actorsInGame = (Integer[]) event.Parameters.get(LiteEventKey.ActorList.value());
				ArrayList<Integer> unknownActors = new ArrayList<Integer>();
				for (Integer i : actorsInGame) {
					if (i.intValue() != localUser.getActorNumber()) {
						if (!onlineUsers.containsKey(i)) {
							unknownActors.add(i);
						}
					}
				}
				if (unknownActors.size() > 0) {
					opRequestActorsInfo(unknownActors);
				}
				break;
			case DiscussionEventCode.LEAVE:
				// Event is defined by Lite. Someone left the room.
				Integer leftActorNumber = (Integer) event.Parameters.get(LiteEventKey.ActorNr.value());
				callbackHandler.onEventLeave(onlineUsers.get(leftActorNumber));
				onlineUsers.remove(leftActorNumber);
				logUsersOnline();
				break;
			case DiscussionEventCode.STRUCTURE_CHANGED: {
				int personId = (Integer) event.Parameters.get(DiscussionParameterKey.USER_ID);
				if (personId != localUser.getUserId()) {
					int changedTopicId = (Integer) event.Parameters
							.get(DiscussionParameterKey.CHANGED_TOPIC_ID);
					if (changedTopicId == INVALID_TOPIC_ID) {
						// special code. new point was added or deleted
						callbackHandler.onRefreshCurrentTopic();
						break;
					}
					callbackHandler.onStructureChanged(changedTopicId);
				}
				break;
			}
			case DiscussionEventCode.ARG_POINT_CHANGED: {
				// check if actor num is here
				if (event.Parameters.containsKey(DiscussionParameterKey.STRUCT_CHANGE_ACTOR_NR)) {
					int actorId = (Integer) event.Parameters
							.get(DiscussionParameterKey.STRUCT_CHANGE_ACTOR_NR);
					if (actorId == localUser.getActorNumber()) {
						break;
					}
				}
				// update
				int pointId = (Integer) event.Parameters.get(DiscussionParameterKey.ARG_POINT_ID);
				if (pointId == INVALID_POINT_ID) {
					// special code. new point was added or deleted
					callbackHandler.onRefreshCurrentTopic();
					break;
				}
				callbackHandler.onArgPointChanged(pointId);
				break;
			}
			case DiscussionEventCode.INSTANT_USER_PLUS_MINUS:
			case DiscussionEventCode.BADGE_GEOMETRY_CHANGED:
			case DiscussionEventCode.BADGE_EXPANSION_CHANGED:
			case DiscussionEventCode.USER_CURSOR_CHANGED:
			case DiscussionEventCode.ANNOTATION_CHANGED:
			case DiscussionEventCode.USER_ACC_PLUS_MINUS:
			case DiscussionEventCode.STATS_EVENT:
				break;
			// throw new UnsupportedOperationException("Event " + DiscussionEventCode.asString(event.Code)
			// + " not implemented yet");
			default:
				throw new IllegalArgumentException("Unknown event code: " + event.Code);
		}
	}

	@Override
	public void onOperationResponse(final OperationResponse operationResponse) {

		byte opCode = operationResponse.OperationCode;
		short returnCode = operationResponse.ReturnCode;
		if (DEBUG) {
			Log.d(TAG, "[onOperationResponse] " + DiscussionOperationCode.asString(opCode)
					+ ", return code: " + returnCode + ", parameters: "
					+ operationResponse.Parameters.values().toString());
		}
		if (returnCode != (short) 0) {
			debugReturn(DebugLevel.INFO, "[onOperationResponse] " + opCode + "/" + returnCode
					+ ", error message: " + operationResponse.DebugMessage);
			return;
		}
		switch (opCode) {
			case DiscussionOperationCode.TEST:
				// ignore it, just for tests
				break;
			case DiscussionOperationCode.JOIN:
				if (operationResponse.Parameters.containsKey(LiteOpKey.ActorNr.value())) {
					localUser.setActorNumber(((Integer) operationResponse.Parameters.get(LiteOpKey.ActorNr
							.value())).intValue());
				} else {
					throw new IllegalStateException(
							"Expected an actor number here to update local user number");
				}
				if (operationResponse.Parameters.containsKey(LiteOpKey.ActorProperties.value())) {
					HashMap<Integer, Object> resp = (HashMap<Integer, Object>) operationResponse.Parameters
							.get(LiteOpKey.ActorProperties.value());
					updateOnlineUsers(resp);
				} else {
					// no users online, we are first
					// throw new IllegalStateException(
					// "Expected an actors list with properties here to update online users");
				}
				logUsersOnline();
				callbackHandler.onConnect();
				break;
			case DiscussionOperationCode.LEAVE:
				peer.disconnect();
				onlineUsers.clear();
				localUser = null;
				break;
			case DiscussionOperationCode.GET_PROPERTIES:
				HashMap<Integer, Object> resp = (HashMap<Integer, Object>) operationResponse.Parameters
						.get(LiteOpKey.ActorProperties.value());
				updateOnlineUsers(resp);
				logUsersOnline();
				break;
			case DiscussionOperationCode.NOTIFY_STRUCTURE_CHANGED:
			case DiscussionOperationCode.NOTIFY_ARGPOINT_CHANGED:
			case DiscussionOperationCode.NOTIFY_ANNOTATION_UPDATED:
			case DiscussionOperationCode.NOTIFY_BADGE_EXPANSION_CHANGED:
			case DiscussionOperationCode.NOTIFY_LEAVE_USER:
			case DiscussionOperationCode.NOTIFY_BADGE_GEOMETRY_CHANGED:
			case DiscussionOperationCode.NOTIFY_USER_ACC_PLUS_MINUS:
			case DiscussionOperationCode.NOTIFY_USER_CURSOR_STATE:
			case DiscussionOperationCode.REQUEST_BADGE_GEOMETRY:
			case DiscussionOperationCode.REQUEST_SYNC_POINTS:
			case DiscussionOperationCode.STATS_EVENT:
				break;
			// throw new UnsupportedOperationException("Operation: "
			// + DiscussionOperationCode.asString(opCode) + " not implemented yet");
			default:
				throw new IllegalArgumentException("Unknown operation code: " + opCode);
		}
	}

	@Override
	public void onStatusChanged(final StatusCode statusCode) {

		switch (statusCode) {
			case Connect:
				debugReturn(DebugLevel.INFO, "peerStatusCallback(): " + statusCode.name() + ", peer.state: "
						+ peer.getPeerState());
				opJoinFromLobby();
				break;
			case Disconnect:
				debugReturn(DebugLevel.INFO, "peerStatusCallback(): " + statusCode.name() + ", peer.state: "
						+ peer.getPeerState());
				localUser = null;
				timer.cancel();
				break;
			case DisconnectByServer:
			case DisconnectByServerLogic:
			case DisconnectByServerUserLimit:
			case EncryptionEstablished:
			case EncryptionFailedToEstablish:
			case Exception:
			case InternalReceiveException:
			case QueueIncomingReliableWarning:
			case QueueIncomingUnreliableWarning:
			case QueueOutgoingAcksWarning:
			case QueueOutgoingReliableError:
			case QueueOutgoingReliableWarning:
			case QueueOutgoingUnreliableWarning:
			case QueueSentWarning:
			case SendError:
			case TimeoutDisconnect:
				debugReturn(DebugLevel.ERROR, "peerStatusCallback(): " + statusCode.name() + ", peer.state: "
						+ peer.getPeerState());
				break;
			default:
				throw new IllegalArgumentException("Unknown status code: " + statusCode.name());
		}
	}

	public boolean opArgPointChanged(final int changedPointId) {

		if (DEBUG) {
			Log.d(TAG, "[opArgPointChanged] point id: " + changedPointId);
		}
		if (!isConnected()) {
			throw new IllegalStateException(
					"You trying to send notification while not connected to the server");
		}
		if (changedPointId < -1) {
			throw new IllegalArgumentException("Point id can't be below zero");
		}
		TypedHashMap<Byte, Object> structureChangedParameters = new TypedHashMap<Byte, Object>(Byte.class,
				Object.class);
		structureChangedParameters.put(DiscussionParameterKey.ARG_POINT_ID, changedPointId);
		structureChangedParameters.put(DiscussionParameterKey.STRUCT_CHANGE_ACTOR_NR, localUser
				.getActorNumber());
		return peer.opCustom(DiscussionOperationCode.NOTIFY_ARGPOINT_CHANGED, structureChangedParameters,
				true);
	}

	public void opJoinFromLobby() {

		HashMap<Byte, Object> actorProperties = new HashMap<Byte, Object>();
		actorProperties.put(ActorPropertiesKey.NAME, localUser.getUserName());
		actorProperties.put(ActorPropertiesKey.DB_ID, localUser.getUserId());
		actorProperties.put(ActorPropertiesKey.DEVICE_TYPE, DeviceType.ANDROID);
		opJoinFromLobby(gameLobbyName, PhotonConstants.LOBBY, actorProperties, true);
	}

	public boolean opSendNotifyStructureChanged(final int activeTopicId) {

		if (DEBUG) {
			Log.d(TAG, "[opSendNotifyStructureChanged] topic id: " + activeTopicId);
		}
		if (!isConnected()) {
			throw new IllegalStateException(
					"You trying to send notification while not connected to the server");
		}
		if (activeTopicId < 0) {
			throw new IllegalArgumentException("Active topic id can't be below zero");
		}
		TypedHashMap<Byte, Object> structureChangedParameters = new TypedHashMap<Byte, Object>(Byte.class,
				Object.class);
		structureChangedParameters.put(DiscussionParameterKey.CHANGED_TOPIC_ID, activeTopicId);
		structureChangedParameters.put(DiscussionParameterKey.FORCE_SELF_NOTIFICATION, (byte) 1);
		structureChangedParameters.put(DiscussionParameterKey.USER_ID, localUser.getUserId());
		structureChangedParameters.put(DiscussionParameterKey.DEVICE_TYPE, DeviceType.ANDROID);
		return peer.opCustom(DiscussionOperationCode.NOTIFY_STRUCTURE_CHANGED, structureChangedParameters,
				true);
	}

	boolean opSendStatsEvent(final int discussionId, final int userId, final int changedTopicId,
			final int statsEventId) {

		if (!isConnected()) {
			throw new IllegalStateException(
					"Cant perfom operation \"opSendStatsEvent\" in disconnected state");
		}
		if (DEBUG) {
			Log.d(TAG, "[opSendStatsEvent] topic id: " + changedTopicId + ", userId: " + userId
					+ ", discussionId: " + discussionId);
		}
		TypedHashMap<Byte, Object> eventStatsParameters = new TypedHashMap<Byte, Object>(Byte.class,
				Object.class);
		eventStatsParameters.put(DiscussionParameterKey.DISCUSSION_ID, discussionId);
		eventStatsParameters.put(DiscussionParameterKey.USER_ID, userId);
		eventStatsParameters.put(DiscussionParameterKey.CHANGED_TOPIC_ID, changedTopicId);
		eventStatsParameters.put(DiscussionParameterKey.STATS_EVENT, statsEventId);
		eventStatsParameters.put(DiscussionParameterKey.DEVICE_TYPE, DeviceType.ANDROID);
		return peer.opCustom(DiscussionOperationCode.STATS_EVENT, eventStatsParameters, true);
	}

	private void logUsersOnline() {

		if (DEBUG) {
			Log.d(TAG, "Users online: " + (onlineUsers.size() + 1));
			Log.d(TAG, "Local user name: " + localUser.getUserName() + " user id: " + localUser.getUserId()
					+ " actor num: " + localUser.getActorNumber());
			for (DiscussionUser user : onlineUsers.values()) {
				Log.d(TAG, "Online user name: " + user.getUserName() + " user id: " + user.getUserId()
						+ " actor num: " + user.getActorNumber());
			}
		}
	}

	private boolean opJoinFromLobby(final String gameName, final String lobbyName,
			final HashMap<Byte, Object> actorProperties, final boolean broadcastActorProperties) {

		if (!isConnected()) {
			throw new IllegalStateException("Cant perfom operation \"opJoinFromLobby\" in disconnected state");
		}
		if (actorProperties == null) {
			throw new IllegalArgumentException(
					"Actor properties was null and required for operation parameters");
		}
		TypedHashMap<Byte, Object> joinParameters = new TypedHashMap<Byte, Object>(Byte.class, Object.class);
		joinParameters.put(LiteLobbyOpKey.RoomName, gameName);
		joinParameters.put(LiteLobbyOpKey.LobbyName, lobbyName);
		joinParameters.put(LiteOpKey.ActorProperties.value(), actorProperties);
		joinParameters.put(LiteOpKey.Broadcast.value(), broadcastActorProperties);
		return peer.opCustom(LiteOpCode.Join, joinParameters, true);
	}

	private boolean opRequestActorsInfo(final List<Integer> unknownActorsNumbers) {

		if (!isConnected()) {
			throw new IllegalStateException("Cant perfom [opRequestActorsInfo] in disconnected state");
		}
		if ((unknownActorsNumbers == null) || (unknownActorsNumbers.size() <= 0)) {
			throw new IllegalArgumentException("Tried to ger actors info without actors numbers");
		}
		TypedHashMap<Byte, Object> opRequestParameters = new TypedHashMap<Byte, Object>(Byte.class,
				Object.class);
		opRequestParameters.put(LiteOpParameterKey.ACTORS, toIntArray(unknownActorsNumbers));
		opRequestParameters.put(LiteOpParameterKey.PROPERTIES, Byte.valueOf(LiteOpPropertyType.ACTOR));
		return peer.opCustom(LiteOpCode.GetProperties, opRequestParameters, true);
	}

	private void startPeerUpdateTimer() {

		// FIXME: stop this timer
		timer = new Timer("main loop");
		TimerTask timerTask = new TimerTask() {

			long lastDispatchTime = 0xFFFFFFFF;
			long lastSendTime = 0xFFFFFFFF;

			@Override
			public void run() {

				if (peer == null) {
					throw new IllegalStateException("Run timer on null peer");
				}
				// test if it's time to dispatch all incoming commands to the application. Dispatching
				// will empty the queue of incoming messages and will fire the related callbacks.
				if ((System.currentTimeMillis() - lastDispatchTime) > PhotonConstants.DISPATCH_INTERVAL) {
					lastDispatchTime = System.currentTimeMillis();
					// dispatch all incoming commands
					try {
						while (peer.dispatchIncomingCommands()) {
							// wait until false in dispatchIncomingCommands
						}
					} catch (ConcurrentModificationException e) {
						MyLog.e(TAG, "[peerService] cant dispatch incoming command", e);
					} catch (NoSuchElementException e) {
						MyLog.e(TAG, "[peerService] cant dispatch incoming command", e);
					}
				}
				// to spare some overhead, we will send outgoing packets in certain intervals, as defined
				// in the settings menu.
				if ((System.currentTimeMillis() - lastSendTime) > PhotonConstants.SEND_INTERVAL) {
					lastSendTime = System.currentTimeMillis();
					if (peer != null) {
						peer.sendOutgoingCommands();
					}
				}
			}
		};
		timer.schedule(timerTask, 0, 5);
	}

	private void updateOnlineUsers(final HashMap<Integer, Object> actorsProperties) {

		Iterator<Entry<Integer, Object>> it = actorsProperties.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, Object> pairs = it.next();
			DiscussionUser newUser = new DiscussionUser();
			newUser.setActorNumber(pairs.getKey());
			HashMap<Byte, Object> actorProperties = (HashMap<Byte, Object>) pairs.getValue();
			newUser.setUserId((Integer) actorProperties.get(Byte.valueOf((byte) 2)));
			newUser.setUserName((String) actorProperties.get(Byte.valueOf((byte) 1)));
			onlineUsers.put(newUser.getActorNumber(), newUser);
			it.remove(); // avoids a ConcurrentModificationException
			callbackHandler.onEventJoin(newUser);
		}
	}

	public class SyncResultReceiver extends ResultReceiver {

		public static final String EXTRA_DISCUSSION_ID = "intent.extra.key.EXTRA_DISCUSSION_ID";
		public static final String EXTRA_EVENT_TYPE = "intent.extra.key.EXTRA_EVENT_TYPE";
		public static final String EXTRA_POINT_ID = "intent.extra.key.EXTRA_POINT_ID";
		public static final String EXTRA_TOPIC_ID = "intent.extra.key.EXTRA_TOPIC_ID";
		public static final String EXTRA_USER_ID = "intent.extra.key.EXTRA_USER_ID";
		public static final int STATUS_ARG_POINT_CHANGED = 0x3;
		public static final int STATUS_EVENT_CHANGED = 0x4;
		public static final int STATUS_STRUCTURE_CHANGED = 0x2;

		public SyncResultReceiver(final Handler handler) {

			super(handler);
		}

		@Override
		protected void onReceiveResult(final int resultCode, final Bundle resultData) {

			if (DEBUG) {
				Log.d(TAG, "[onReceiveResult] code: " + resultCode + ", data: " + resultData.toString());
			}
			super.onReceiveResult(resultCode, resultData);
			switch (resultCode) {
				case STATUS_ARG_POINT_CHANGED:
					int pointId = resultData.getInt(EXTRA_POINT_ID);
					if (isConnected()) {
						opArgPointChanged(pointId);
					}
					break;
				case STATUS_STRUCTURE_CHANGED:
					int topicId = resultData.getInt(EXTRA_TOPIC_ID);
					if (isConnected()) {
						opSendNotifyStructureChanged(topicId);
					}
					break;
				case STATUS_EVENT_CHANGED:
					int discussionsId = resultData.getInt(EXTRA_DISCUSSION_ID);
					int changedTopicId = resultData.getInt(EXTRA_TOPIC_ID);
					int userId = resultData.getInt(EXTRA_USER_ID);
					int statsEventId = resultData.getInt(EXTRA_EVENT_TYPE);
					if (isConnected()) {
						opSendStatsEvent(discussionsId, userId, changedTopicId, statsEventId);
					}
					break;
				default:
					throw new IllegalArgumentException("Unknown result code: " + resultCode);
			}
		}
	}
}
