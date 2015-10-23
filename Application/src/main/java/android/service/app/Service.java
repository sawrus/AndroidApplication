package android.service.app;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.app.db.Database;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.db.sync.Sync;
import android.service.app.db.user.Account;
import android.service.app.rest.CallbackHandler;
import android.service.app.rest.DatabaseSyncOutput;
import android.service.app.rest.RestSync;
import android.service.app.utils.Log;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Service extends android.app.Service
{
    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    public static final String OK = "ok ";
    public static final String SMS = "SMS";

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

        Log.v("a" + DatabaseHelper.ACCOUNT.a());
    }

    private void fillDevice()
    {
        DatabaseHelper androidDatabase = getAndroidDatabase(getApplicationContext());
        device = androidDatabase.selectFirstDevice();
        Log.v("device=" + device);
        androidDatabase.close();
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

            DatabaseHelper localDatabase = getAndroidDatabase(context);
            CallbackHandler<DatabaseSyncOutput> handler = getStringCallbackHandler();
            RestSync<String> syncTask = new RestSync<>(localDatabase, context, handler);
            syncTask.execute();
        } catch (Exception e)
        {
            Log.v("e: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @NonNull
    private static CallbackHandler<DatabaseSyncOutput> getStringCallbackHandler()
    {
        return new CallbackHandler<DatabaseSyncOutput>()
        {
            private DatabaseSyncOutput result;

            @Override
            public void handle(DatabaseSyncOutput result)
            {
                this.result = result;
                Log.v("result=" + result);
            }

            @Override
            public String toString()
            {
                return String.valueOf(result);
            }
        };
    }

    public static String getDeviceName()
    {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer))
        {
            return capitalize(model);
        } else
        {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s)
    {
        if (s == null || s.length() == 0)
        {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first))
        {
            return s;
        } else
        {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    @NonNull
    private static void initLocalDatabase(Context context)
    {
        DatabaseHelper androidDatabase = getAndroidDatabase(context);

        String deviceName = getDeviceName();
        Log.v("deviceName=" + deviceName);

        Device firstDevice = androidDatabase.selectFirstDevice();
        Log.v("firstDevice=" + firstDevice);
        Log.v("messages=" + androidDatabase.getMessages());
        if (firstDevice.isEmpty())
        {
            SQLiteDatabase database = androidDatabase.getWritableDatabase();
            database.beginTransaction();
            try
            {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
                String number = tm.getLine1Number();
                Log.v("number=" + number);

                //todo: need to parse this parameters from settings
                String email = number + "_sawrus@gmail.com";
                int accountId = androidDatabase.addAccount(new Account(email));
                Log.v("accountId=" + accountId);

                Device device = new Device(deviceName, accountId);
                int deviceId = androidDatabase.addDevice(device);
                Log.v("deviceId=" + deviceId);

                Sync sync = new Sync(accountId, deviceId, device.getTableName());
                androidDatabase.updateOrInsertSyncIfNeeded(sync);
                //todo: need to parse this parameters from settings

                database.setTransactionSuccessful();
            } finally
            {
                database.endTransaction();
            }
        } else
        {
            Sync sync = new Sync(firstDevice.getAccountId(), firstDevice.getId(), firstDevice.getTableName());
            androidDatabase.updateOrInsertSyncIfNeeded(sync);
        }

        androidDatabase.close();
    }

    @NonNull
    private static DatabaseHelper getAndroidDatabase(Context context)
    {
        return new DatabaseHelper(context, Database.ANDROID_V_1_5);
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

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return remoteServiceStub;
    }

    public void runSmsEvent(final String address, final String body, final boolean incoming, ContentResolver contentResolver)
    {
        final ContentResolver resolver = contentResolver == null ? getApplicationContext().getContentResolver() : contentResolver;
        Log.v("resolver=" + resolver + "; incoming=" + incoming);

        new Thread(new Runnable()
        {
            public void run()
            {
                String message = address + ": " + body;
                printMessageOnScreen(message);

                try
                {
                    if (incoming)
                        checkAndSendSmsIfNeeded();
                    else
                        removeOldSms(resolver);
                } catch (Exception e)
                {
                    printMessageOnScreen(e.getMessage());
                    e.printStackTrace();
                }

                saveMessageInDatabase(address, incoming, body);
            }
        }).start();
    }

    public void checkAndSendSmsIfNeeded()
    {
        ContentResolver resolver = getApplicationContext().getContentResolver();
        Cursor cursor = resolver.query(SmsObserver.SMS_URI, SmsObserver.columns, null, null, null);
        if (cursor == null) return;
        cursor.moveToFirst();

        String body = cursor.getString(SmsObserver.BODY);
        String[] split = body.split(" ");
        Pair<Integer, Integer> pair = getPair(cursor);
        if (pair == null) return;
        int passwordResult = pair.first;
        int phoneNumberResult = pair.second;

        if (passwordResult > 0 && phoneNumberResult > 0)
        {
            SmsManager sms = SmsManager.getDefault();
            String destinationAddress = split[phoneNumberResult];
            String password = split[passwordResult];
            String message = OK + password;
            printMessageOnScreen(destinationAddress + ": " + message);
            removeMessageById(resolver, cursor.getInt(SmsObserver.ID));
            sms.sendTextMessage(destinationAddress, null, message, null, null);
        }

        cursor.close();
    }


    public void removeOldSms(final ContentResolver resolver)
    {
        Cursor cursor = resolver.query(SmsObserver.SMS_URI, SmsObserver.columns, null, null, null);
        Log.v("cursor=" + cursor);
        if (cursor == null) return;

        final List<Integer> messagesForDelete = new ArrayList<>();

        cursor.moveToFirst();
        while (!cursor.isClosed())
        {
            String body = String.valueOf(cursor.getString(SmsObserver.BODY));
            Log.v("body=" + body);
            String[] split = body.split(" ");

            Pair<Integer, Integer> pair = getPair(cursor);
            if (pair == null) continue;

            int passwordResult = pair.first;
            int phoneNumberResult = pair.second;

            boolean isOkSms = split.length == 2 && body.toLowerCase().contains(OK.toLowerCase());
            boolean isSendSms = body.toLowerCase().contains(SMS.toLowerCase()) && (passwordResult > 0 || phoneNumberResult > 0);
            if (isOkSms || isSendSms)
                messagesForDelete.add(cursor.getInt(SmsObserver.ID));

            if (cursor.isLast()) break;
            boolean moveToNext = cursor.moveToNext();
            Log.v("moveToNext=" + moveToNext);
        }

        cursor.close();

        for (Integer messageId : messagesForDelete)
            removeMessageById(resolver, messageId);
    }

    private void deleteMessageById(ContentResolver resolver, Cursor cursor)
    {
        removeMessageById(resolver, cursor.getInt(SmsObserver.ID));
    }

    private void removeMessageById(ContentResolver resolver, int messageId)
    {
        try
        {
            int result = resolver.delete(Uri.parse(SmsObserver.CONTENT_SMS + messageId), null, null);
            printMessageOnScreen("deleting SMS/id: " + messageId + "; result: " + result);
        } catch (Exception e)
        {
            printMessageOnScreen("deleting SMS/id e: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Pair<Integer, Integer> getPair(Cursor cursor)
    {
        String body = cursor.getString(SmsObserver.BODY);
        String[] split = body.split(" ");

        Log.v("messageParts=" + Arrays.asList(split));

        int passwordIndex = -1;
        int phoneNumberIndex = -1;
        int length = split.length;
        for (int i = 0; i < length; i++)
        {
            final String part = split[i];
            String toLowerCase = String.valueOf(part).toLowerCase();
            if (SmsObserver.passwordPrefixes.contains(toLowerCase))
            {
                passwordIndex = i;
            } else if (SmsObserver.phonePrefixes.contains(toLowerCase))
            {
                phoneNumberIndex = i;
            }
        }
        Log.v("passwordIndex=" + passwordIndex);
        Log.v("phoneNumberIndex=" + phoneNumberIndex);

        int passwordResult = passwordIndex < length ? passwordIndex + 1 : -1;
        int phoneNumberResult = phoneNumberIndex < length ? phoneNumberIndex + 1 : -1;
        Log.v("passwordResult=" + passwordResult);
        Log.v("phoneNumberResult=" + phoneNumberResult);

        return new Pair<>(passwordResult, phoneNumberResult);
    }

    public void printMessageOnScreen(String message)
    {
        Log.v("message=" + message);

        Intent i = new Intent("EVENT_UPDATED");
        i.putExtra("<Key>", message);
        sendBroadcast(i);
    }

    private void saveMessageInDatabase(String address, boolean incoming, String body)
    {
        DatabaseHelper androidDatabase = getAndroidDatabase(getApplicationContext());
        Device device = androidDatabase.selectFirstDevice();
        Log.v("device=" + device);

        SQLiteDatabase database = androidDatabase.getWritableDatabase();
        database.beginTransaction();
        try
        {
            androidDatabase.addMessage(new Message(address, incoming, body, device.getId()));
            database.setTransactionSuccessful();
        } finally
        {
            database.endTransaction();
        }

        Set<Message> localDatabaseMessages = androidDatabase.getMessages();
        Log.v("internal:localDatabaseMessages=" + localDatabaseMessages);

        androidDatabase.close();
    }
}
