package android.service.app.db.inventory;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.service.app.db.Data;
import android.service.app.db.user.Account;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Device extends Data
{
    private int id = -1;
    private String name = "";
    private int account_id = -2;

    private static final String table_name = "device";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String ACCOUNT_ID = "account_id";

    private static final Map<String, String> fields = new LinkedHashMap<String, String>(){
        {put(ID, INTEGER_PRIMARY_KEY);}
        {put(NAME, TEXT);}
        {put(ACCOUNT_ID, INTEGER);}
    };

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
    public Object insert(SQLiteDatabase database)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAME, getName());
        contentValues.put(ACCOUNT_ID, getAccountId());
        return insert(database, table_name, contentValues);
    }


    public Map<String, Object> getData()
    {
        return new LinkedHashMap<String, Object>()
        {
            {put(NAME, getName());}
            {put(ACCOUNT_ID, getAccountId());}
        };
    }

    public Account getAccount(SQLiteDatabase database)
    {
        return new Account().selectAccount(database, ID, getAccountId());
    }

    public Device selectDevice(SQLiteDatabase database, String fieldFilter, Object valueFilter)
    {
        return fillDeviceFromCursor(select(database, fieldFilter, valueFilter));
    }

    public Device selectFirstDevice(SQLiteDatabase database)
    {
        return fillDeviceFromCursor(selectAll(database));
    }

    @NonNull
    private Device fillDeviceFromCursor(Cursor cursor)
    {
        int idIndex = cursor.getColumnIndex(ID);
        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();

            Device device = new Device();
            device.setId(cursor.getInt(idIndex));
            device.setName(cursor.getString(cursor.getColumnIndex(NAME)));
            device.setAccountId(cursor.getInt(cursor.getColumnIndex(ACCOUNT_ID)));
            return device;
        }

        return new Device();
    }

    public Device(){}

    public Device(String name, int account_id)
    {
        this.name = name;
        this.account_id = account_id;
    }

    public Device(int id, String name, int account_id)
    {
        this.id = id;
        this.name = name;
        this.account_id = account_id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setAccountId(int account_id)
    {
        this.account_id = account_id;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public int getAccountId()
    {
        return account_id;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        return getId() == device.getId() && account_id == device.account_id && getName().equals(device.getName());

    }

    @Override
    public int hashCode()
    {
        int result = getId();
        result = 31 * result + getName().hashCode();
        result = 31 * result + account_id;
        return result;
    }

    @Override
    public String toString()
    {
        return "Device{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", account_id=" + account_id +
                '}';
    }
}
