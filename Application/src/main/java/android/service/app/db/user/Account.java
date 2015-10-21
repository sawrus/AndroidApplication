package android.service.app.db.user;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.service.app.db.Data;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Account extends Data
{
    private int id = -1;
    private String email = "";

    private static final String table_name = "account";
    public static final String ID = "id";
    public static final String EMAIL = "email";
    private static final Map<String, String> fields = new LinkedHashMap<String, String>(){
        {put(ID, INTEGER_PRIMARY_KEY);}
        {put(EMAIL, TEXT);}
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
        return new LinkedHashMap<String, Object>()
        {
            {put(EMAIL, getEmail());}
        };
    }

    public Account selectAccount(SQLiteDatabase database, String fieldFilter, Object valueFilter)
    {
        return fillAccountFromCursor(select(database, fieldFilter, valueFilter));
    }

    public Account selectFirstAccount(SQLiteDatabase database)
    {
        return fillAccountFromCursor(selectAll(database));
    }

    @NonNull
    private Account fillAccountFromCursor(Cursor cursor)
    {
        int idIndex = cursor.getColumnIndex(ID);
        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();

            Account account = new Account();
            account.setId(cursor.getInt(idIndex));
            account.setEmail(cursor.getString(cursor.getColumnIndex(EMAIL)));
            return account;
        }

        return new Account();
    }

    public Account(){}

    public Account(String email)
    {
        this.email = email;
    }

    public Account(int id, String email)
    {
        this.id = id;
        this.email = email;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public int getId()
    {
        return id;
    }

    public String getEmail()
    {
        return email;
    }

    @Override
    public String toString()
    {
        return "Account{" +
                "id=" + id +
                ", email='" + email + '\'' +
                '}';
    }
}
