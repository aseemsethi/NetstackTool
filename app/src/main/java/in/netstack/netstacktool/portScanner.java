package in.netstack.netstacktool;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static in.netstack.netstacktool.common.hideKeyboard;


/**
 * Created by aseem on 23-09-2016.
 * Android includes multiple network programming classes, e.g.,
  java.net – (Socket, URL)
  org.apache - (HttpRequest, HttpResponse)
  android.net – (URI, AndroidHttpClient, AudioStream)
 There are three main classes in Java Sockets
  ServerSocket
  Socket
  InetAddress
 For HTTP, use HttpURLConnection
 */

public class portScanner extends Fragment {
    private static final String TAG = "portScanner";
    static final String SERVERIP = "172.217.26.206"; // this is from Saved State
    static final String GSERVERIP = "172.217.26.206"; //index for Bundles

    String serverIP;
    int sPortInt, ePortInt;
    EditText psServer = null;
    EditText sPort = null;
    EditText ePort = null;
    TextView psReport = null;
    doPort myPort;

    public interface historyEventListener {public void historyEvent(String s);}
    ping.historyEventListener eventListener;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            eventListener = (ping.historyEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement historyEventListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.ps_fragment, container, false);
        psServer = (EditText) v.findViewById(R.id.port_server);
        sPort = (EditText) v.findViewById(R.id.s_port);
        ePort = (EditText) v.findViewById(R.id.e_port);

        psReport = (TextView) v.findViewById(R.id.port_report);

        serverIP = psServer.getText().toString();  // save it as a class variable

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            psServer.setText(savedInstanceState.getString(SERVERIP));
            Log.d(TAG, "Restoring Server IP from Saved State" + psServer.getText().toString());
            serverIP = psServer.getText().toString();  // save it as a class variable
        }
        Button start_button = (Button) v.findViewById(R.id.port_start);
        start_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "starting portScanner with: " + psServer.getText().toString());
                hideKeyboard(getActivity());
                eventListener.historyEvent(psServer.getText().toString());  // send event to Activity
                Toast.makeText(v.getContext(), "portScanner start", Toast.LENGTH_SHORT).show();
                sPortInt = Integer.parseInt(sPort.getText().toString());
                ePortInt = Integer.parseInt(ePort.getText().toString());
                myPort = new doPort(psServer.getText().toString(), sPortInt, ePortInt, psReport);
                myPort.execute();
            }
        });
        Button disc_button = (Button) v.findViewById(R.id.psDisc);
        disc_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Stopping Port Scanner");
                if (myPort != null)
                    myPort.cancel(true); // interrupt if running is true
            }
        });
        sPort.setOnFocusChangeListener(new View.OnFocusChangeListener()
        { @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus == true)
                Toast.makeText(v.getContext(), "Enter Sarting port for scan", Toast.LENGTH_SHORT).show();
        }});
        return v;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            serverIP = bundle.getString(GSERVERIP, "0.0.0.0");
            Log.d(TAG, " !!!!! Bundle is not null: Setting portScanner Server IP from settings:" + serverIP);
            psServer = (EditText) getActivity().findViewById(R.id.port_server);
            psServer.setText(serverIP);
            sPort.setText("1");
            ePort.setText("100");
        } else {
            Log.d(TAG, "!!!! Bundle is null");
        }
    }

    private void publishOut(String str) {
    }
    private void publishOut(byte[] data) throws UnsupportedEncodingException {
        String str;
        str = new String(data, "UTF8");
        psReport = (TextView) getActivity().findViewById(R.id.bgp_report);
        if(psReport == null) return;
        psReport.append(str);
    }
}

class doPort extends AsyncTask<String, String, Boolean> {
    String dstAddress;
    int start, end;
    String response = "";
    InetAddress in = null;
    private static final String TAG = "portScanner";
    int timeout = 1000;

    TextView textResponse;
    doPort(String addr, int sport, int eport, TextView textResponse) {
        dstAddress = addr;
        start = sport;
        end = eport;
        this.textResponse = textResponse;
        Log.d(TAG, "portScanner Constructor called for: " + dstAddress + " Ports: " + sport +":" + eport);
    }
    protected Boolean doInBackground(String... arg0) {
        byte messages[] = new byte[200];
        Socket socket = null;

        for (int i = start; i<= end; i++) {
            String Str1 = new String("\nPort: " + i + " Opened"); // initialize this
            if (isCancelled()) {
                Log.d(TAG, "\nCancelling for loop for portScanner");
                break;
            }
            try {
                socket = new Socket();
                SocketAddress address = new InetSocketAddress(dstAddress, i);
                socket.connect(address, timeout);
                //OPEN
                socket.close();
            } catch (UnknownHostException e) {
                //WRONG ADDRESS
                //Str1 = new String("\nPort: " + i + " Closed (Address)");
                Log.d(TAG, "Unknown Host"); continue;
            } catch (SocketTimeoutException e) {
                //TIMEOUT
                //Str1 = new String("\nPort: " + i + " Closed(Timeout)");
                continue;
            } catch (IOException e) {
                //CLOSED
                //Str1 = new String("\nPort: " + i + " Closed(Closed)");
                Log.d(TAG, "Closed Host"); continue;
            }
            publishProgress(Str1);
            }
        return true;
    }

    @Override
    protected void onCancelled(Boolean result) {
        Log.d(TAG, "Cancelled");
        textResponse.append("\n\nPort Scan cancelled");
    }

    @Override
    protected void onProgressUpdate(String... Str1) {
        if (Str1[0] != null)
            textResponse.append(Str1[0]);
    }
    protected void onPostExecute(Boolean result) {
        if (result == true) {
            textResponse.setTextColor(Color.BLUE);
            textResponse.append("\n\nPort Scanner completed - success");
        } else {
            textResponse.setTextColor(Color.RED);
            textResponse.append("\n\nPort Scanner completed - failure");
        }
        super.onPostExecute(result);
    }
}