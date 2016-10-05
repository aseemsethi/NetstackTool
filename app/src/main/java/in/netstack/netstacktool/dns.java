package in.netstack.netstacktool;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.Socket;

import static in.netstack.netstacktool.common.hideKeyboard;

//import android.support.v4.app.Fragment;

/**
 * Created by aseem on 23-09-2016.
 */

public class dns extends Fragment{
    private static final String TAG = "DNS";
    static final String SERVERIP = "172.217.26.206"; // this is from Saved State
    static final String GSERVERIP = "8.8.8.8";  // this is from Main Activity

    String serverIP, serverDNS;
    EditText dnsServer = null;
    TextView dnsReport = null;

    public interface historyEventListener {
        public void historyEvent(String s);
    }
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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.dns_fragment, container, false);
        dnsServer = (EditText) v.findViewById(R.id.dns_server);
        dnsReport = (TextView) v.findViewById(R.id.dns_report);

        serverIP = dnsServer.getText().toString();  // save it as a class variable

        Button start_button = (Button) v.findViewById(R.id.dns_start);
        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "starting DNS");
                hideKeyboard(getActivity());
                eventListener.historyEvent(dnsServer.getText().toString().replaceAll("\\s+",""));  // send event to Activity
                Toast.makeText(v.getContext(), "DNS start", Toast.LENGTH_SHORT).show();
                doDNS myDNS = new doDNS(dnsServer.getText().toString().replaceAll("\\s+",""), dnsReport);
                myDNS.execute();
            }
        });
        dnsReport.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {}
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0) {
                    Log.d(TAG, "DNS response recvd.");
                    eventListener.historyEvent(dnsReport.getText().toString());  // send event to Activity
                }
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
            Log.d(TAG, " !!!!! Bundle is not null: Setting DNS Server IP from settings:" + serverIP);
            dnsServer = (EditText) getActivity().findViewById(R.id.dns_server);
            dnsServer.setText(serverDNS);
        } else {
            Log.d(TAG, "!!!! Bundle is null");
        }
    }
}

class doDNS extends AsyncTask<String, Boolean, String> {
    String dstAddress;
    int dstPort;
    String response = "";
    InetAddress in = null;

    TextView textResponse;
    doDNS(String addr, TextView textResponse) {
        dstAddress = addr;
        this.textResponse = textResponse;
    }
    protected String doInBackground(String... arg0) {
        Socket socket = null;
        try {
            InetAddress ipAddr = InetAddress.getByName(dstAddress);
            Log.d("doDNS", "DNS doInBackground resolved to: " + ipAddr.getHostAddress());
            return ipAddr.getHostAddress();
        } catch (Exception e) {
            Log.e("doDNS", "Error", e);
            return null;
        }
    }
    protected void onPostExecute(String result) {
        textResponse.setText(result);
        super.onPostExecute(result);
    }
}