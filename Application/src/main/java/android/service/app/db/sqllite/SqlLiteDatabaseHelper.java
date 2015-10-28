package android.service.app.db.sqllite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.service.app.db.data.GenericDataApi;
import android.service.app.db.data.GenericDataInsertApi;
import android.service.app.db.data.impl.Data;
import android.service.app.db.data.GenericData;
import android.service.app.db.GenericDatabase;
import android.service.app.db.data.GenericGps;
import android.service.app.db.data.GenericMessage;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.stub.DataStub;
import android.service.app.db.data.GenericSync;
import android.service.app.db.data.impl.Sync;
import android.service.app.db.data.GenericAccount;
import android.service.app.utils.Log;

import java.util.LinkedHashSet;
import java.util.Set;

public class SqlLiteDatabaseHelper extends SQLiteOpenHelper implements GenericDatabase
{
    public static final Data EMPTY = new DataStub.EmptyData();
    public static final GenericDataApi<GenericAccount> ACCOUNT = new DataStub.AccountStub();
    public static final GenericDataApi<GenericDevice> DEVICE = new DataStub.DeviceStub();
    public static final GenericDataApi<GenericMessage> MESSAGE = new DataStub.MessageStub();
    public static final GenericDataApi<GenericGps> GPS = new DataStub.GpsStub();
    public static final GenericDataApi<GenericSync> SYNC = new DataStub.SyncStub();
    private SqlLiteDatabase database;

    protected SqlLiteDatabaseHelper(Context context, SqlLiteDatabase database)
    {
        super(context, database.databaseName, null, database.databaseVersion);
        this.database = database;
    }

    private <T extends GenericData> int checkTypeAndThrowException(T data)
    {
        throw new IllegalStateException("SqlLite API not applicable for this instance, " + data + "; class=" + data.getClass().getSimpleName());
    }

    public <T extends GenericData> int insert(T data)
    {
        if (data instanceof SqlLiteApi)
        {
            return wrapForWrite((GenericDataApi<T>) data).insert();
        }
        return checkTypeAndThrowException(data);
    }

    public <T extends GenericData> int insert(Set<T> dataSet)
    {
        Set<GenericDataApi<T>> dataApiSet = new LinkedHashSet<>();
        for (T data: dataSet)
        {
            if (data instanceof SqlLiteApi) dataApiSet.add((GenericDataApi<T>) data);
            else
                checkTypeAndThrowException(data);
        }

        if (!dataApiSet.isEmpty())
            return wrapForWrite(dataApiSet.iterator().next()).insert();
        else
            return GenericDatabase.EMPTY_DATA;
    }

    public <A extends GenericData, T extends GenericDataApi<A>> T wrapForRead(T data)
    {
        if (data instanceof SqlLiteApi) ((SqlLiteApi)data).setReadableDatabase(getReadableDatabase());
        return data;
    }

    protected <A extends GenericData, T extends GenericDataApi<A>> T wrapForWrite(T data)
    {
        wrapForRead(data);
        if (data instanceof SqlLiteApi) ((SqlLiteApi)data).setWritableDatabase(getWritableDatabase());
        return data;
    }

    @Override
    public GenericDataApi<GenericDevice> devices()
    {
        return wrapForWrite(DEVICE);
    }

    @Override
    public GenericDataApi<GenericAccount> accounts()
    {
        return wrapForWrite(ACCOUNT);
    }

    @Override
    public GenericDataApi<GenericMessage> messages()
    {
        return wrapForWrite(MESSAGE);
    }

    @Override
    public GenericDataApi<GenericSync> points()
    {
        return wrapForWrite(SYNC);
    }

    @Override
    public GenericDataApi<GenericGps> coordinates()
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
        }
        finally
        {
            database.endTransaction();
        }

        if (Log.isInfoEnabled()) Log.info("points=" + points().getAll());
    }

    private void updateSync(GenericSync sync, GenericSync newSync)
    {
        ((Sync) points()).update(getWritableDatabase(), sync, newSync);
    }

    public GenericSync getSyncByTableName(String tableName)
    {
        return points().filterBy(Sync.TABLE, tableName);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        for (Data data : database.tableSet)
            db.execSQL(data.generateCreateTableScript());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (Log.isInfoEnabled()) Log.info("oldVersion=" + oldVersion);
        if (Log.isInfoEnabled()) Log.info("newVersion=" + newVersion);

        for (Data data : SqlLiteDatabase.getDatabaseByNameVersion(database.databaseName, oldVersion).tableSet)
            db.execSQL(data.generateDropTableScript());

        database = SqlLiteDatabase.getDatabaseByNameVersion(database.databaseName, newVersion);
        onCreate(db);
    }
}
