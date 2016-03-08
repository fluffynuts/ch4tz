package com.github.fluffynuts.ch4tz;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
// import android.support.design.widget.Snackbar;   // get this again when we feel peckish
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;
import microsoft.aspnet.signalr.client.transport.LongPollingTransport;

public class MainActivity extends AppCompatActivity {
    private ArrayList<MessageWrapper> _messages = new ArrayList<MessageWrapper>();
    private MessageItemAdapter _adapter;
    private int _notificationId = 0;
    private HubConnection _connection;
    private HubProxy _hub;
    private ClientRegistration _registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setListViewAdapter();

        listenForMessages();
        bindFabToSendMessage();
    }

    private void setListViewAdapter() {
        _adapter = new MessageItemAdapter((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE), this, R.layout.chat_list, _messages);
        ListView listView = (ListView)findViewById(R.id.messagesList);
        listView.setAdapter(_adapter);
    }

    private void listenForMessages() {
        Platform.loadPlatformComponent(new AndroidPlatformComponent());
        String host = "http://ch4tz.azurewebsites.net";
        _connection = new HubConnection(host);
        _hub = _connection.createHubProxy("ChatHub");
        SignalRFuture<Void> awaitConnection = _connection.start(new LongPollingTransport(_connection.getLogger()));
        awaitConnection.onError(new ErrorCallback() {
            @Override
            public void onError(Throwable throwable) {
                Log.e("get connection", throwable.getMessage());
                listenForMessages();
            }
        });
        try {
            awaitConnection.get();
            displayMessagesAsNotificationsFrom(_hub);
            displayMessagesInListViewFrom(_hub);
        } catch (InterruptedException e) {
            Log.e("get connection", e.getMessage());
        } catch (ExecutionException e) {
            Log.e("get connection", e.getMessage());
        }
    }

    private void displayMessagesInListViewFrom(HubProxy hub) {
        hub.on("SendMessage", new SubscriptionHandler1<MessageWrapper>() {
            @Override
            public void run(final MessageWrapper messageWrapper) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            _adapter.add(messageWrapper);
                            _adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Log.e("update chats", e.getMessage());
                        }
                    }
                });
            }
        }, MessageWrapper.class);
    }

    private void showMessageInUI(MessageWrapper messageWrapper) {
        try {
            _messages.add(messageWrapper);
            _adapter.notifyDataSetChanged();
        } catch (Exception ex) {
            Log.e("display message", ex.getMessage());
        }
    }

    public String getSenderFrom(MessageWrapper messageWrapper) {
        return messageWrapper.Sender == null || messageWrapper.Sender.Name == null ? "(unknown)": messageWrapper.Sender.Name;
    }
    
    public String getMessageFrom(MessageWrapper messageWrapper) {
       return messageWrapper.Message == null ? "" : messageWrapper.Message;
    }

    private void displayMessagesAsNotificationsFrom(HubProxy hub) {
        hub.on("SendMessage",
                new SubscriptionHandler1<MessageWrapper>() {
                    @Override
                    public void run(MessageWrapper messageWrapper) {
                        String sender = getSenderFrom(messageWrapper);
                        String message = getMessageFrom(messageWrapper);
                        String text = sender + ": " + message;
                        showNotification("Ch4tz Message!", text, text);
                    }
                }, MessageWrapper.class);
    }

    private void bindFabToSendMessage() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void showNotification(String title, String shortText, String longText) {    // TODO: add an intent for clickable

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                                                .setSmallIcon(R.drawable.t_rex)
                                                .setContentTitle(title)
                                                .setContentText(shortText);
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText(longText);
        builder.setStyle(style);
        manager.notify(++_notificationId, builder.build());
    }

    private void sendMessage() {
        EditText text = (EditText)findViewById(R.id.messageInput);
        String messageText = text.getText().toString().trim();
        if (messageText.length() == 0) {
            return;
        }
        text.setText("");
        MessageWrapper message = new MessageWrapper();
        message.Sender = _registration;
        message.Target = "main";
        message.Message = messageText;
        _hub.invoke("SendMessage", message);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

