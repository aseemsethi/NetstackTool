package in.netstack.netstacktool;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * Created by aseem on 08-10-2016.
 */

public class clientCommonRecv extends ResultReceiver {
    private Receiver mReceiver;
    private static final String TAG = "clientCommonRecv";


    public clientCommonRecv(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        if (receiver != null)
            Log.d("aseemview set mReceiver", receiver.toString());
        else
            Log.d("aseemview set mReceiver", "is NULL");

        mReceiver = receiver;
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            Log.d(TAG, "clientCommonRecv calling Activity!");
            mReceiver.onReceiveResult(resultCode, resultData);
        } else {
            Log.d(TAG, "clientCommonRecv cannot call Activity, recvr is null!");
        }
    }
}
