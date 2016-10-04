package in.netstack.netstacktool;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

//import android.support.v4.app.Fragment;

/**
 * Created by aseem on 23-09-2016.
 */

import in.netstack.netstacktool.BgpPacket.bgpPacket;

import static in.netstack.netstacktool.common.hideKeyboard;

public class bgp extends Fragment{
    private static final String TAG = "BGP";
    static final String SERVERIP = "172.217.26.206"; // this is from Saved State
    static final String GSERVERIP = "172.217.26.206"; //index for Bundles
    EditText bgp_server;
    TextView bgp_report;
    client myClient;
    String serverIP;
    public static final short BGP_PACKET_MARKER_LENGTH = 16;
    public static final short BGP_PACKET_HEADER_LENGTH = 19;
    public static final int BGP_VERSION = 4;
    public static final int BGP_PORT = 179;

    public interface historyEventListener {public void historyEvent(String s);}
    historyEventListener eventListener;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            eventListener = (historyEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement historyEventListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final EditText bgp_version, bgp_myas, bgp_routerID;
        final TextView bgp_report;

        // TBD: Remove the following block
        bgpPacket.Builder pack = bgpPacket.newBuilder();
        pack.setLen("19");
        pack.setType("4");
        pack.setVersion("4");
        pack.setMyas("1");
        pack.setId("192.168.1.3");
        // Remove - used to see how protobuf is setup

        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.bgp_fragment, container, false);

        bgp_server = (EditText) v.findViewById(R.id.bgp_server);
        bgp_version = (EditText) v.findViewById(R.id.bgp_version);
        bgp_myas = (EditText) v.findViewById(R.id.bgp_myas);
        bgp_routerID = (EditText) v.findViewById(R.id.bgp_routerID);
        bgp_report = (TextView) v.findViewById(R.id.bgp_report);
        //int version = pack.getVersion();
        bgp_version.setText(String.valueOf(pack.getVersion()));
        bgp_myas.setText(String.valueOf(pack.getMyas()));
        bgp_routerID.setText(pack.getId());

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
            public void onClick(View view) {
                hideKeyboard(v.getContext());
                eventListener.historyEvent(bgp_server.getText().toString());  // send event to Activity
                Log.d(TAG, "starting bgp server IP: " + bgp_server.getText().toString());
                myClient = new client(bgp_server.getText().toString(), BGP_PORT, bgp_report); //179
                myClient.execute();
            }
        });
        Button open_button = (Button) v.findViewById(R.id.bgp_open);
        open_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "send data: " + serverIP);
                if (myClient == null)
                    Toast.makeText(v.getContext(), "bgp: no connection", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(v.getContext(), "bgp send data", Toast.LENGTH_SHORT).show();
                try {
                    sendOpen(bgp_myas.getText().toString());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                sendKeepalive();
            }
        });
        return v;
    }
    public void sendKeepalive() {
        short pktSize = 0 + BGP_PACKET_HEADER_LENGTH;
        // Create a non-direct ByteBuffer with a 10 byte capacity
        // The underlying storage is a byte array.
        ByteBuffer buffer = ByteBuffer.allocate(pktSize);
        buffer.order(ByteOrder.BIG_ENDIAN);

        for(int i=0; i<BGP_PACKET_MARKER_LENGTH; i++)
            buffer.put((byte)0xFF); // at position 0
        Log.d(TAG, String.valueOf(buffer.position()));
        buffer.putShort(pktSize);
        Log.d(TAG, String.valueOf(buffer.position()));
        buffer.put((byte)(4)); // KeepAlive
        Log.d(TAG, String.valueOf(buffer.position()));
        Log.d(TAG, "Number of bytes sent: " + String.valueOf(buffer.position()));
        buffer.flip();
        if (myClient != null)
            myClient.SendDataToNetwork(buffer.array());
    }

    public void sendOpen(String myas) throws UnknownHostException {
        short pktSize = 10 + BGP_PACKET_HEADER_LENGTH;
        short myasInt = Short.valueOf(myas);
        InetAddress ip = InetAddress.getByName("192.168.1.103");
        byte[] bytes = ip.getAddress();

        // Create a non-direct ByteBuffer with a 10 byte capacity
        // The underlying storage is a byte array.
        ByteBuffer buffer = ByteBuffer.allocate(pktSize);
        buffer.order(ByteOrder.BIG_ENDIAN);

        for(int i=0; i<BGP_PACKET_MARKER_LENGTH; i++)
            buffer.put((byte)0xFF); // at position 0
        Log.d(TAG, String.valueOf(buffer.position()));
        buffer.putShort(pktSize);  // Size
        Log.d(TAG, String.valueOf(buffer.position()));
        buffer.put((byte)(1)); // Open

        buffer.put((byte)(4)); // Version
        buffer.putShort(myasInt); // my AS
        buffer.putShort((short)120); // hold time
        for (byte b : bytes) {
            buffer.put((byte)(b & 0xFF));
        }
        buffer.put((byte)(0)); // Open

        Log.d(TAG, String.valueOf(buffer.position()));
        Log.d(TAG, "Number of bytes sent: " + String.valueOf(buffer.position()));
        buffer.flip();
        if (myClient != null)
            myClient.SendDataToNetwork(buffer.array());
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
        bgp_report.setMovementMethod(new ScrollingMovementMethod());
    }

    private void appendToOutput(byte[] data) {
        String str;
        if (data[0] != -1) {
            ;  // since all BGP messages has FF as marker, which is -1
            // To print other messages - non data
            try {
                str = new String(data, "UTF8");
                appendToOutput(str);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }return;
        }

        bgp_report = (TextView) getActivity().findViewById(R.id.bgp_report);
        if(bgp_report == null) return;
        Log.d(TAG, "Received: " + data[0] + "  " + data[18]);
        switch(data[18]) {
            case 1: bgp_report.append("\n" + "Recvd Open"); break;
            case 2: bgp_report.append("\n" + "Recvd Update"); break;
            case 3: bgp_report.append("\n" + "Recvd Notify"); break;
            case 4: bgp_report.append("\n" + "Recvd Keepalive"); break;
            case 5: bgp_report.append("\n" + "Recvd Route Refresh"); break;
            default: bgp_report.append("\n" + "Recvd Unknown Type: " + data[18]); break;
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
                myClient = null;
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
            myClient = null;
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
                myClient = null;
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(byte[]... values) {
            if (values.length > 0) {
                Log.d(TAG, "onProgressUpdate: " + values[0].length + " bytes received.");
                //appendToOutput(new String(values[0]));
                appendToOutput(values[0]);
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
