package in.netstack.netstacktool;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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

public class monitor extends Fragment {
    private static final String TAG = "Ping";
    static final String SERVERIP = "172.217.26.206"; // this is from Saved State
    static final String GSERVERIP = "172.217.26.206"; //index for Bundles

    String serverIP;
    int serverPort;
    EditText monitorServer = null;
    EditText monitorPort = null;
    TextView monitorReport = null;

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
        View v = inflater.inflate(R.layout.monitor_fragment, container, false);
        monitorServer = (EditText) v.findViewById(R.id.monitor_server);
        monitorPort = (EditText) v.findViewById(R.id.monitor_port);
        monitorReport = (TextView) v.findViewById(R.id.monitor_report);

        serverIP = monitorServer.getText().toString();  // save it as a class variable

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            monitorServer.setText(savedInstanceState.getString(SERVERIP));
            Log.d(TAG, "Restoring Server IP from Saved State" + monitorServer.getText().toString());
            serverIP = monitorServer.getText().toString();  // save it as a class variable
        }
        Button start_button = (Button) v.findViewById(R.id.monitor_start);
        start_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "starting monitor with: " + monitorServer.getText().toString());
                hideKeyboard(getActivity());
                eventListener.historyEvent(monitorServer.getText().toString());  // send event to Activity
                Toast.makeText(v.getContext(), "monitor start", Toast.LENGTH_SHORT).show();
                serverPort = Integer.parseInt(monitorPort.getText().toString());
                doPing myPing = new doPing(monitorServer.getText().toString(), serverPort, monitorReport);
                myPing.execute();
            }
        });
        monitorPort.setOnFocusChangeListener(new View.OnFocusChangeListener()
        { @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus == true)
                Toast.makeText(v.getContext(), "Enter port for socket() open", Toast.LENGTH_SHORT).show();
        }});

        // Spinner element
        Spinner spinner = (Spinner) v.findViewById(R.id.monitor_spinner);
        // Spinner click listener
        //spinner.setOnItemSelectedListener(this);
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("TCP");
        categories.add("HTTP");
        spinner.setSelection(0);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            serverIP = bundle.getString(GSERVERIP, "0.0.0.0");
            Log.d(TAG, " !!!!! Bundle is not null: Setting Ping Server IP from settings:" + serverIP);
            monitorServer = (EditText) getActivity().findViewById(R.id.monitor_server);
            monitorServer.setText(serverIP);
            monitorPort.setText("80");
        } else {
            Log.d(TAG, "!!!! Bundle is null");
        }
    }
}

class doMonitor extends AsyncTask<String, Boolean, Boolean> {
    String dstAddress;
    int dstPort;
    String response = "";
    InetAddress in = null;

    TextView textResponse;
    doMonitor(String addr, int port, TextView textResponse) {
        dstAddress = addr;
        dstPort = port;
        this.textResponse = textResponse;
        Log.d("doPing", "Ping Constructor called for: " + dstAddress);
    }
    protected Boolean doInBackground(String... arg0) {
        Socket socket = null;
        try {
            Log.d("doPing", "doInBackground called for " + dstAddress);
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(dstAddress, dstPort), 1000);
            }
            return true;
        } catch (Exception e) {
            Log.e("doPing", "Error", e);
            return false;
        }
    }

    protected void onPostExecute(Boolean result) {
        if (result == true)
            textResponse.setText("Ping Success !!");
        else
            textResponse.setText("Ping Failure !!");

        super.onPostExecute(result);
    }
}