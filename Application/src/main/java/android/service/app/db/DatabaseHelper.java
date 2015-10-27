package android.service.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.service.app.db.data.GenericGps;
import android.service.app.db.data.GenericMessage;
import android.service.app.db.inventory.GenericDevice;
import android.service.app.db.stub.DataStub;
import android.service.app.db.sync.GenericSync;
import android.service.app.db.sync.impl.Sync;
import android.service.app.db.user.GenericAccount;
import android.service.app.utils.Log;

import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper implements GenericDatabase
{
    public static final Data EMPTY = new DataStub.EmptyData();
    public static final GenericAccount ACCOUNT = new DataStub.AccountStub();
    public static final GenericDevice DEVICE = new DataStub.DeviceStub();
    public static final GenericMessage MESSAGE = new DataStub.MessageStub();
    public static final GenericGps GPS = new DataStub.GpsStub();
    public static final GenericSync SYNC = new DataStub.SyncStub();
    private Database database;

    protected DatabaseHelper(Context context, Database database)
    {
        super(context, database.databaseName, null, database.databaseVersion);
        this.database = database;
    }

    public <T extends GenericData> int insert(T data)
    {
        return wrapForWrite(data).insert();
    }

    public <T extends GenericData> int insert(Set<T> data)
    {
        if (data != null && !data.isEmpty())
            return wrapForWrite(data.iterator().next()).insert();
        else
            return GenericDatabase.EMPTY_DATA;
    }

    public <T extends GenericData> T wrapForRead(T data)
    {
        data.setReadableDatabase(getReadableDatabase());
       return data;
    }

    private <T extends GenericData> T wrapForWrite(T data)
    {
        wrapForRead(data);
        data.setWritableDatabase(getWritableDatabase());
        return data;
    }

    @Override
    public GenericDevice devices()
    {
        return wrapForWrite(DEVICE);
    }

    private GenericAccount accounts()
    {
        return wrapForWrite(ACCOUNT);
    }

    @Override
    public GenericAccount account()
    {
        return accounts().getFirst();
    }

    @Override
    public GenericMessage messages()
    {
        return wrapForWrite(MESSAGE);
    }

    @Override
    public GenericSync sync_points()
    {
        return wrapForWrite(SYNC);
    }

    @Override
    public GenericGps coordinates()
    {
        return wrapForWrite(GPS);
    }

    @Override
    public void updateOrInsertSyncIfNeeded(GenericSync newSync)
    {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();

        try
        {
            GenericSync sync = getSyncByTableName(newSync.getTable());
            if (sync.isEmpty())
                insert(newSync);
            else
                updateSync(sync, newSync);

            database.setTransactionSuccessful();
        } finally
        {
            database.endTransaction();
        }

        if (Log.isInfoEnabled()) Log.info("sync_points=" + sync_points().getAll());
    }

    private void updateSync(GenericSync sync, GenericSync newSync)
    {
        sync_points().update(getWritableDatabase(), sync, newSync);
    }

    public GenericSync getSyncByTableName(String tableName)
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
