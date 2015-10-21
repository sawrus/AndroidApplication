package android.service.app.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.service.app.db.data.Gps;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.db.sync.Sync;
import android.service.app.db.user.Account;
import android.service.app.utils.Log;

import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper
{
    public static final Device DEVICE = new Device();
    public static final Message MESSAGE = new Message();
    public static final Account ACCOUNT = new Account();
    public static final Sync SYNC = new Sync();
    public static final Gps GPS = new Gps();
    private Database database;

    public DatabaseHelper(Context context, Database database)
    {
        super(context, database.databaseName, null, database.databaseVersion);
        this.database = database;
    }

    public int addAccount(Account data)
    {
        return Integer.valueOf(String.valueOf(data.insert(getWritableDatabase())));
    }

    public int addDevice(Device data)
    {
        return Integer.valueOf(String.valueOf(data.insert(getWritableDatabase())));
    }

    public int addMessage(Message data)
    {
        return Integer.valueOf(String.valueOf(data.insert(getWritableDatabase())));
    }

    public int addSync(Sync data)
    {
        return Integer.valueOf(String.valueOf(data.insert(getWritableDatabase())));
    }

    public int addGps(Gps data)
    {
        return Integer.valueOf(String.valueOf(data.insert(getWritableDatabase())));
    }

    public Device selectFirstDevice()
    {
        return DEVICE.selectFirstDevice(getReadableDatabase());
    }

    public Account selectFirstAccount()
    {
        return ACCOUNT.selectFirstAccount(getReadableDatabase());
    }

    public Set<Message> getMessages()
    {
        return MESSAGE.selectAllMessages(getReadableDatabase());
    }

    public Set<Gps> getGpsSet()
    {
        return GPS.selectAllGps(getReadableDatabase());
    }

    public Set<Sync> getSyncSet()
    {
        return SYNC.selectAllSync(getReadableDatabase());
    }

    public void updateOrInsertSyncIfNeeded(Sync newSync)
    {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();

        try
        {
            Sync sync = selectSyncByTableName(newSync.getTable());
            if (sync.isEmpty())
                addSync(newSync);
            else
                updateSync(sync, newSync);

            database.setTransactionSuccessful();
        } finally
        {
            database.endTransaction();
        }

        Log.v("updateOrInsertSyncIfNeeded:syncSet=" + getSyncSet());
    }

    private void updateSync(Sync sync, Sync newSync)
    {
        SYNC.update(getWritableDatabase(), sync, newSync);
    }

    public Sync selectSyncByTableName(String tableName)
    {
        return SYNC.selectSync(getReadableDatabase(), Sync.TABLE, tableName);
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
        Log.v("oldVersion=" + oldVersion);
        Log.v("newVersion=" + newVersion);

        for (Data data: Database.getDatabaseByNameVersion(database.databaseName, oldVersion).tableSet)
            db.execSQL(data.generateDropTableScript());

        database = Database.getDatabaseByNameVersion(database.databaseName, newVersion);
        onCreate(db);
    }
}
