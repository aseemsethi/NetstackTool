package in.netstack.netstacktool;

import android.app.Fragment;
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

import java.net.InetAddress;
import java.net.Socket;

//import android.support.v4.app.Fragment;

/**
 * Created by aseem on 23-09-2016.
 */

public class dns extends Fragment{
    private static final String TAG = "DNS";
    static final String SERVERIP = "172.217.26.206"; // this is from Saved State
    static final String GSERVERIP = "172.217.26.206";  // this is from Main Activity
    static final String GSERVERDNS = "www.cisco.com";  // this is from Main Activity

    String serverIP, serverDNS;
    EditText dnsServer = null;
    TextView dnsReport = null;

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
                Toast.makeText(v.getContext(), "DNS start", Toast.LENGTH_SHORT).show();
                doDNS myDNS = new doDNS(dnsServer.getText().toString(), dnsReport);
                myDNS.execute();
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
            serverDNS = bundle.getString(GSERVERDNS, "www.cisco.com");
            Log.d(TAG, " !!!!! Bundle is not null: Setting DNS Server IP from settings:" + serverIP);
            dnsServer = (EditText) getActivity().findViewById(R.id.dns_server);
            dnsServer.setText(serverDNS);
        } else {
            Log.d(TAG, "!!!! Bundle is null");
        }
    }
}

class doDNS extends AsyncTask<String, Boolean, Boolean> {
    String dstAddress;
    int dstPort;
    String response = "";
    InetAddress in = null;

    TextView textResponse;
    doDNS(String addr, TextView textResponse) {
        dstAddress = addr;
        this.textResponse = textResponse;
    }
    protected Boolean doInBackground(String... arg0) {
        Socket socket = null;
        try {
            Log.d("doDNS", "doInBackground called for " + dstAddress);
            if (in.getByName(dstAddress).isReachable(5000))
                return true;
            else
                return false;
        } catch (Exception e) {
            Log.e("doDNS", "Error", e);
            return false;
        }
    }
    protected void onPostExecute(Boolean result) {
        if (result == true)
            textResponse.setText("DNS Success");
        else
            textResponse.setText("DNS Failure");

        super.onPostExecute(result);
    }
}