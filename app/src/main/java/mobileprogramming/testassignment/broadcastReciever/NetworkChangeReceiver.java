package mobileprogramming.testassignment.broadcastReciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.greenrobot.eventbus.EventBus;

import mobileprogramming.testassignment.event.NetworkChangeEvent;

/**
 * Reciever to get network change updates
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            NetworkChangeEvent networkChangeEvent = new NetworkChangeEvent();
            networkChangeEvent.setNetworkType(activeNetwork.getTypeName());
            networkChangeEvent.setNetworkName(activeNetwork.getExtraInfo());
            EventBus.getDefault().post(networkChangeEvent);
        }
    }
}