package in.netstack.netstacktool;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import static android.content.ContentValues.TAG;

public class netstack extends AppCompatActivity
        implements ping.historyEventListener, dns.historyEventListener, bgp.historyEventListener,
        ssl.historyEventListener, monitor.historyEventListener, portScanner.historyEventListener,
        history.historyEventListener {
    static final String GSERVERIP = "172.217.26.206"; //index for Bundles
    static final String GSERVERDNS = "DNS"; //index for Bundles
    static final String SERVER1 = "SERVER1", SERVER2 = "SERVER2", SERVER3 = "SERVER3", SERVER4 = "SERVER4", SERVER5 = "SERVER5";
    EditText g_server, g_dnsname;
    String server = "192.168.1.200";
    String dns_server = null;
    private static final String TAG = "main netstack";
    FragmentManager fragmentManager = getFragmentManager();
    // used to maintain FIFO History Q
    String[] h = new String[] {null, null, null, null, null};
    int hIndex = 0;
    Boolean history_selected = false;

    // Used for communciation from fragment to activity
    @Override
    public void historyEvent(String s) {
        if (s == null) return;
        Log.d(TAG, "Activity recvd from ping fragment: " +  s + " " + "saving in index: " + hIndex);
        for (int i=0;i<5;i++)  // Don't file duplicate entries
            if ((h[i] != null) && (h[i].equalsIgnoreCase(s)))
                return;
        h[hIndex] = s; hIndex++; if (hIndex > 4) hIndex = 0;
    }
    //@Override
    public void selectEvent(String s) {
        Log.d(TAG, "Activity recvd select from history fragment: " +  s + " " + "saving in index: " + hIndex);
        server = s;
        dns_server = s;
        history_selected = true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_netstack);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Start with the logo fragment
        Bundle bundle = new Bundle();
        bundle.putString(GSERVERIP, server);
        FragmentTransaction ft1 = fragmentManager.beginTransaction();
        logo logoOne = new logo();
        logoOne.setArguments(bundle);
        ft1.replace(R.id.fragment_container, logoOne, "Logo");
        ft1.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_netstack, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //setContentView(R.layout.activity_netstack);
        g_server = (EditText) findViewById(R.id.gserver);

        if (history_selected == true) {
            Log.d(TAG, "History selected: " + server);
            if (g_server != null) g_server.setText(server);
        }

        Bundle bundle = new Bundle();
        if (g_server != null) {
            Log.d(TAG, " !!!!! Setting Server IP from g_server: " + g_server.getText().toString());
            bundle.putString(GSERVERIP, g_server.getText().toString());
            bundle.putString(GSERVERDNS, dns_server);
        } else {
            Log.d(TAG, " !!!!  g_server is NULL, server: " + server);
            bundle.putString(GSERVERIP, server);
            bundle.putString(GSERVERDNS, dns_server);
        }
        bundle.putString(SERVER1, h[0]);
        bundle.putString(SERVER2, h[1]);
        bundle.putString(SERVER3, h[2]);
        bundle.putString(SERVER4, h[3]);
        bundle.putString(SERVER5, h[4]);

        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.d(TAG, "settings menu selected");
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                FragmentTransaction ft1 = fragmentManager.beginTransaction();
                logo logoOne = new logo();
                logoOne.setArguments(bundle);
                ft1.replace(R.id.fragment_container, logoOne, "Logo");
                ft1.commit();
                return true;
            case R.id.action_ping:
                Log.d(TAG, "ping menu selected");
                Toast.makeText(this, "Ping", Toast.LENGTH_SHORT).show();
                FragmentTransaction ftping = fragmentManager.beginTransaction();
                // Create arg that can be sent to fragment
                ping pingOne = new ping();
                pingOne.setArguments(bundle);
                ftping.replace(R.id.fragment_container, pingOne, "Ping");
                ftping.commit();
                return true;
            case R.id.action_dns:
                Log.d(TAG, "DNS menu selected");
                Toast.makeText(this, "DNS", Toast.LENGTH_SHORT).show();
                FragmentTransaction ftdns = fragmentManager.beginTransaction();
                dns dnsOne = new dns();
                dnsOne.setArguments(bundle);
                ftdns.replace(R.id.fragment_container, dnsOne, "DNS");
                ftdns.commit();
                return true;
            case R.id.action_ps:
                Log.d(TAG, "portScanner menu selected");
                Toast.makeText(this, "portScanner", Toast.LENGTH_SHORT).show();
                FragmentTransaction ftps = fragmentManager.beginTransaction();
                portScanner psOne = new portScanner();
                psOne.setArguments(bundle);
                ftps.replace(R.id.fragment_container, psOne, "SSL");
                ftps.commit();
                return true;
            case R.id.action_bgp:
                Log.d(TAG, "bgp menu selected");
                Toast.makeText(this, "BGP", Toast.LENGTH_SHORT).show();
                FragmentTransaction ftbgp = fragmentManager.beginTransaction();
                bgp bgpOne = new bgp();
                bgpOne.setArguments(bundle);
                ftbgp.replace(R.id.fragment_container, bgpOne, "BGP");
                ftbgp.commit();
                return true;
            case R.id.action_monitor:
                Log.d(TAG, "monitor menu selected");
                Toast.makeText(this, "Monitor", Toast.LENGTH_SHORT).show();
                FragmentTransaction ftmon = fragmentManager.beginTransaction();
                monitor monOne = new monitor();
                monOne.setArguments(bundle);
                ftmon.replace(R.id.fragment_container, monOne, "MONITOR");
                ftmon.commit();
                return true;
            case R.id.action_netinfo:
                Log.d(TAG, "netinfo menu selected");
                Toast.makeText(this, "Net Info", Toast.LENGTH_SHORT).show();
                FragmentTransaction ftnet = fragmentManager.beginTransaction();
                netInfo netOne = new netInfo();
                ftnet.replace(R.id.fragment_container, netOne, "MONITOR");
                ftnet.commit();
                return true;
            case R.id.action_history:
                Log.d(TAG, "history menu selected");
                Toast.makeText(this, "History", Toast.LENGTH_SHORT).show();
                FragmentTransaction fthis = fragmentManager.beginTransaction();
                history historyOne = new history();
                historyOne.setArguments(bundle);
                fthis.replace(R.id.fragment_container, historyOne, "HISTORY");
                fthis.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}