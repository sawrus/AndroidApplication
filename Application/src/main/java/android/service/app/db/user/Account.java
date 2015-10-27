package android.service.app.db.user;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.service.app.db.Data;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.GenericDatabase;
import android.service.app.db.inventory.GenericDevice;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Account extends Data<GenericAccount> implements GenericAccount
{
    private String email = GenericDatabase.EMPTY_EMAIL;

    private static final String table_name = "account";
    public static final String EMAIL = "email";
    private static final Map<String, String> fields = Collections.unmodifiableMap(new LinkedHashMap<String, String>()
    {
        {
            put(EMAIL, TEXT);
        }
    });

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
            {put(EMAIL, getEmail());}
            {putAll(data);}
        };
    }

    @NonNull
    protected GenericAccount getDataFromCursor(Cursor cursor)
    {
        GenericAccount data = new Account();
        data.setEmail(cursor.getString(cursor.getColumnIndex(EMAIL)));
        fillGenericByCursor(data, cursor);
        return data;
    }

    @Override
    protected GenericAccount emptyData()
    {
        return new Account();
    }

    public Account()
    {
        super();
    }

    public Account(String email)
    {
        this.email = email;
    }

    @Override
    public void setEmail(String email)
    {
        this.email = email;
    }

    @Override
    public String getEmail()
    {
        return email;
    }

    @Override
    public String toString()
    {
        return "Account{" +
                "email='" + email + '\'' +
                '}' + " - " + super.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Account account = (Account) o;

        return getEmail().equals(account.getEmail());

    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + getEmail().hashCode();
        return result;
    }
}
