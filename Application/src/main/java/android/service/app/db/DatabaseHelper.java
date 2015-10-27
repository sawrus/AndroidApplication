package android.service.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.service.app.db.data.Gps;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.db.sync.Sync;
import android.service.app.db.user.Account;
import android.service.app.utils.Log;

public class DatabaseHelper extends SQLiteOpenHelper
{
    public static final Device DEVICE = new Device();
    public static final Message MESSAGE = new Message();
    public static final Account ACCOUNT = new Account();
    public static final Sync SYNC = new Sync();
    public static final Gps GPS = new Gps();
    private Database database;

    protected DatabaseHelper(Context context, Database database)
    {
        super(context, database.databaseName, null, database.databaseVersion);
        this.database = database;
    }

    public <T extends Data> int addData(T data)
    {
        if (Log.isDebugEnabled()) Log.debug("insert:data=" + data);
        return Integer.valueOf(String.valueOf(data.insert(getWritableDatabase())));
    }

    public <T extends Data> T wrapForRead(T data)
    {
       return (T) data.setReadableDatabase(getReadableDatabase());
    }

    private  <T extends Data> T wrapForWrite(T data)
    {
        wrapForRead(data);
        data.setWritableDatabase(getWritableDatabase());
        return data;
    }

    public Device devices()
    {
        return wrapForWrite(DEVICE);
    }

    public Device device()
    {
        return devices().getFirst();
    }

    private Account accounts()
    {
        return wrapForWrite(ACCOUNT);
    }

    public Account account()
    {
        return accounts().getFirst();
    }

    public Message messages()
    {
        return wrapForWrite(MESSAGE);
    }

    public Sync sync_points()
    {
        return wrapForWrite(SYNC);
    }

    public Gps coordinates()
    {
        return wrapForWrite(GPS);
    }

    public void updateOrInsertSyncIfNeeded(Sync newSync)
    {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();

        try
        {
            Sync sync = getSyncByTableName(newSync.getTable());
            if (sync.isEmpty())
                addData(newSync);
            else
                updateSync(sync, newSync);

            database.setTransactionSuccessful();
        } finally
        {
            database.endTransaction();
        }

        if (Log.isInfoEnabled()) Log.info("sync_points=" + sync_points().getAll());
    }

    private void updateSync(Sync sync, Sync newSync)
    {
        sync_points().update(getWritableDatabase(), sync, newSync);
    }

    public Sync getSyncByTableName(String tableName)
    {
        return sync_points().filterBy(Sync.TABLE, tableName);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        for (Data data: database.tableSet)
            db.execSQL(data.generateCreateTableScript());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (Log.isInfoEnabled()) Log.info("oldVersion=" + oldVersion);
        if (Log.isInfoEnabled()) Log.info("newVersion=" + newVersion);

        for (Data data: Database.getDatabaseByNameVersion(database.databaseName, oldVersion).tableSet)
            db.execSQL(data.generateDropTableScript());

        database = Database.getDatabaseByNameVersion(database.databaseName, newVersion);
        onCreate(db);
    }
}
