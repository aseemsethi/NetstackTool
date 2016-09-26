package in.netstack.netstacktool;

import android.os.AsyncTask;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by aseem on 25-09-2016.
 */

/*
 * Takes String as parameter, void is the return for onProgress,
 * and Boolean is the postProgress return type.
 */
public class client extends AsyncTask<String, Boolean, Boolean> {
    String dstAddress;
    int dstPort;
    String response = "";
    boolean connected = false;
    InetAddress in = null;

    TextView textResponse;
    client(String addr, int port, TextView textResponse) {
        dstAddress = addr;
        dstPort = port;
        this.textResponse = textResponse;
    }
    protected Boolean doInBackground(String... arg0) {
        Socket socket = null;
        try {
            // for Ping use in.getByName(dstAddress)
            Log.d("ClientActivity", "doInBackground called for " + dstAddress);
            socket = new Socket(dstAddress, dstPort);
        } catch (Exception e) {
            Log.e("ClientActivity", "C: Error", e);
            connected = false;
            return false;
        }
        return true;
    }
    protected void onPostExecute(Boolean result) {
        if (result == true)
            textResponse.setText("Connected");
        else
            textResponse.setText("Failed to Connect");

        super.onPostExecute(result);
    }
}
