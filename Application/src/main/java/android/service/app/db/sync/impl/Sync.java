package android.service.app.db.sync.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.service.app.db.Data;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.sync.GenericSync;
import android.service.app.utils.Log;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Sync extends Data<GenericSync> implements GenericSync
{
    private int account_id = -3;
    private int sync_id = -2;
    private String table = "";

    private static final String table_name = "sync";
    public static final String SYNC_ID = "sync_id";
    public static final String ACCOUNT_ID = "account_id";
    public static final String TABLE = "table_name";
    private static final Map<String, String> fields = Collections.unmodifiableMap(new LinkedHashMap<String, String>()
    {
        {
            put(ID, INTEGER_PRIMARY_KEY);
        }

        {
            put(SYNC_ID, INTEGER);
        }

        {
            put(ACCOUNT_ID, INTEGER);
        }

        {
            put(TABLE, TEXT);
        }
    });

    public Sync()
    {
        super();
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
    protected Sync emptyData()
    {
        return new Sync();
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
    public Map<String, Object> getData()
    {
        final Map<String, Object> data = super.getData();
        return new LinkedHashMap<String, Object>()
        {
            {put(SYNC_ID, getSyncId());}
            {put(ACCOUNT_ID, getAccountId());}
            {put(TABLE, getTable());}
            {putAll(data);}
        };
    }

    @Override
    public void update(SQLiteDatabase database, GenericSync oldSync, GenericSync newSync)
    {
        if (Log.isDebugEnabled()) Log.debug("update:oldSync=" + oldSync);
        if (Log.isDebugEnabled()) Log.debug("update:newSync=" + newSync);
        if (oldSync != null && newSync != null && !oldSync.isEmpty())
        {
            ContentValues data = new ContentValues();
            data.put(Sync.SYNC_ID, newSync.getSyncId());
            data.put(Sync.TABLE, newSync.getTable());
            database.update(getTableName(), data, Sync.ID + "=" + oldSync.getId(), null);

            String updateScript = "UPDATE " + getTableName() + " SET " + Sync.SYNC_ID + " = " + newSync.getSyncId() + " WHERE " + Sync.ID + " = " + oldSync.getId();
            if (Log.isDebugEnabled()) Log.debug("updateScript=" + updateScript);
        }
    }

    protected GenericSync getDataFromCursor(Cursor cursor)
    {
        GenericSync data = new Sync();
        data.setAccountId(cursor.getInt(cursor.getColumnIndex(ACCOUNT_ID)));
        data.setSyncId(cursor.getInt(cursor.getColumnIndex(SYNC_ID)));
        data.setTable(cursor.getString(cursor.getColumnIndex(TABLE)));
        fillGenericByCursor(data, cursor);
        return data;
    }

    @Override
    public String toString()
    {
        return "Sync{" +
                "account_id=" + account_id +
                ", sync_id=" + sync_id +
                ", table='" + table + '\'' +
                '}' + " - " + super.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Sync sync = (Sync) o;

        if (account_id != sync.account_id) return false;
        if (sync_id != sync.sync_id) return false;
        return getTable().equals(sync.getTable());

    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + account_id;
        result = 31 * result + sync_id;
        result = 31 * result + getTable().hashCode();
        return result;
    }
}
