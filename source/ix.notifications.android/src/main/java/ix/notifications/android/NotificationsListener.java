package ix.notifications.android;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.StateChangedCallback;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.transport.LongPollingTransport;

public class NotificationsListener {
    private final UUID _clientId;
    private String _serviceUrl;
    private HubConnection _connection;
    private HubProxy _hub;
    private SignalRFuture<Void> _awaitConnection;
    public final static String HUB_NOT_INITIALIZED = "The SignalR Hub has not been initialized yet. You must call start() before assigning a message handler";
    private final Lock _lock = new ReentrantLock();
    private final static String SUBSCRIPTION_NAME = "notify";
    private final static String HUB_NAME = "DeviceNotifierHub";

    public NotificationsListener(String serviceUrl, UUID clientId) {
        _serviceUrl = serviceUrl;
        _clientId = clientId;
    }

    public void start() throws ExecutionException, InterruptedException {
        _lock.lock();
        try {
            connectToDeviceNotifierService();
        } finally {
            _lock.unlock();
        }
    }

    public void stop() {
        if (_connection == null) {
            return;
        }
        _lock.lock();
        HubConnection connection;
        HubProxy hub;
        try {
            connection = _connection;
            _connection = null;
            hub = _hub;
            _hub = null;
        } finally {
            _lock.unlock();
        }
        if (hub != null)
            hub.removeSubscription(SUBSCRIPTION_NAME);
        if (connection != null)
            connection.stop();
    }

    public void addJsonNotificationHandler(JsonMessageHandler messageHandler) {
        _lock.lock();
        try {
            VerifyHubIsInitialized();
            _hub.on(SUBSCRIPTION_NAME, messageHandler, String.class);
        } finally {
            _lock.unlock();
        }
    }

    public HubConnection getConnection() {
        return _connection;
    }

    public String getServiceUrl() {
        return _serviceUrl;
    }

    private void connectToDeviceNotifierService() throws InterruptedException, ExecutionException {
        _connection = new HubConnection(_serviceUrl);
        registerForNotificationsOnConnectionConnectedWith(_connection);
        _hub = _connection.createHubProxy(HUB_NAME);
        _awaitConnection = _connection.start(new LongPollingTransport(_connection.getLogger()));
        _awaitConnection.get();
    }

    private void registerForNotificationsOnConnectionConnectedWith(HubConnection connection) {
        connection.stateChanged(new StateChangedCallback() {
                                     @Override
                                     public void stateChanged(ConnectionState oldState, ConnectionState newState) {
                                         switch (newState) {
                                             case Connected:
                                                 registerForNotifications(_clientId);
                                                 break;
                                             case Connecting:
                                             case Reconnecting:
                                             case Disconnected:
                                                 break;
                                         }
                                     }
                                 });
    }

    private void registerForNotifications(UUID clientId) {
        _hub.invoke("RegisterForNotifications", clientId);
    }

    private void VerifyHubIsInitialized() {
        if (_hub == null) {
            throw new NullPointerException(HUB_NOT_INITIALIZED);
        }
    }

}


