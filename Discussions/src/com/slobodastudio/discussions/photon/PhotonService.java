package com.slobodastudio.discussions.photon;

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
import de.exitgames.client.photon.LiteOpCode;
import de.exitgames.client.photon.LiteOpKey;
import de.exitgames.client.photon.LitePeer;
import de.exitgames.client.photon.OperationResponse;
import de.exitgames.client.photon.PhotonPeer.PeerStateValue;
import de.exitgames.client.photon.StatusCode;
import de.exitgames.client.photon.TypedHashMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PhotonService extends Service implements IPhotonPeerListener {

	private static final String LOBBY = "discussion_lobby";
	private static final String TAG = "PhotonService";
	private String dbSrvAddr;
	private int discussionId;
	private DiscussionUser localUser;
	private final List<PhotonServiceCallback> m_callbacks = new LinkedList<PhotonServiceCallback>();
	private final IBinder mBinder = new LocalBinder();
	/** Not sure how to use this one. Defines unique session on server. */
	LitePeer peer;
	Object sequenceNumberingLockObj = new Object();
	/** Handler required to process async events that are interfering with UI (eventAction changes players
	 * array while UI reads it when redrawing. So eventAction should be executed in main loop to avoid
	 * ConcurrentModificationException) */
	final Handler syncHandler = new Handler();
	Timer timer;

	public void addCallbackListener(final PhotonServiceCallback cb) {

		if (cb != null) {
			m_callbacks.add(cb);
		}
	}

	public void connect(final int discussionId, final String dbSrvAddr, final String UsrName,
			final int usrDbId) {

		this.discussionId = discussionId;
		this.dbSrvAddr = dbSrvAddr;
		localUser = DiscussionUser.newInstance(UsrName, usrDbId);
		peer = new LitePeer(this, PhotonConstants.USE_TCP);
		if (!peer.connect(PhotonConstants.SERVER_URL, PhotonConstants.APPLICATION_NAME)) {
			Log.v(TAG, "Not connected");
		}
		timer = new Timer("main loop");
		TimerTask timerTask = new TimerTask() {

			long lastDispatchTime = 0xFFFFFFFF;
			long lastSendTime = 0xFFFFFFFF;

			@Override
			public void run() {

				if (null == peer) {
					cancel();
					timer.cancel();
					return;
				}
				// TODO: here was some command behavior
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

	@Override
	public void debugReturn(final DebugLevel level, final String message) {

		Log.d(TAG, message);
	}

	public void disconnect() {

		if (null != timer) {
			timer.cancel();
			timer = null;
		}
		if (null != peer) {
			// peer.opLeave(gameName);
			peer.disconnect();
		}
		peer = null;
	}

	@Override
	public IBinder onBind(final Intent arg0) {

		return mBinder;
	}

	@Override
	public void onCreate() {

		super.onCreate();
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		disconnect();
	}

	@Override
	public void onEvent(final EventData event) {

		// most events will contain the actorNumber of the player who sent the event, so check if the event
		// origin is known
		// get the player that raised this event
		// Player p = usersOnline.get(actorNr);
		switch (event.Code.byteValue()) {
			case LiteEventCode.Join:
				// Event is defined by Lite. A peer entered the room. It could be this peer!
				// This event provides the current list of actors and a actorNumber of the player who is new.
				// get the list of current players and check it against local list - create any that's not yet
				// there
				Log.v(TAG, "onEvent(): LiteEventCode.Join");
				// Integer[] actorsInGame = (Integer[]) event.Parameters.get(LiteEventKey.ActorList.value());
				// for (int i : actorsInGame) {
				// if (i != localUser.getId()) {
				// usersOnline.put(i, new Player(i));
				// }
				// }
				// peer.opExchangeKeysForEncryption();
				// sendPlayerInfo(); // the new peers does not have our info, so send it again
				break;
			case LiteEventCode.Leave:
				Log.v(TAG, "onEvent(): LiteEventCode.Leave");
				// Event is defined by Lite. Someone left the room.
				// usersOnline.remove(actorNr);
				break;
			case DiscussionEventCode.STRUCTURE_CHANGED:
				Log.v(TAG, "onEvent(): DiscussionEventCode.STRUCTURE_CHANGED");
				// TODO: add if parameters contain parameter key
				int structChangedActorId = ((Integer) event.Parameters
						.get(DiscussionParameterKey.STRUCT_CHANGE_ACTOR_NR)).intValue();
				if ((structChangedActorId != -1) && (structChangedActorId != localUser.getActorNr())) {
					Log.v(TAG, "Structure changed update");
					Integer topicId = (Integer) event.Parameters.get(Byte
							.valueOf(DiscussionParameterKey.CHANGED_TOPIC_ID));
					onStructureChanged(topicId.intValue());
				} else {
					Log.v(TAG, "Self structure update");
				}
				break;
			default:
				Log.v(TAG, "onEvent() default: " + event.Code);
				break;
		}
	}

	@Override
	public void onOperationResponse(final OperationResponse operationResponse) {

		byte opCode = operationResponse.OperationCode;
		short returnCode = operationResponse.ReturnCode;
		if ((opCode != LiteOpCode.RaiseEvent) || (returnCode != (short) 0)) {
			debugReturn(DebugLevel.INFO, "OnOperationResponse() " + opCode + "/" + returnCode);
		}
		// handle operation returns (aside from "join", this demo does not watch for returns)
		switch (opCode) {
			case LiteOpCode.Join:
				debugReturn(DebugLevel.INFO, "Join request: " + operationResponse.Parameters.toString());
				Log.v(TAG, "onOperationResponse(): LiteOpCode.Join");
				// get the local player's numer from the returnvalues, get the player from the list and
				// colorize it:
				if (operationResponse.Parameters.containsKey(LiteOpKey.ActorNr.value())) {
					localUser.setActorNr(((Integer) operationResponse.Parameters.get(LiteOpKey.ActorNr
							.value())).intValue());
				} else {
					Log.w(TAG, "onOperationResponse(): LiteOpCode.Join doesnt contain key actorNr");
				}
				// LocalPlayer.generateColor();
				// usersOnline.put(localUser.id, localUser);
				// sendPlayerInfoNull(false);
				// debugReturn(DebugLevel.INFO, "Local Player ID: " + localUser.id);
				break;
			case LiteOpCode.Leave:
				Log.v(TAG, "onOperationResponse(): LiteOpCode.Leave");
				// TODO: reset to some invalid or origin state
				localUser.setActorNr(-1);
				break;
			case LiteOpCode.GetProperties:
			case DiscussionOperationCode.RequestBadgeGeometry:
				throw new UnsupportedOperationException("Not implemented yet");
			default:
				Log.v(TAG, "onOperationResponse(): unknown opCode: " + opCode);
				break;
		}
	}

	@Override
	public void peerStatusCallback(final StatusCode statusCode) {

		String message;
		switch (statusCode) {
			case Connect:
				Log.v(TAG, "peerStatusCallback(): Connect");
				loginDone(true);
				TypedHashMap<Byte, Object> actorProperties = new TypedHashMap<Byte, Object>(Byte.class,
						Object.class);
				actorProperties.put(ActorPropertiesCode.Name, localUser.getName());
				actorProperties.put(ActorPropertiesCode.DbId, localUser.getId());
				OpJoinFromLobby(getDiscussionRoom(), LOBBY, actorProperties, true);
				break;
			case Disconnect:
				Log.v(TAG, "peerStatusCallback(): Connect");
				// TODO: try to reconnect
				localUser = null;
				// TODO
				break;
			case Exception_Connect:
				Log.v(TAG, "peerStatusCallback(): Exception_Connect(ed) peer.state: " + peer.getPeerState());
				message = "Exception_Connect(ed) peer.state: " + peer.getPeerState();
				debugReturn(DebugLevel.ERROR, message);
				errorOccured(message);
				break;
			case Exception:
				Log.v(TAG, "peerStatusCallback(): Exception peer.state: " + peer.getPeerState());
				message = "Exception peer.state: " + peer.getPeerState();
				debugReturn(DebugLevel.ERROR, message);
				errorOccured(message);
				break;
			case SendError:
				Log.v(TAG, "peerStatusCallback(): SendError! peer.state: " + peer.getPeerState());
				message = "SendError! peer.state: " + peer.getPeerState();
				debugReturn(DebugLevel.ERROR, message);
				errorOccured(message);
				break;
			case TimeoutDisconnect:
				Log.v(TAG, "peerStatusCallback(): TimeoutDisconnect! peer.state: " + peer.getPeerState());
				message = "TimeoutDisconnect! peer.state: " + peer.getPeerState();
				debugReturn(DebugLevel.ERROR, message);
				errorOccured(message);
				break;
			default:
				Log.v(TAG, "peerStatusCallback(): " + statusCode);
				message = "PeerStatusCallback: " + statusCode;
				debugReturn(DebugLevel.ERROR, message);
				break;
		}
	}

	public void removeCallbackListener(final PhotonServiceCallback cb) {

		if (cb != null) {
			m_callbacks.remove(cb);
		}
	}

	public void SendNotifyStructureChanged(final int activeTopicId) {

		if (!isConnected()) {
			throw new IllegalStateException(
					"You trying to send notification while not connected to the server");
		}
		TypedHashMap<Byte, Object> structureChangedParameters = new TypedHashMap<Byte, Object>(Byte.class,
				Object.class);
		structureChangedParameters.put(DiscussionParameterKey.CHANGED_TOPIC_ID, activeTopicId);
		structureChangedParameters.put(DiscussionParameterKey.STRUCT_CHANGE_ACTOR_NR, localUser.getActorNr());
		peer.opCustom(DiscussionOperationCode.NotifyStructureChanged, structureChangedParameters, true);
	}

	// Callback broadcasters
	private void errorOccured(final String message) {

		for (PhotonServiceCallback h : m_callbacks) {
			h.errorOccurred(message);
		}
	}

	private String getDiscussionRoom() {

		return dbSrvAddr + "discussion#" + discussionId;
	}

	private boolean isConnected() {

		return (peer != null) && (peer.getPeerState() == PeerStateValue.Connected.value());
	}

	private void loginDone(final boolean ok) {

		for (PhotonServiceCallback h : m_callbacks) {
			h.loginDone(ok);
		}
	}

	private void onStructureChanged(final int topicId) {

		for (PhotonServiceCallback h : m_callbacks) {
			h.onStructureChanged(topicId);
		}
	}

	private boolean OpJoinFromLobby(final String gameLobbyName, final String lobbyName,
			final TypedHashMap<Byte, Object> actorProperties, final boolean broadcastActorProperties) {

		// All operations get their parameters as key-value set (a Hashtable)
		TypedHashMap<Byte, Object> opParameters = new TypedHashMap<Byte, Object>(Byte.class, Object.class);
		opParameters.put(LiteLobbyOpKey.RoomName, gameLobbyName);
		opParameters.put(LiteLobbyOpKey.LobbyName, lobbyName);
		if (actorProperties != null) {
			opParameters.put(LiteOpKey.ActorProperties.value(), actorProperties);
			if (broadcastActorProperties) {
				opParameters.put(LiteOpKey.Broadcast.value(), broadcastActorProperties);
			}
		}
		return peer.opCustom(LiteOpCode.Join, opParameters, true);
	}

	/** Class for clients to access. Because we know this service always runs in the same process as its
	 * clients, we don't need to deal with IPC. */
	public class LocalBinder extends Binder {

		PhotonService getService() {

			return PhotonService.this;
		}
	}
}
