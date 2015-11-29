package android.service.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

public class OnBootReceiver extends BroadcastReceiver
{
    protected Handler handler = new Handler();

    @Override
    public void onReceive(Context context, Intent intent)
    {
    }
}
