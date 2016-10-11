package in.netstack.netstacktool;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

import static android.app.DownloadManager.STATUS_RUNNING;

public class clientCommon extends IntentService{
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    private static final String TAG = "clientCommon";
    Bundle bundle = new Bundle();
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public clientCommon() {
        super(clientCommon.class.getName());
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public clientCommon(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final ResultReceiver recv = intent.getParcelableExtra("receiver");
        String url = intent.getStringExtra("url");
        int interval = intent.getIntExtra("interval", 0);
        String test = intent.getStringExtra("test");
        String toAddress = intent.getStringExtra("toAddress");
        int toPort = intent.getIntExtra("toPort", 80);
        String passed = "\nHTTP GET Passed";
        String failed = "\nHTTP GET Failed";
        Log.d(TAG, "Service Starting with: " + url + " Interval: " + interval);
        if (!TextUtils.isEmpty(url)) {
            Log.d(TAG, "clientCommon reporting Status!");
            recv.send(STATUS_RUNNING, Bundle.EMPTY);
            String[] results = new String[0];
            try {
                if (test == "HTTP")
                    results = downloadData(url);
                else if (test == "TCP")
                    results = tcpConnect(toAddress, toPort);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DownloadException e) {
                e.printStackTrace();
            }
            /* Sending result back to activity */
            if (null != results && results.length > 0) {
                bundle.putStringArray("result", results);
                bundle.putString("answer", passed);
                Log.d(TAG, "clientCommon reporting Finished Status!");
                recv.send(STATUS_FINISHED, bundle);
            } else {
                bundle.putString("answer", failed);
                Log.d(TAG, "clientCommon got no results");
            }
        }
        Log.d(TAG, "clientCommon Service Stopping!");
        this.stopSelf();
    }

    private String[] tcpConnect(String dstAddress, int dstPort) throws IOException, DownloadException {
        String[] results = null;
        Socket soc = null;
        soc = new Socket();
        try {
            soc.connect(new InetSocketAddress(dstAddress, dstPort), 1000);
            results = new String[1];
            results[0] = "TCP Monitor Passed";
            } catch (Exception e) {
                Log.d("doHTTP", "Socket Open Error");
                results[0] = "TCP Monitor Failed";
        }
        return results;
    }

    private String[] downloadData(String requestUrl) throws IOException, DownloadException {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;

        /* forming th java.net.URL object */
        URL url = new URL(requestUrl);
        urlConnection = (HttpURLConnection) url.openConnection();
        /* optional request header */
        urlConnection.setRequestProperty("Content-Type", "application/json");
        /* optional request header */
        urlConnection.setRequestProperty("Accept", "application/json");
        /* for Get request */
        urlConnection.setRequestMethod("GET");
        int statusCode = urlConnection.getResponseCode();
        /* 200 represents HTTP OK */
        if (statusCode == 200) {
            Log.d(TAG, "Recvd Data");
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            String response = convertInputStreamToString(inputStream);
            String[] results = parseResult(response);
            return results;
        } else {
            Log.d(TAG, "Error Receiving Data");
            throw new DownloadException("Failed to fetch data!!");
        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
            /* Close Stream */
        if (null != inputStream) {
            inputStream.close();
        }
        return result;
    }

    private String[] parseResult(String result) {

        String[] blogTitles = null;
        try {
            JSONObject response = new JSONObject(result);
            JSONArray posts = response.optJSONArray("posts");
            blogTitles = new String[posts.length()];

            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.optJSONObject(i);
                String title = post.optString("title");
                blogTitles[i] = title;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return blogTitles;
    }

    public class DownloadException extends Exception {
        public DownloadException(String message) {
            super(message);
        }
        public DownloadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}