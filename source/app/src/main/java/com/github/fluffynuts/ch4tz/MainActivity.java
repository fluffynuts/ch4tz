package com.github.fluffynuts.ch4tz;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            //sendMessage();
                showNotification();
            }
        });
    }

    private void showNotification() {
        String textContent = "Bacon ipsum dolor amet shoulder pancetta biltong pork ham corned beef kielbasa ground round andouille turducken meatloaf pig jerky prosciutto. Cupim pork chop andouille, biltong spare ribs filet mignon tail. Pastrami ground round andouille picanha, pork spare ribs chuck hamburger. Ribeye tenderloin shank beef. Swine venison meatball t-bone landjaeger alcatra pork tail pork chop turkey hamburger sirloin chuck.";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                                                .setSmallIcon(R.mipmap.notification_icon)
                                                .setContentTitle("Meep!");
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        style.addLine(textContent);
        builder.setStyle(style);
        manager.notify(1, builder.build());
    }

    private void sendMessage() {
        EditText text = (EditText)findViewById(R.id.messageInput);
        String messageText = text.getText().toString().trim();
        if (messageText.length() == 0) {
            return;
        }
        text.setText("");

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
