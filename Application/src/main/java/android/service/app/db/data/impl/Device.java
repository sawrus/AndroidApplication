package android.service.app.db.data.impl;

import android.database.Cursor;
import android.service.app.db.GenericDatabase;
import android.service.app.db.data.GenericAccount;
import android.service.app.db.data.GenericDataApi;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.sqllite.SqlLiteApi;
import android.service.app.db.sqllite.impl.SqlLiteDatabaseHelper;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Device extends Data<GenericDevice> implements GenericDevice
{
    private String name = "";
    private int account_id = GenericDatabase.EMPTY_DATA;

    public static final String table_name = "devices";
    public static final String NAME = "name";
    public static final String ACCOUNT_ID = "account_id";

    private static final Map<String, String> fields = Collections.unmodifiableMap(new LinkedHashMap<String, String>(){
        {put(NAME, TEXT);}
        {put(ACCOUNT_ID, INTEGER);}
    });

    @Override
    public Set<String> getFields()
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
    public GenericDevice getDataFromCursor(Cursor cursor)
    {
        GenericDevice data = new Device();
        data.setName(cursor.getString(cursor.getColumnIndex(NAME)));
        data.setAccountId(cursor.getInt(cursor.getColumnIndex(ACCOUNT_ID)));
        fillGenericByCursor(data, cursor);
        return data;
    }

    @Override
    public GenericDevice emptyData()
    {
        return new Device();
    }

    public Device()
    {
        super();
    }

    public Device(String name, int account_id)
    {
        this.name = name;
        this.account_id = account_id;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public void setAccountId(int account_id)
    {
        this.account_id = account_id;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getAccountId()
    {
        return account_id;
    }

    //todo: Need to refactor
    @Override
    public GenericAccount getAccount()
    {
        GenericDataApi<GenericAccount> account = SqlLiteDatabaseHelper.ACCOUNT;
        ((SqlLiteApi<GenericAccount>)account).setReadableDatabase(getReadableDatabase());
        return account.getFirst();
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
