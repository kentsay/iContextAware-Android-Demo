package ethz.inf.islab.smarthome;

import android.app.NotificationManager;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by kentsay on 5/4/15.
 */
public class MainActivity extends ActionBarActivity {

    private String host = "188.226.178.156";
    private String port = "9000";
    private WebSocketClient mWebSocketClient;
    WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectWebSocket();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //TODO: run the app as service in the background
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void connectWebSocket() {
        Log.i("WebSocket", "Trying to connect");
        URI uri;
        try {
            uri = new URI("ws://" + host + ":" + port);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("WebSocket", "Opened");
                //send identification to the web socket server to identify your display
                mWebSocketClient.send("{\"command\": \"identify\", \"id\": \"nexus\"}");

                TextView textView = (TextView)findViewById(R.id.messages);
                textView.setText("Handshake complete, connection build");
            }

            @Override
            public void onMessage(String s) {
                Log.i("WebSocket", "message receiving: " + s);
                final String message = s;

                try {
                    JSONArray messages = new JSONArray(message);
                    for (int i = 0 ; i < messages.length(); i++) {
                        String title = messages.getJSONObject(i).getString("post_title");
                        String content = messages.getJSONObject(i).getString("post_content");
                        Log.i("WebSocket", content);
                        switch (title) {
                            case "SmartHome: where is my phone":
                                Log.i("WebSocket", "searching for phone, trigger ringtone");
                                playRingTone();
                                break;
                            case "CAB - Infobox":
                                Log.i("WebSocket", content);
                                TextView textView = (TextView) findViewById(R.id.messages);
                                textView.setText(Html.fromHtml(content, new ImageGetter(), null));
                                break;
                            default:
                                break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        TextView textView = (TextView) findViewById(R.id.messages);
//                        textView.setText(textView.getText() + "\n" + message);
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("WebSocket", "Closed: " + i + " " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("WebSocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    private class ImageGetter implements Html.ImageGetter {

        public Drawable getDrawable(String source) {
            int id;
            if (source.equals("http://188.226.178.156/wp-content/uploads/2015/05/building_details1.gif")) {
                Log.i("WebSocket", source);
                id = R.drawable.building_details;
            }
            else {
                Log.i("WebSocket", "it's not in");
                return null;
            }

            Drawable d = getResources().getDrawable(id);
            d.setBounds(0,0,500, 472);
            return d;
        }
    };


    public void sendMessage(View view) {
        EditText editText = (EditText)findViewById(R.id.message);
        mWebSocketClient.send(editText.getText().toString());
        editText.setText("");
    }

    /*
        Trigger ringtone when received Message equals to the right command
     */
    public void playRingTone() {
        RingtoneDialogFragment alert = new RingtoneDialogFragment();
        alert.show(getFragmentManager(), "FoundAlert");
        notifyMessage();
    }

    public void notifyMessage() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("SmartHome")
                        .setContentText("Someone is looking for your phone!");
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
