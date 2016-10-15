package in.netstack.netstacktool;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by aseem on 23-09-2016.
 */

public class netInfo extends Fragment {
    private static final String TAG = "netInfo";

    TextView netReport = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.net_fragment, container, false);
        netReport= (TextView) v.findViewById(R.id.net_report);

        Button nw_button = (Button) v.findViewById(R.id.nw_info);
        nw_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Net Info start", Toast.LENGTH_SHORT).show();
                getNetworks(netReport);
            }
        });
        return v;
    }

    public static String getMobileIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public void getNetworks(TextView netReport) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connectivityManager.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    Log.d(TAG, "NETWORKNAME (Lolipop): " + networkInfo.getTypeName());
                    netReport.append("\n\n" + networkInfo.getTypeName());
                    if (networkInfo.isConnected() == true)
                        netReport.append(" : Connected");
                    else
                        netReport.append(" : Disconnected");
                    if (networkInfo.getTypeName().equalsIgnoreCase("WIFI")) {
                        WifiManager wm = (WifiManager) getActivity().getSystemService(WIFI_SERVICE);
                        @SuppressWarnings("deprecation")
                        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                        netReport.append("\n IP Address : " + ip);
                        netReport.append("\nSSID : " + networkInfo.getExtraInfo());
                    } else if (networkInfo.getTypeName().equalsIgnoreCase("MOBILE")) {
                        netReport.append("\n IP Address : " + getMobileIpAddress());
                        netReport.append("\nNetwork : " + networkInfo.getExtraInfo());
                    }
                }
            }
        }else {
            if (connectivityManager != null) {
                //noinspection deprecation
                NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            Log.d(TAG, "NETWORKNAME: " + anInfo.getTypeName());
                            netReport.append("\n\n" + anInfo.getTypeName());
                            if (anInfo.isConnected() == true)
                                netReport.append(" : Connected");
                            else
                                netReport.append(" : Disconnected");
                            if (anInfo.getTypeName().equalsIgnoreCase("WIFI")) {
                                WifiManager wm = (WifiManager) getActivity().getSystemService(WIFI_SERVICE);
                                @SuppressWarnings("deprecation")
                                String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                                netReport.append("\n IP Address : " + ip);
                                netReport.append("\nSSID : " + anInfo.getExtraInfo());
                            } else if (anInfo.getTypeName().equalsIgnoreCase("MOBILE")) {
                                netReport.append("\n IP Address : " + getMobileIpAddress());
                                netReport.append("\nNetwork : " + anInfo.getExtraInfo());
                            }
                        }
                    }
                }
            }
        }
    }
}