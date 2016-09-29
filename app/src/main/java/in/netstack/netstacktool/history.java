package in.netstack.netstacktool;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import android.support.v4.app.Fragment;

/**
 * Created by aseem on 23-09-2016.
 */

public class history extends Fragment{
    private static final String TAG = "History";
    static final String SERVER1 = "SERVER1", SERVER2 = "SERVER2", SERVER3 = "SERVER3", SERVER4 = "SERVER4", SERVER5 = "SERVER5";
    TextView hServer = null;
    TextView hserver1 = null, hserver2 = null, hserver3 = null, hserver4 = null, hserver5 = null;

    String selectedServer;

    public interface historyEventListener {
        public void historyEvent(String s);
        public void selectEvent(String s);
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
        View v = inflater.inflate(R.layout.history_fragment, container, false);
        final Button select_button = (Button) v.findViewById(R.id.history_select);
        hserver1 = (TextView) v.findViewById(R.id.hserver1);
        hserver2 = (TextView) v.findViewById(R.id.hserver2);
        hserver3 = (TextView) v.findViewById(R.id.hserver3);
        hserver4 = (TextView) v.findViewById(R.id.hserver4);
        hserver5 = (TextView) v.findViewById(R.id.hserver5);

        select_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "starting ping");
                Toast.makeText(v.getContext(), "history selected", Toast.LENGTH_SHORT).show();
                eventListener.selectEvent(selectedServer);  // send event to Activity
            }
        });
        CheckBox checkBox1 = (CheckBox) v.findViewById(R.id.checkBox1);
        CheckBox checkBox2 = (CheckBox) v.findViewById(R.id.checkBox2);
        CheckBox checkBox3 = (CheckBox) v.findViewById(R.id.checkBox3);
        CheckBox checkBox4 = (CheckBox) v.findViewById(R.id.checkBox4);
        CheckBox checkBox5 = (CheckBox) v.findViewById(R.id.checkBox5);
        checkBox1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    Log.d(TAG, "CheckBox1 is checked");
                    selectedServer = hserver1.getText().toString();
                } else Log.d(TAG, "CheckBox1 is unchecked"); }});
        checkBox2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    Log.d(TAG, "CheckBox2 is checked");
                    selectedServer = hserver2.getText().toString();
                } else Log.d(TAG, "CheckBox2 is unchecked"); }});
        checkBox3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    Log.d(TAG, "CheckBox3 is checked");
                    selectedServer = hserver2.getText().toString();
                } else Log.d(TAG, "CheckBox3 is unchecked"); }});
        checkBox4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    Log.d(TAG, "CheckBox4 is checked");
                    selectedServer = hserver2.getText().toString();
                } else Log.d(TAG, "CheckBox4 is unchecked"); }});
        checkBox5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    Log.d(TAG, "CheckBox5 is checked");
                    selectedServer = hserver2.getText().toString();
                } else Log.d(TAG, "CheckBox5 is unchecked"); }});
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            Log.d(TAG, " !!!!! Bundle is not null: Setting History Server IP from settings:");
            String server1 = bundle.getString(SERVER1, "0.0.0.0");
            hServer = (TextView) getActivity().findViewById(R.id.hserver1);
            hServer.setText(server1);

            String server2 = bundle.getString(SERVER2, "0.0.0.0");
            hServer = (TextView) getActivity().findViewById(R.id.hserver2);
            hServer.setText(server2);

            String server3 = bundle.getString(SERVER3, "0.0.0.0");
            hServer = (TextView) getActivity().findViewById(R.id.hserver3);
            hServer.setText(server3);

            String server4 = bundle.getString(SERVER4, "0.0.0.0");
            hServer = (TextView) getActivity().findViewById(R.id.hserver4);
            hServer.setText(server4);

            String server5 = bundle.getString(SERVER5, "0.0.0.0");
            hServer = (TextView) getActivity().findViewById(R.id.hserver5);
            hServer.setText(server5);
        } else {
            Log.d(TAG, "!!!! Bundle is null");
        }
    }
}
