package com.slobodastudio.discussions.photon;

import com.slobodastudio.discussions.photon.constants.ActorPropertiesCode;
import com.slobodastudio.discussions.photon.constants.DiscussionEventCode;
import com.slobodastudio.discussions.photon.constants.DiscussionOperationCode;
import com.slobodastudio.discussions.photon.constants.DiscussionParameterKey;
import com.slobodastudio.discussions.photon.constants.LiteLobbyOpKey;
import com.slobodastudio.discussions.photon.constants.LiteOpParameterKey;
import com.slobodastudio.discussions.photon.constants.LiteOpPropertyType;
import com.slobodastudio.discussions.photon.constants.PhotonConstants;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import de.exitgames.client.photon.DebugLevel;
import de.exitgames.client.photon.EventData;
import de.exitgames.client.photon.IPhotonPeerListener;
import de.exitgames.client.photon.LiteEventCode;
import de.exitgames.client.photon.LiteEventKey;
import de.exitgames.client.photon.LiteOpCode;
import de.exitgames.client.photon.LiteOpKey;
import de.exitgames.client.photon.LitePeer;
import de.exitgames.client.photon.OperationResponse;
import de.exitgames.client.photon.PhotonPeer.PeerStateValue;
import de.exitgames.client.photon.StatusCode;
import de.exitgames.client.photon.TypedHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

public class PhotonService extends Service implements IPhotonPeerListener {

	private static final String TAG = "PhotonService";
	private final PhotonServiceCallbackHandler callbackHandler = new PhotonServiceCallbackHandler();
	private String gameLobbyName;
	private DiscussionUser localUser;
	private final IBinder mBinder = new LocalBinder();
	private final Hashtable<Integer, DiscussionUser> onlineUsers = new Hashtable<Integer, DiscussionUser>();
	LitePeer peer;
	Object sequenceNumberingLockObj = new Object();
	/** Handler required to process async events that are interfering with UI (eventAction changes players
	 * array while UI reads it when redrawing. So eventAction should be executed in main loop to avoid
	 * ConcurrentModificationException) */
	final Handler syncHandler = new Handler();
	Timer timer;

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
			Log.v(TAG, level.name() + " : " + message);
		}
	}

	public void disconnect() {

		if (timer == null) {
			throw new IllegalStateException("Timer was null at the disconnect point");
		}
		if (peer == null) {
			throw new IllegalStateException("Peer was null at the disconnect point");
		}
		timer.cancel();
		timer = null;
		peer.opLeave(gameLobbyName);
		peer.disconnect();
		peer = null;
	}

	public PhotonServiceCallbackHandler getCallbackHandler() {

		return callbackHandler;
	}

	@Override
	public IBinder onBind(final Intent arg0) {

		return mBinder;
	}

	@Override
	public void onDestroy() {

		disconnect();
		super.onDestroy();
	}

	@Override
	public void onEvent(final EventData event) {

		// most events will contain the actorNumber of the player who sent the event, so check if the event
		// origin is known
		switch (event.Code.byteValue()) {
			case LiteEventCode.Join:
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
			case LiteEventCode.Leave:
				Log.v(TAG, "onEvent(): LiteEventCode.Leave");
				// Event is defined by Lite. Someone left the room.
				Integer leftActorNumber = (Integer) event.Parameters.get(LiteEventKey.ActorNr.value());
				callbackHandler.onEventLeave(onlineUsers.get(leftActorNumber));
				onlineUsers.remove(leftActorNumber);
				logUsersOnline();
				break;
			case DiscussionEventCode.BADGE_GEOMETRY_CHANGED:
			case DiscussionEventCode.BADGE_EXPANSION_CHANGED:
			case DiscussionEventCode.USER_CURSOR_CHANGED:
			case DiscussionEventCode.STRUCTURE_CHANGED:
			case DiscussionEventCode.ARG_POINT_CHANGED:
				throw new UnsupportedOperationException("Event: " + event.Code + " not implemented yet");
			default:
				throw new IllegalArgumentException("Unknown event code: " + event.Code);
		}
	}

	@Override
	public void onOperationResponse(final OperationResponse operationResponse) {

		byte opCode = operationResponse.OperationCode;
		short returnCode = operationResponse.ReturnCode;
		if ((opCode != LiteOpCode.RaiseEvent) || (returnCode != (short) 0)) {
			debugReturn(DebugLevel.INFO, "OnOperationResponse() " + opCode + "/" + returnCode);
		}
		switch (opCode) {
			case DiscussionOperationCode.Test:
				// ignore it, just for tests
				break;
			case LiteOpCode.Join:
				debugReturn(DebugLevel.INFO, "Join response: "
						+ operationResponse.Parameters.values().toString());
				if (operationResponse.Parameters.containsKey(LiteOpKey.ActorNr.value())) {
					localUser.setActorNumber(((Integer) operationResponse.Parameters.get(LiteOpKey.ActorNr
							.value())).intValue());
				} else {
					throw new IllegalStateException(
							"Expected an actor number here to update local user number");
				}
				if (operationResponse.Parameters.containsKey(LiteOpKey.ActorProperties.value())) {
					HashMap<Byte, Object> resp = (HashMap<Byte, Object>) operationResponse.Parameters
							.get(LiteOpKey.ActorProperties.value());
					updateOnlineUsers(resp);
				} else {
					throw new IllegalStateException(
							"Expected an actors list with properties here to update online users");
				}
				logUsersOnline();
				break;
			case LiteOpCode.Leave:
				debugReturn(DebugLevel.INFO, "Join response: "
						+ operationResponse.Parameters.values().toString());
				onlineUsers.clear();
				localUser = null;
				break;
			case LiteOpCode.GetProperties:
				debugReturn(DebugLevel.INFO, "GetProperties response: "
						+ operationResponse.Parameters.values().toString());
				if (operationResponse.Parameters.containsKey(LiteOpKey.ActorProperties.value())) {
					HashMap<Byte, Object> resp = (HashMap<Byte, Object>) operationResponse.Parameters
							.get(LiteOpKey.ActorProperties.value());
					updateOnlineUsers(resp);
				} else {
					throw new IllegalStateException(
							"Expected an actors list with properties here to update online users");
				}
				logUsersOnline();
				break;
			case DiscussionOperationCode.RequestBadgeGeometry:
				throw new UnsupportedOperationException("Operation: " + opCode + " not implemented yet");
			default:
				throw new IllegalArgumentException("onOperationResponse(): unknown opCode: " + opCode);
		}
	}

	public boolean opSendNotifyStructureChanged(final int activeTopicId) {

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
		structureChangedParameters.put(DiscussionParameterKey.STRUCT_CHANGE_ACTOR_NR, localUser
				.getActorNumber());
		return peer
				.opCustom(DiscussionOperationCode.NotifyStructureChanged, structureChangedParameters, true);
	}

	@Override
	public void peerStatusCallback(final StatusCode statusCode) {

		switch (statusCode) {
			case Connect:
				debugReturn(DebugLevel.INFO, "peerStatusCallback(): " + statusCode.name() + ", peer.state: "
						+ peer.getPeerState());
				callbackHandler.onConnect();
				HashMap<Byte, Object> actorProperties = new HashMap<Byte, Object>();
				actorProperties.put(ActorPropertiesCode.Name, localUser.getUserName());
				actorProperties.put(ActorPropertiesCode.DbId, localUser.getUserId());
				opJoinFromLobby(gameLobbyName, PhotonConstants.LOBBY, actorProperties, true);
				break;
			case Disconnect:
				debugReturn(DebugLevel.INFO, "peerStatusCallback(): " + statusCode.name() + ", peer.state: "
						+ peer.getPeerState());
				localUser = null;
				// TODO: try to reconnect
				break;
			case Exception_Connect:
			case Exception:
			case SendError:
			case TimeoutDisconnect:
				debugReturn(DebugLevel.ERROR, "peerStatusCallback(): " + statusCode.name() + ", peer.state: "
						+ peer.getPeerState());
				break;
			default:
				throw new IllegalArgumentException("Unknown status code: " + statusCode.name());
		}
	}

	private boolean isConnected() {

		if (peer == null) {
			throw new IllegalStateException("Peer was null, while you are checking its connection");
		}
		return peer.getPeerState() == PeerStateValue.Connected.value();
	}

	private void logUsersOnline() {

		Log.v(TAG, "Users online: " + (onlineUsers.size() + 1));
		Log.v(TAG, "Local user name: " + localUser.getUserName() + " user id: " + localUser.getUserId()
				+ " actor num: " + localUser.getActorNumber());
		for (DiscussionUser user : onlineUsers.values()) {
			Log.v(TAG, "Online user name: " + user.getUserName() + " user id: " + user.getUserId()
					+ " actor num: " + user.getActorNumber());
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
			throw new IllegalStateException(
					"Cant perfom operation \"request actor info\" in disconnected state");
		}
		if (unknownActorsNumbers.size() <= 0) {
			throw new IllegalArgumentException("Tried to ger actors info without actors numbers");
		}
		TypedHashMap<Byte, Object> opRequestParameters = new TypedHashMap<Byte, Object>(Byte.class,
				Object.class);
		opRequestParameters.put(LiteOpParameterKey.ACTORS, toIntArray(unknownActorsNumbers));
		opRequestParameters.put(LiteOpParameterKey.PROPERTIES, Byte.valueOf(LiteOpPropertyType.ACTOR));
		return peer.opCustom(LiteOpCode.GetProperties, opRequestParameters, true);
	}

	private void startPeerUpdateTimer() {

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
					while (peer.dispatchIncomingCommands()) {
						// wait until false in dispatchIncomingCommands
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

	private void updateOnlineUsers(final HashMap<Byte, Object> actorsProperties) {

		Iterator<Entry<Byte, Object>> it = actorsProperties.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Byte, Object> pairs = it.next();
			DiscussionUser newUser = new DiscussionUser();
			newUser.setActorNumber(pairs.getKey().intValue());
			HashMap<Byte, Object> actorProperties = (HashMap<Byte, Object>) pairs.getValue();
			newUser.setUserId((Integer) actorProperties.get(Byte.valueOf((byte) 2)));
			newUser.setUserName((String) actorProperties.get(Byte.valueOf((byte) 1)));
			onlineUsers.put(newUser.getActorNumber(), newUser);
			it.remove(); // avoids a ConcurrentModificationException
			callbackHandler.onEventJoin(newUser);
		}
	}

	/** Class for clients to access. Because we know this service always runs in the same process as its
	 * clients, we don't need to deal with IPC. */
	public class LocalBinder extends Binder {

		/** @return instance of PhotonService */
		public PhotonService getService() {

			return PhotonService.this;
		}
	}
}
