package in.netstack.netstacktool;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

//import android.support.v4.app.Fragment;

/**
 * Created by aseem on 23-09-2016.
 */

import in.netstack.netstacktool.BgpPacket.bgpPacket;

public class bgp extends Fragment{
    private static final String TAG = "BGP";
    static final String SERVERIP = "172.217.26.206"; // this is from Saved State
    static final String GSERVERIP = "172.217.26.206";  // this is from Main Activity
    EditText bgp_server;
    TextView bgp_report;
    client myClient;
    String serverIP;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final EditText bgp_version, bgp_myas, bgp_routerID;
        final TextView bgp_report;

        bgpPacket pack = bgpPacket.newBuilder()
                .setVersion(4)
                .build();
        /*
        bgpPacket.Builder pack2 = bgpPacket.newBuilder();
        pack2.setVersion(1);
        bgpPacket pack3 = bgpPacket.newBuilder()
                        .setVersion(1)
                        .build();
        */

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.bgp_fragment, container, false);

        bgp_server = (EditText) v.findViewById(R.id.bgp_server);
        bgp_version = (EditText) v.findViewById(R.id.bgp_version);
        bgp_myas = (EditText) v.findViewById(R.id.bgp_myas);
        bgp_routerID = (EditText) v.findViewById(R.id.bgp_routerID);
        bgp_report = (TextView) v.findViewById(R.id.bgp_report);
        int version = pack.getVersion();
        Log.d(TAG, "Setting BGP version to: " + pack.getVersion());
        // Integer.parseInt(myEditText.getText().toString())
        bgp_version.setText(String.valueOf(version));

        serverIP = bgp_server.getText().toString();  // save it as a class variable

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            bgp_server.setText(savedInstanceState.getString(SERVERIP));
            Log.d(TAG, "Restoring Server IP from Saved State" + bgp_server.getText().toString());
            serverIP = bgp_server.getText().toString();  // save it as a class variable
        }

        Button start_button = (Button) v.findViewById(R.id.bgp_start);

        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "starting bgp server IP: " + bgp_server.getText().toString());
                myClient = new client(bgp_server.getText().toString(), 179, bgp_report); //179
                myClient.execute();
            }
        });
        Button stop_button = (Button) v.findViewById(R.id.bgp_stop);
        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "send data: " + serverIP);
                Toast.makeText(v.getContext(), "bgp send data", Toast.LENGTH_SHORT).show();
                sendKeepalive();
            }
        });
        return v;
    }
    public void sendKeepalive() {
        byte[] keepAlive = new byte[]{
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                0x00, 0x13, // Length = 19
                (byte) 0x04, // type code KEEPALIVE }
        };
        myClient.SendDataToNetwork(keepAlive);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private void appendToOutput(String str) {
        Log.d(TAG, "appendToOutput 2 called with: " + str);

        bgp_report = (TextView) getActivity().findViewById(R.id.bgp_report);
        if(bgp_report == null) return;
        bgp_report.append(str);
        bgp_report.append("\n");
        bgp_report.setMovementMethod(new ScrollingMovementMethod());
    }

    private void appendToOutput(byte[] data) {
        String str;
        try {
            str = new String(data, "UTF8");
            /*
            for (int index = 0; index < data.length; index++) {
             Log.i(TAG, String.format("0x%20x", data[index])); }
            */
            appendToOutput(str);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            serverIP = bundle.getString(GSERVERIP, "0.0.0.0");
            Log.d(TAG, " !!!!! Bundle is not null: Setting BGP Server IP from settings" + serverIP);
            bgp_server = (EditText) getActivity().findViewById(R.id.bgp_server);
            bgp_server.setText(serverIP);
        } else {
            Log.d(TAG, "!!!! Bundle is null");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putString(SERVERIP, serverIP);
        Log.d(TAG, "Saving Server IP" + serverIP);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public class client extends AsyncTask<String, byte[], Boolean> {
        private static final String TAG = "BGP Client";
        String dstAddress;
        int dstPort;
        String response = "";
        boolean connected = false;
        InetAddress in = null;
        Socket socket = null;
        InputStream is;
        OutputStream os;
        byte buffer[] = new byte[4096];

        TextView textResponse;
        client(String addr, int port, TextView response) {
            dstAddress = addr;
            dstPort = port;
            this.textResponse = response;
        }
        protected Boolean doInBackground(String... arg0) {
            String Str1 = new String("Trying Connect");
            String Str2 = new String("Connected");

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
                            for (int index = 0; index < 20; index++) {
                                Log.i(TAG, String.format("0x%20x", cmd[index])); }
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
            else
                Log.i(TAG, "SendDataToNetwork: Cannot send message. Socket is closed");
            return false;
        }

        @Override
        protected void onProgressUpdate(byte[]... values) {
            if (values.length > 0) {
                Log.d(TAG, "onProgressUpdate: " + values[0].length + " bytes received.");
                appendToOutput(buffer);
            }
        }
        protected void onPostExecute(Boolean result) {
            Log.d("ClientActivity", "onPostExecute called for " + result);
            if (result == true)
                textResponse.setText("Disconnected to BGP Port");
            else
                textResponse.setText("Disconnection Failed to BGP Port");
            super.onPostExecute(result);
        }
    } // end async task class
} // end activity
