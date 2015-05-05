package ethz.inf.islab.smarthome;

import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by kentsay on 5/4/15.
 */
public class MainActivity extends ActionBarActivity {

    private String host = "10.2.96.166";
    private String port = "9002";
    private WebSocketClient mWebSocketClient;

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

    private void connectWebSocket() {
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
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
                TextView textView = (TextView)findViewById(R.id.messages);
                textView.setText("Handshake complete, connection build");
            }

            @Override
            public void onMessage(String s) {
                //TODO: add notification
                Log.i("WebSocket", "message receiving: " + s);
                final String message = s;

                try {
                    JSONObject msg = new JSONObject(message);
                    String command = msg.getString("post_content");
                    switch (command) {
                        case "where is my phone":
                            Log.i("WebSocket", "searching for phone, trigger ringtone");
                            playRingTone();
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = (TextView) findViewById(R.id.messages);
                        textView.setText(textView.getText() + "\n" + message);
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
    }
}
