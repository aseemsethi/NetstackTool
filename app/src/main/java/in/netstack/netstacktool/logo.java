package in.netstack.netstacktool;

import android.app.Fragment;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import static android.R.attr.fragment;

/**
 * Created by aseem on 23-09-2016.
 */

public class logo extends Fragment {
    private static final String TAG = "settings";
    static final String SERVERIP = "172.217.26.206"; // this is from Saved State
    static final String GSERVERIP = "172.217.26.206";  // this is from Main Activity

    EditText server;
    String serverIP;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.logo_fragment, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            serverIP = bundle.getString(GSERVERIP, "172.217.26.206");
            Log.d(TAG, "Bundle is not null:"
                    + serverIP);
            server = (EditText) getActivity().findViewById(R.id.gserver);
            server.setText(serverIP);
        } else {
            Log.d(TAG, "!!!! Bundle is null");
        }
    }
}
