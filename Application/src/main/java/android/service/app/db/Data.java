package android.service.app.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.service.app.utils.Log;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class Data
{
    protected static final String INTEGER = "INTEGER";
    protected static final String DOUBLE = "DOUBLE";
    protected static final String INTEGER_PRIMARY_KEY = "INTEGER PRIMARY KEY";
    protected static final String INTEGER_PRIMARY_KEY_AUTOINCREMENT = "INTEGER PRIMARY KEY AUTOINCREMENT";

    protected static final String TEXT = "TEXT";
    protected static final String DATETIME = "DATETIME";

    public Data()
    {
    }

    protected static String generateCreateTableScript(String tableName, Map<String, String> fields)
    {
        String script = "CREATE TABLE " + tableName + " (";
        for (Map.Entry<String, String> filed : fields.entrySet())
            script += filed.getKey() + " " + filed.getValue() + ",";
        return script.substring(0, script.lastIndexOf(",")) + ")";
    }

    protected String getDateTime()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public abstract String generateCreateTableScript();

    protected static String generateDropTableScript(String tableName)
    {
        return "DROP TABLE " + tableName;
    }

    public abstract String generateDropTableScript();

    protected static Object insert(SQLiteDatabase database, String tableName, ContentValues values)
    {
        return database.insert(tableName, null, values);
    }

    public Object insert(SQLiteDatabase database)
    {
        ContentValues contentValues = getContentValues();
        return insert(database, getTableName(), contentValues);
    }

    @NonNull
    public ContentValues getContentValues()
    {
        ContentValues contentValues = new ContentValues();
        for (Map.Entry<String, Object> entry: getData().entrySet())
        {
            Object value = entry.getValue();
            if (value instanceof String) contentValues.put(entry.getKey(), ((String) value));
            else if (value instanceof Integer) contentValues.put(entry.getKey(), ((Integer) value));
        }
        return contentValues;
    }

    protected Cursor select(SQLiteDatabase database, String fieldFilter, String valueFilter)
    {
        String script = "SELECT ";
        Set<String> fields = getFields();

        for (String field : fields)
            script += field + ", ";

        script = script.substring(0, script.lastIndexOf(",")) + " FROM " + getTableName() + " WHERE " + fieldFilter + " = " + valueFilter;
        Log.v("script=" + script);
        return database.rawQuery(script, null);
    }


    protected Cursor select(SQLiteDatabase database, String fieldFilter, Object valueFilter)
    {
        String script = "SELECT ";
        Set<String> fields = getFields();

        for (String field : fields)
            script += field + ", ";

        if (valueFilter instanceof String) valueFilter = "\'" + valueFilter + "\'";

        script = script.substring(0, script.lastIndexOf(",")) + " FROM " + getTableName() + " WHERE " + fieldFilter + " = " + valueFilter;
        Log.v("script=" + script);
        return database.rawQuery(script, null);
    }

    protected Cursor selectAll(SQLiteDatabase database)
    {
        String script = "SELECT ";
        Set<String> fields = getFields();

        for (String field : fields)
            script += field + ", ";

        script = script.substring(0, script.lastIndexOf(",")) + " FROM " + getTableName();
        Log.v("script=" + script);
        return database.rawQuery(script, null);
    }

    protected Object delete(SQLiteDatabase database, String fieldFilter, String valueFilter)
    {
        Log.v("delete from " + getTableName() + " where " + fieldFilter + " = " + valueFilter);
        return database.delete(getTableName(), fieldFilter, new String[]{valueFilter});
    }

    protected abstract Set<String> getFields();

    public abstract String getTableName();

    public boolean isEmpty()
    {
        return -1 == getId();
    }

    public abstract int getId();

    public abstract Map<String, Object> getData();

    public native String a();
    public native String b();
    public native String c();
    public native String d();
    public native String e();
    public native String f();
    public native String g();
    public native String h();
    public native String i();
    public native String k();

    static {
        System.loadLibrary("Application");
    }

}
