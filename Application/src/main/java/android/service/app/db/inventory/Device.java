package android.service.app.db.inventory;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.service.app.db.Data;
import android.service.app.db.DatabaseHelper;
import android.support.annotation.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Device extends Data<Device>
{
    private String name = "";
    private int account_id = -2;

    private static final String table_name = "device";
    public static final String NAME = "name";
    public static final String ACCOUNT_ID = "account_id";

    private static final Map<String, String> fields = new LinkedHashMap<String, String>(){
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

    public Map<String, Object> getData()
    {
        final Map<String, Object> data = super.getData();
        return new LinkedHashMap<String, Object>()
        {
            {put(NAME, getName());}
            {put(ACCOUNT_ID, getAccountId());}
            {putAll(data);}
        };
    }

    @NonNull
    public Device getDataFromCursor(Cursor cursor)
    {
        Device data = new Device();
        data.setName(cursor.getString(cursor.getColumnIndex(NAME)));
        data.setAccountId(cursor.getInt(cursor.getColumnIndex(ACCOUNT_ID)));
        fillGenericByCursor(data, cursor);
        return data;
    }

    @Override
    protected Device emptyData()
    {
        return DatabaseHelper.DEVICE;
    }

    public Device()
    {
        super();
    }

    public Device(SQLiteDatabase readableDatabase)
    {
        super(readableDatabase);
    }

    public Device(String name, int account_id)
    {
        this.name = name;
        this.account_id = account_id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setAccountId(int account_id)
    {
        this.account_id = account_id;
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
    public String toString()
    {
        return "Device{" +
                "name='" + name + '\'' +
                ", account_id=" + account_id +
                '}' + " - " + super.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Device device = (Device) o;

        if (account_id != device.account_id) return false;
        return getName().equals(device.getName());

    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + account_id;
        return result;
    }
}
