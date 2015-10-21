package android.service.app.db.sync;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.service.app.db.Data;
import android.service.app.utils.Log;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Sync extends Data
{
    private int id = -1;
    private int account_id = -3;
    private int sync_id = -2;
    private String table = "";

    private static final String table_name = "sync";
    public static final String ID = "id";
    public static final String SYNC_ID = "sync_id";
    public static final String ACCOUNT_ID = "account_id";
    public static final String TABLE = "table_name";
    private static final Map<String, String> fields = new LinkedHashMap<String, String>(){
        {put(ID, INTEGER_PRIMARY_KEY);}
        {put(SYNC_ID, INTEGER);}
        {put(ACCOUNT_ID, INTEGER);}
        {put(TABLE, TEXT);}
    };

    public Sync()
    {
    }

    public Sync(int id, int account_id, int sync_id, String table)
    {
        this.id = id;
        this.account_id = account_id;
        this.sync_id = sync_id;
        this.table = table;
    }

    public Sync(int account_id, int sync_id, String table)
    {
        this.account_id = account_id;
        this.sync_id = sync_id;
        this.table = table;
    }

    public int getAccountId()
    {
        return account_id;
    }

    public int getSyncId()
    {
        return sync_id;
    }

    public String getTable()
    {
        return table;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setAccountId(int account_id)
    {
        this.account_id = account_id;
    }

    public void setSyncId(int sync_id)
    {
        this.sync_id = sync_id;
    }

    public void setTable(String table)
    {
        this.table = table;
    }

    @Override
    public String generateCreateTableScript()
    {
        return generateCreateTableScript(table_name, fields);
    }

    @Override
    public String generateDropTableScript()
    {
        return generateDropTableScript(table_name);
    }

    @Override
    protected Set<String> getFields()
    {
        return fields.keySet();
    }

    @Override
    public String getTableName()
    {
        return table_name;
    }

    @Override
    protected int getId()
    {
        return id;
    }

    @Override
    public Map<String, Object> getData()
    {
        return new LinkedHashMap<String, Object>()
        {
            {put(SYNC_ID, getSyncId());}
            {put(ACCOUNT_ID, getAccountId());}
            {put(TABLE, getTable());}
        };
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sync sync = (Sync) o;

        return getId() == sync.getId() && account_id == sync.account_id && sync_id == sync.sync_id && getTable().equals(sync.getTable());
    }

    @Override
    public int hashCode()
    {
        int result = getId();
        result = 31 * result + account_id;
        result = 31 * result + sync_id;
        result = 31 * result + getTable().hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "Sync{" +
                "id=" + id +
                ", account_id=" + account_id +
                ", sync_id=" + sync_id +
                ", table='" + table + '\'' +
                '}';
    }

    public Set<Sync> selectAllSync(SQLiteDatabase database)
    {
        Cursor cursor = selectAll(database);
        Set<Sync> syncs = new LinkedHashSet<>();

        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();

            do
            {
                Sync sync = new Sync();
                sync.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                sync.setAccountId(cursor.getInt(cursor.getColumnIndex(ACCOUNT_ID)));
                sync.setSyncId(cursor.getInt(cursor.getColumnIndex(SYNC_ID)));
                sync.setTable(cursor.getString(cursor.getColumnIndex(TABLE)));

                syncs.add(sync);
                Log.v("sync=" + sync + "; cursor=" + cursor);
                if (!cursor.isLast()) cursor.moveToNext();
                else break;
            } while (!cursor.isClosed());
        }

        return syncs;
    }

    public void update(SQLiteDatabase database, Sync oldSync, Sync newSync)
    {
        Log.v("update:oldSync=" + oldSync);
        Log.v("update:newSync=" + newSync);
        if (oldSync != null && newSync != null && !oldSync.isEmpty())
        {
            ContentValues data = new ContentValues();
            data.put(Sync.SYNC_ID, newSync.getSyncId());
            data.put(Sync.TABLE, newSync.getTable());
            database.update(getTableName(), data, Sync.ID + "=" + oldSync.getId(), null);

            String updateScript = "UPDATE " + getTableName() + " SET " + Sync.SYNC_ID + " = " + newSync.getSyncId() + " WHERE " + Sync.ID + " = " + oldSync.getId();
            Log.v("updateScript=" + updateScript);
            //database.execSQL(updateScript);
        }
    }

    public Sync selectSync(SQLiteDatabase database, String fieldFilter, Object valueFilter)
    {
        return fillSyncFromCursor(select(database, fieldFilter, valueFilter));
    }

    private Sync fillSyncFromCursor(Cursor cursor)
    {
        int idIndex = cursor.getColumnIndex(ID);
        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();

            Sync sync = new Sync();
            sync.setId(cursor.getInt(idIndex));
            sync.setAccountId(cursor.getInt(cursor.getColumnIndex(ACCOUNT_ID)));
            sync.setSyncId(cursor.getInt(cursor.getColumnIndex(SYNC_ID)));
            sync.setTable(cursor.getString(cursor.getColumnIndex(TABLE)));
            return sync;
        }

        return new Sync();
    }
}
