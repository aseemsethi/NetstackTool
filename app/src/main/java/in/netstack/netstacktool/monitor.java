package in.netstack.netstacktool;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
    private static final String TAG = "Monitor";
    static final String SERVERIP = "172.217.26.206"; // this is from Saved State
    static final String GSERVERIP = "172.217.26.206"; //index for Bundles

    String serverIP;
    int serverPort, interval;
    EditText monitorServer = null;
    EditText monitorPort = null;
    EditText monitorInterval = null;
    TextView monitorReport = null;
    Spinner spinner;
    doMonitor myMon;
    doHTTP myHTTP;
    private clientCommonRecv mRecv;
    boolean doLongService = false;
    Intent intent;
    String test_type;

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
        monitorInterval = (EditText) v.findViewById(R.id.monitor_interval);
        // Spinner element
        final Spinner spinner = (Spinner) v.findViewById(R.id.monitor_spinner);
        CheckBox cb = (CheckBox) v.findViewById(R.id.monitor_cb);
        Context ctx = this.getActivity();

        //Start Intent Service
        mRecv = new clientCommonRecv(new Handler());
        mRecv.setReceiver(this);
        intent = new Intent(Intent.ACTION_SYNC, null,
                getActivity(), clientCommon.class);
        /* Send optional extras to Download IntentService */
        intent.putExtra("url", "http://stacktips.com/api/get_category_posts/?dev=1&slug=android");
        intent.putExtra("receiver", mRecv);
        intent.putExtra("requestId", 101);

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
                hideKeyboard(getActivity());
                eventListener.historyEvent(monitorServer.getText().toString());  // send event to Activity
                serverPort = Integer.parseInt(monitorPort.getText().toString());
                interval = Integer.parseInt(monitorInterval.getText().toString());
                Toast.makeText(v.getContext(), "Starting Monitor", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Starting monitor with: " + monitorServer.getText().toString() +
                        " " + "Interval: " + interval + "Type: " + test_type);
                if (doLongService == false) {
                    Log.d(TAG, "Short Lived monitor");
                    if (test_type == "TCP") {
                        myMon = new doMonitor(monitorServer.getText().toString(),
                                serverPort, interval, monitorReport);
                        myMon.execute();
                    } else if (test_type == "HTTP") {
                        myHTTP = new doHTTP(monitorServer.getText().toString(),
                                serverPort, interval, monitorReport);
                        myHTTP.execute();
                    }
                } else {
                    Log.d(TAG, "Long Lived monitor");
                    if (test_type == "HTTP") {
                        intent.putExtra("interval", interval);
                        v.getContext().startService(intent);
                    } else {
                        intent.putExtra("toAddress", monitorServer.getText().toString());
                        intent.putExtra("toPort", serverPort);
                    }
                }
            }
        });
        Button disc_button = (Button) v.findViewById(R.id.monitor_stop);
        disc_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "monitor stop", Toast.LENGTH_SHORT).show();
                eventListener.historyEvent(monitorServer.getText().toString());  // send event to Activity
                Log.d(TAG, "Disconnecting bgp server IP: " + monitorServer.getText().toString());
                if (myMon != null)
                    myMon.cancel(true); // interrupt if running is true
            }
        });
        monitorPort.setOnFocusChangeListener(new View.OnFocusChangeListener()
        { @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus == true)
                Toast.makeText(v.getContext(), "Enter port for socket() open", Toast.LENGTH_SHORT).show();
        }});
        cb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    Log.d(TAG, "monitor cb is checked");
                    doLongService = true;
        }}});

        // Spinner click listener
        //spinner.setOnItemSelectedListener(this);
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("TCP");
        categories.add("HTTP");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
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
            monitorPort.setText("80");
        } else {
            Log.d(TAG, "!!!! Bundle is null");
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case clientCommon.STATUS_RUNNING:
                //setProgressBarIndeterminateVisibility(true);
                Log.d(TAG, "Monitor fragment recvd result");
                break;
            case clientCommon.STATUS_FINISHED:
                /* Hide progress & extract result from bundle */
                /*
                String[] results = resultData.getStringArray("result");
                for (int i = 0; i<results.length; i++) {
                    monitorReport.append(results[i]);
                    monitorReport.append("\n");
                }
                */
                String answer = resultData.getString("answer");
                monitorReport.append(answer);
                monitorReport.append("\n");

                monitorReport.append("-------------------------");

                /* Update ListView with result */
                //arrayAdapter = new ArrayAdapter(MyActivity.this, android.R.layout.simple_list_item_2, results);
                //listView.setAdapter(arrayAdapter);

                break;
            case clientCommon.STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                //Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                break;
        }
    }
}

class doMonitor extends AsyncTask<String, byte[], Boolean> {
    String dstAddress;
    int dstPort, interval;
    String response = "";
    InetAddress in = null;
    byte messages[] = new byte[200];
    Socket soc = null;


    TextView textResponse;
    doMonitor(String addr, int port, int m_interval, TextView textResponse) {
        dstAddress = addr;
        dstPort = port;
        this.textResponse = textResponse;
        interval = m_interval;
        Log.d("doMonitor", "Monitor Constructor called for: " + dstAddress);
    }
    protected Boolean doInBackground(String... arg0) {
        Socket socket = null;
        boolean again = false;
        String Str3 = new String("\nDisconnecting !");
        do {
            try {
                Log.d("doMonitor", "doInBackground called for " + dstAddress + "Interval: " + interval);
                soc = new Socket();
                soc.connect(new InetSocketAddress(dstAddress, dstPort), 1000);
                // Repeated monitoring
                Log.d("doMonitor", "Publish stuff");
                Arrays.fill(messages, (byte) 0); // zero out the buffer
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                String Str1 = new String("\nMonitor " + dstAddress + " - Success at: " + currentDateTimeString);
                System.arraycopy(Str1.getBytes("UTF-8"), 0, messages, 0, Str1.length());
                publishProgress(messages);
                if (interval == 0) {
                    return true;
                }
                try {
                    Log.d("doMonitor", "Sleep interval"); again = true;
                    Thread.sleep(interval * 60 * 1000);
                    if (isCancelled()) {
                        Arrays.fill(messages, (byte) 0); // zero out the buffer
                        System.arraycopy(Str3.getBytes("UTF-8"), 0, messages, 0, Str3.length());
                        publishProgress(messages);
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Log.d("doMonitor", "Socket Open Error");
                return false;
            }
        } while(again = true);
        Log.d("doMonitor", "TCP Async Task returns");
        return true;
    }

    @Override protected void onProgressUpdate(byte[]... values) {
        byte[] data = values[0];
        String str = null;
        if (values.length > 0) {
            try {
                str = new String(data, "UTF8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "onProgressUpdate: " + values[0].length + " bytes received.");
            if (str != null)
                textResponse.append(str);
        }
    }

    protected void onPostExecute(Boolean result) {
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        if (result == true)
            textResponse.append("\nMonitor TCP Completed: " + dstAddress + " - Success at: " + currentDateTimeString);
        else
            textResponse.append("\nMonitor TCP Completed: " + dstAddress + " - Failure at: " + currentDateTimeString);
        textResponse.append("\n------------------------------");
        super.onPostExecute(result);
    }
    @Override
    protected void onCancelled(Boolean result) {
        Log.d(TAG, "onCancelled called");
        try {
            if (soc != null)
                soc.close();
            String str = new String(messages, "UTF8");
            textResponse.append(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class doHTTP extends AsyncTask<String, byte[], Boolean> {
    String dstAddress;
    int dstPort, interval;
    String response = "";
    InetAddress in = null;
    byte messages[] = new byte[200];
    Socket soc = null;


    TextView textResponse;
    doHTTP(String addr, int port, int m_interval, TextView textResponse) {
        dstAddress = addr;
        dstPort = port;
        this.textResponse = textResponse;
        interval = m_interval;
        Log.d("doHTTP", "Monitor Constructor called for: " + dstAddress);
    }
    protected Boolean doInBackground(String... arg0) {
        Socket socket = null;
        boolean again = false;
        String Str3 = new String("\nDisconnecting !");
        do {
            try {
                Log.d("doHTTP", "doInBackground called for " + dstAddress + "Interval: " + interval);
                soc = new Socket();
                soc.connect(new InetSocketAddress(dstAddress, dstPort), 1000);
                // Repeated monitoring
                Arrays.fill(messages, (byte) 0); // zero out the buffer
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                String Str1 = new String("\nMonitor " + dstAddress + " - Success at: " + currentDateTimeString);
                System.arraycopy(Str1.getBytes("UTF-8"), 0, messages, 0, Str1.length());
                publishProgress(messages);
                if (interval == 0) {
                    return true;
                }
                try {
                    Log.d("doHTTP", "Sleep interval"); again = true;
                    Thread.sleep(interval * 60 * 1000);
                    if (isCancelled()) {
                        Arrays.fill(messages, (byte) 0); // zero out the buffer
                        System.arraycopy(Str3.getBytes("UTF-8"), 0, messages, 0, Str3.length());
                        publishProgress(messages);
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Log.d("doHTTP", "Socket Open Error");
                return false;
            }
        } while(again = true);
        Log.d("doMonitor", "HTTP Async Task returns");
        return true;
    }

    @Override protected void onProgressUpdate(byte[]... values) {
        byte[] data = values[0];
        String str = null;
        if (values.length > 0) {
            try {
                str = new String(data, "UTF8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "onProgressUpdate: " + values[0].length + " bytes received.");
            if (str != null)
                textResponse.append(str);
        }
    }

    protected void onPostExecute(Boolean result) {
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        if (result == true)
            textResponse.append("\nMonitor HTTP: " + dstAddress + " - Success at: " + currentDateTimeString);
        else
            textResponse.append("\nMonitor HTTP: " + dstAddress + " - Failure at: " + currentDateTimeString);
        textResponse.append("\n------------------------------");
        super.onPostExecute(result);
    }
    @Override
    protected void onCancelled(Boolean result) {
        Log.d("doHTTP", "onCancelled called");
        try {
            if (soc != null)
                soc.close();
            String str = new String(messages, "UTF8");
            textResponse.append(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}