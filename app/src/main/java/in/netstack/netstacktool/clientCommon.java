package in.netstack.netstacktool;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by aseem on 04-10-2016.
 */

public class clientCommon extends AsyncTask<String, byte[], Boolean> {
    private static final String TAG = "Common Client";
    String dstAddress;
    int dstPort;
    String response = "";
    boolean connected = false;
    InetAddress in = null;
    Socket socket = null;
    InputStream is;
    OutputStream os;
    byte buffer[] = new byte[4096];

    TextView textResponse, progress;
    clientCommon(String addr, int port, TextView response, TextView progress) {
        dstAddress = addr;
        dstPort = port;
        this.textResponse = response;
        this.progress = progress;
    }
    protected Boolean doInBackground(String... arg0) {
        String Str1 = new String("Trying Connect....");
        String Str2 = new String("Connected !");

        try {
            Log.d(TAG, "doInBackground called with: " + dstAddress);
            System.arraycopy(Str1.getBytes("UTF-8"), 0, buffer, 0, Str1.length());
            publishProgress(buffer);
            socket = new Socket(dstAddress, dstPort);
            Log.d(TAG, "socket connected");
            Arrays.fill(buffer, (byte) 0); // zero out the buffer
            System.arraycopy(Str2.getBytes("UTF-8"), 0, buffer, 0, Str2.length());
            publishProgress(buffer);
            is = socket.getInputStream();
            os = socket.getOutputStream();
            //This is blocking
            int read;
            while((read = is.read(buffer, 0, 512)) > 0 ) {
                byte[] idata = new byte[read];
                Log.i(TAG, "!!!!      Recvd data bytes: " + read);
                System.arraycopy(buffer, 0, idata, 0, read); // since buffer could be overwritten
                publishProgress(idata);
            }
        } catch (Exception e) {
            Log.e("ClientActivity", "C: Error", e);
            connected = false;
            return false;
        } finally {
            try {
                is.close();
                os.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, "Finished");
        }
        return true;
    }

    public boolean SendDataToNetwork(final byte[] cmd) { //You run this from the main thread.
        if ((socket != null) && (socket.isConnected() == true)) {
            Log.d(TAG, "SendDataToNetwork: Writing received message to socket");
            new Thread(new Runnable() {
                public void run() {
                    try {
                        //for (int index = 0; index < 20; index++) {
                        //   Log.i(TAG, String.format("0x%20x", cmd[index])); }
                        os.write(cmd);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Log.i(TAG, "SendDataToNetwork: Message send failed. Caught an exception");
                    }
                }
            }
            ).start();
            return true;
        }
        else {
            Log.i(TAG, "SendDataToNetwork: Cannot send message. Socket is closed");
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(byte[]... values) {
        if (values.length > 0) {
            Log.d(TAG, "onProgressUpdate: " + values[0].length + " bytes received.");
            //appendToOutput(values[0]);
        }
    }
    protected void onPostExecute(Boolean result) {
        Log.d("ClientActivity", "onPostExecute called for " + result);
        if (result == true)
            textResponse.setText("Disconnected to Port");
        else
            textResponse.setText("Disconnection Failed to Port");
        super.onPostExecute(result);
    }
} // end async task class
