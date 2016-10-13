package in.netstack.netstacktool;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;
import static android.content.Context.ACTIVITY_SERVICE;
import static in.netstack.netstacktool.R.id.monitor_interval;
import static in.netstack.netstacktool.R.id.text;
import static in.netstack.netstacktool.R.id.textView;
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

public class monitor extends Fragment implements
        clientCommonRecv.Receiver{
    private static final String TAG = "Monitor clientCommon";
    static final String SERVERIP = "172.217.26.206"; // this is from Saved State
    static final String GSERVERIP = "172.217.26.206"; //index for Bundles

    String serverIP;
    int serverPort, interval;
    EditText monitorServer = null;
    EditText monitorPort = null;
    EditText monitorInterval = null;
    TextView monitorReport = null;
    private clientCommonRecv mRecv;
    boolean doLongService = false;
    Intent intent;
    String test_type;
    View v;

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
    public void onResume() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("monitor-event"));
        super.onResume();
        Log.d("aseemview on resume", v.toString());
    }
    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                mMessageReceiver);
        super.onPause();
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            monitorReport = (TextView) v.findViewById(R.id.monitor_report);
            monitorReport.append("\n" + message);
            Log.d(TAG, "Bcst rcv: " + message);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.monitor_fragment, container, false);
        monitorServer = (EditText) v.findViewById(R.id.monitor_server);
        monitorPort = (EditText) v.findViewById(R.id.monitor_port);
        monitorReport = (TextView) v.findViewById(R.id.monitor_report);
        monitorInterval = (EditText) v.findViewById(R.id.monitor_interval);
        // Spinner element
        final Spinner spinner = (Spinner) v.findViewById(R.id.monitor_spinner);
        final Context ctx = this.getActivity();

        serverIP = monitorServer.getText().toString();  // save it as a class variable

        /* LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("monitor-event"));
        */
        boolean found = false;
        final ActivityManager manager = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if (clientCommon.class.getName().equals(service.service.getClassName())) {
                Log.d("TAG", "Monitor Service is Running !!!");
                found = true;
            }
        }
        //Start Intent Service
        if (found == false) {
            mRecv = new clientCommonRecv(new Handler());
            mRecv.setReceiver(this);
            intent = new Intent(Intent.ACTION_SYNC, null,
                    getActivity(), clientCommon.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("receiver", mRecv);
            intent.putExtra("requestId", 101);
        } else {
            Log.d(TAG, "Not starting Monitor Service again !!!!!!!");
        }

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            monitorServer.setText(savedInstanceState.getString(SERVERIP));
            Log.d("aseemview", "Restoring Server IP from Saved State" + monitorServer.getText().toString());
            serverIP = monitorServer.getText().toString();  // save it as a class variable
        }
        Button start_button = (Button) v.findViewById(R.id.monitor_start);
        start_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(getActivity());
                eventListener.historyEvent(monitorServer.getText().toString());  // send event to Activity
                serverPort = Integer.parseInt(monitorPort.getText().toString());
                interval = Integer.parseInt(monitorInterval.getText().toString());
                Toast.makeText(v.getContext(), "Starting Monitor", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Starting monitor with: " + monitorServer.getText().toString() +
                        " " + "Interval: " + interval + "Type: " + test_type);
                    if (test_type == "HTTP") {
                        intent.putExtra("interval", interval);
                        intent.putExtra("test", "HTTP");
                        intent.putExtra("url", monitorServer.getText().toString());
                        v.getContext().startService(intent);
                    } else {
                        intent.putExtra("toAddress", monitorServer.getText().toString());
                        intent.putExtra("toPort", serverPort);
                        intent.putExtra("test", "TCP");
                        v.getContext().startService(intent);
                    }
                }
        });
        Button disc_button = (Button) v.findViewById(R.id.monitor_stop);
        disc_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "monitor stop", Toast.LENGTH_SHORT).show();
                eventListener.historyEvent(monitorServer.getText().toString());  // send event to Activity
                Log.d(TAG, "Disconnecting Monitor server IP: " + monitorServer.getText().toString());
                v.getContext().stopService(new Intent(getActivity(), clientCommon.class));
            }
        });
        monitorPort.setOnFocusChangeListener(new View.OnFocusChangeListener()
        { @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus == true)
                Toast.makeText(v.getContext(), "Enter port for socket() open", Toast.LENGTH_SHORT).show();
        }});

        // Spinner click listener
        List<String> categories = new ArrayList<String>();
        categories.add("TCP");
        categories.add("HTTP");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);
        spinner.setSelection(1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                test_type = spinner.getSelectedItem().toString();
                Log.d(TAG, "spinner selected: " + test_type);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            serverIP = bundle.getString(GSERVERIP, "0.0.0.0");
            Log.d(TAG, " !!!!! Bundle is not null: Setting Monitor Server IP from settings:" + serverIP);
            monitorServer = (EditText) getActivity().findViewById(R.id.monitor_server);
            monitorServer.setText(serverIP);
            monitorPort.setText("443");
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

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        /*
        monitorReport = (TextView) v.findViewById(R.id.monitor_report);
        switch (resultCode) {
            case clientCommon.STATUS_RUNNING:
                Log.d(TAG, "Monitor fragment recvd result Running");
                monitorReport.append("!");
                break;
            case clientCommon.STATUS_FINISHED:
                Log.d(TAG, "Monitor fragment recvd result Finished");
                String answer = resultData.getString("answer");
                if (answer != null) {
                    monitorReport.append(answer);
                    monitorReport.append("\n");
                    monitorReport.append("-------------------------");
                }
                break;
            case clientCommon.STATUS_ERROR:
                String error = resultData.getString(Intent.EXTRA_TEXT);
                break;
        }
        */
    }
}