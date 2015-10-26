package android.service.app;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.app.db.Database;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.db.sync.Sync;
import android.service.app.db.user.Account;
import android.service.app.rest.ImportDataTask;
import android.service.app.rest.SyncOutput;
import android.service.app.rest.ExportDataTask;
import android.service.app.utils.Android;
import android.service.app.utils.Log;
import android.support.annotation.NonNull;
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

        fillDevice();
    }

    @NonNull
    private static DatabaseHelper getAndroidDatabase(Context context)
    {
        return new DatabaseHelper(context, Database.ANDROID_V_1_5);
    }

    private void fillDevice()
    {
        DatabaseHelper androidDatabase = getAndroidDatabase(getApplicationContext());
        device = androidDatabase.device();
        Log.v("device=" + device);
        androidDatabase.close();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.v("call onStartCommand");

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

    public static void initDatabase(Context context)
    {
        try
        {
            //context.deleteDatabase(Database.ANDROID_V_1_5.databaseName);
            initLocalDatabase(context);
        } catch (Exception e)
        {
            Log.v("e: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runSync(Context context)
    {
        // sync for object functionality
        new ExportDataTask<>(getAndroidDatabase(context), context, SyncOutput.getStringCallbackHandler()).execute();

        // sync for subobject functionality
        new ImportDataTask<>(getAndroidDatabase(context), context, SyncOutput.getStringCallbackHandler()).execute();
    }

    private static void initLocalDatabase(Context context)
    {
        DatabaseHelper androidDatabase = getAndroidDatabase(context);

        if (androidDatabase.device().isEmpty())
        {
            SQLiteDatabase database = androidDatabase.getWritableDatabase();
            database.beginTransaction();
            try
            {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
                String number = tm.getLine1Number();
                //todo: need to parse this parameters from settings
                int accountId = androidDatabase.addData(new Account(number + "_sawrus@gmail.com"));

                Device device = new Device(Android.getDeviceName(), accountId);
                int deviceId = androidDatabase.addData(device);

                Sync sync = new Sync(accountId, deviceId, device.getTableName());
                androidDatabase.updateOrInsertSyncIfNeeded(sync);
                //todo: need to parse this parameters from settings

                database.setTransactionSuccessful();
            } finally
            {
                database.endTransaction();
            }
        }

        Log.v("actualMessages=" + androidDatabase.messages().getActualBySync());
        androidDatabase.close();
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
                printDataOnScreen(address + ": " + body);
                saveMessageInDatabase(address, incoming, body);
            }
        }).start();
    }

    private void saveMessageInDatabase(String address, boolean incoming, String body)
    {
        DatabaseHelper androidDatabase = getAndroidDatabase(getApplicationContext());

        SQLiteDatabase database = androidDatabase.getWritableDatabase();
        database.beginTransaction();
        try
        {
            androidDatabase.addData(new Message(address, incoming, body, device.getId()));
            database.setTransactionSuccessful();
        } finally
        {
            database.endTransaction();
        }

        androidDatabase.close();
    }

    public void printDataOnScreen(String data)
    {
        Log.v(data);
        Intent i = new Intent("EVENT_UPDATED");
        i.putExtra("<Key>", data);
        sendBroadcast(i);
    }
}
