package in.netstack.netstacktool;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import android.support.v4.app.Fragment;

/**
 * Created by aseem on 23-09-2016.
 */

public class bgp extends Fragment{
    private static final String TAG = "BGP";
    static final String SERVERIP = "172.217.26.206"; // this is from Saved State
    static final String GSERVERIP = "172.217.26.206";  // this is from Main Activity
    EditText bgp_server;

    String serverIP;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final EditText bgp_version, bgp_myas, bgp_routerID;
        final TextView bgp_report;

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.bgp_fragment, container, false);

        bgp_server = (EditText) v.findViewById(R.id.bgp_server);
        bgp_version = (EditText) v.findViewById(R.id.bgp_version);
        bgp_myas = (EditText) v.findViewById(R.id.bgp_myas);
        bgp_routerID = (EditText) v.findViewById(R.id.bgp_routerID);
        bgp_report = (TextView) v.findViewById(R.id.bgp_report);

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
                Log.d(TAG, "starting bgp server IP: " + serverIP);
                connectToPeer(v, bgp_server.getText().toString(), 179, bgp_report);
            }
        });
        Button stop_button = (Button) v.findViewById(R.id.bgp_stop);
        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "stopping bgp server IP: " + serverIP);
                Toast.makeText(v.getContext(), "bgp stop", Toast.LENGTH_SHORT).show();
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
            Log.d(TAG, " !!!!! Bundle is not null: Setting BGP Server IP from settings" + serverIP);
            bgp_server = (EditText) getActivity().findViewById(R.id.bgp_server);
            bgp_server.setText(serverIP);
        } else {
            Log.d(TAG, "!!!! Bundle is null");
        }
    }
    private void connectToPeer(View v, String server, int port, TextView response) {
        Log.d(TAG, "connecting to bgp server IP: " + server);
        Toast.makeText(v.getContext(), "BGP connect to " + server, Toast.LENGTH_SHORT).show();
        client myClient = new client(server, port, response);
        myClient.execute();
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putString(SERVERIP, serverIP);
        Log.d(TAG, "Saving Server IP" + serverIP);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
}
