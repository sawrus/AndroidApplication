package android.service.app.db.sqllite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.service.app.db.GenericDatabase;
import android.service.app.db.data.DeviceDependable;
import android.service.app.db.data.GenericAccount;
import android.service.app.db.data.GenericData;
import android.service.app.db.data.GenericDataApi;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.data.GenericSync;
import android.service.app.db.data.impl.Device;
import android.service.app.db.data.impl.Sync;
import android.service.app.utils.Log;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SqlLiteData<T extends GenericData> implements SqlLiteApi<T>
{
    //SQL Lite general api
    @Override
    public abstract Set<String> getFields();

    @Override
    public abstract String getTableName();

    @Override
    public abstract String generateCreateTableScript();

    @Override
    public abstract String generateDropTableScript();

    @Override
    public abstract T emptyData();

    @Override
    public abstract T getDataFromCursor(Cursor cursor);

    public static final String ID = "id";
    public static final String SYNCID = "syncid";
    public static final String DESCRIPTION = "description";
    public static final String TIMEZONE = "timezone";
    public static final String CREATED_WHEN = "created_when";

    public static final String EQUAL = "=";
    public static final String MORE_THAN = ">";
    public static final String LESS_THAN = "<";
    public static final String NOT_EQUAL = "!=";
    public static final String GMT_TIME_ZONE = "GMT";

    protected static final String INTEGER = "INTEGER";
    protected static final String DOUBLE = "DOUBLE";
    protected static final String INTEGER_PRIMARY_KEY = "INTEGER PRIMARY KEY";
    protected static final String INTEGER_PRIMARY_KEY_AUTOINCREMENT = "INTEGER PRIMARY KEY AUTOINCREMENT";
    protected static final String TEXT = "TEXT";
    protected static final String DATETIME = "DATETIME";

    public static GenericDevice device = new Device();
    private static AtomicBoolean wasInitiatedEarlier = new AtomicBoolean(false);

    private SQLiteDatabase readableDatabase = null;
    private SQLiteDatabase writableDatabase = null;

    private static final Map<String, String> fields = Collections.unmodifiableMap(new LinkedHashMap<String, String>()
    {
        {
            put(ID, INTEGER_PRIMARY_KEY);
        }

        {
            put(DESCRIPTION, TEXT);
        }

        {
            put(TIMEZONE, TEXT);
        }

        {
            put(CREATED_WHEN, TEXT);
        }
    });

    public boolean isWasInitiatedEarlier()
    {
        return wasInitiatedEarlier.get();
    }

    public synchronized void init()
    {
        if (readableDatabase != null && device.isEmpty() && this instanceof DeviceDependable)
        {
            device = initDevices(readableDatabase);
            if (!device.isEmpty() && !isWasInitiatedEarlier()) wasInitiatedEarlier.set(true);
        }
    }

    public SqlLiteData(SQLiteDatabase readableDatabase, SQLiteDatabase writableDatabase)
    {
        this.readableDatabase = readableDatabase;
        this.writableDatabase = writableDatabase;
    }

    public SqlLiteData(SQLiteDatabase readableDatabase)
    {
        this.readableDatabase = readableDatabase;
    }

    public void setReadableDatabase(SQLiteDatabase readableDatabase)
    {
        this.readableDatabase = readableDatabase;
    }

    public void setWritableDatabase(SQLiteDatabase writableDatabase)
    {
        this.writableDatabase = writableDatabase;
    }

    protected SQLiteDatabase getReadableDatabase()
    {
        if (readableDatabase == null) throw new IllegalStateException("stub mode for read, class = " + getClass().getSimpleName());
        else if (!isWasInitiatedEarlier()) init();
        return readableDatabase;
    }

    protected SQLiteDatabase getWritableDatabase()
    {
        if (writableDatabase == null) throw new IllegalStateException("stub mode for write, class = " + getClass().getSimpleName());
        return writableDatabase;
    }

    public boolean withReadableDatabase()
    {
        return readableDatabase != null;
    }

    public boolean withWritableDatabase()
    {
        return writableDatabase != null;
    }

    public SqlLiteData()
    {
        //init();
    }

    protected static String generateCreateTableScript(String tableName, Map<String, String> fields)
    {
        LinkedHashMap<String, String> _fields = new LinkedHashMap<>(fields);
        _fields.putAll(SqlLiteData.fields);
        String script = "CREATE TABLE " + tableName + " (";
        for (Map.Entry<String, String> filed : _fields.entrySet())
            script += filed.getKey() + " " + filed.getValue() + ",";
        return script.substring(0, script.lastIndexOf(",")) + ")";
    }

    protected static String generateDropTableScript(String tableName)
    {
        return "DROP TABLE " + tableName;
    }

    protected static int insert(SQLiteDatabase database, String tableName, ContentValues values)
    {
        return (int) database.insert(tableName, null, values);
    }

    private int insert(SQLiteDatabase database)
    {
        return insert(database, getTableName(), getContentValues());
    }

    public int insert()
    {
        return insert(getWritableDatabase(), getTableName(), getContentValues());
    }

    public int insert(T data)
    {
        if (data instanceof SqlLiteApi)
        {
            SqlLiteApi sqlLiteData = (SqlLiteApi) data;
            if (!sqlLiteData.withReadableDatabase())
                sqlLiteData.setReadableDatabase(getReadableDatabase());
            if (!sqlLiteData.withWritableDatabase())
                sqlLiteData.setWritableDatabase(getWritableDatabase());
        }
        return data.insert();
    }

    public int insert(Set<T> dataSet)
    {
        int result = GenericDatabase.DATA_NOT_FOUND;
        for (T data: dataSet) result = insert(data);
        return result;
    }

    public Map<String, Object> getData()
    {
        return Collections.emptyMap();
    }

    @NonNull
    private ContentValues getContentValues()
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

    private Cursor select(SQLiteDatabase database, String fieldFilter, Object valueFilter, String operator)
    {
        String script = selectColumnsQueryPart();
        if (valueFilter instanceof String) valueFilter = "\'" + valueFilter + "\'";
        script += " WHERE " + fieldFilter + " " + operator + " " + valueFilter;
        return runQuery(database, script);
    }

    private int getMaxId()
    {
        String max_id = "max_id";
        String script = "SELECT max(" + ID + ") " + max_id + " FROM " + getTableName();
        Cursor cursor = runQuery(getReadableDatabase(), script);
        if (checkCursor(cursor))
        {
            return cursor.getInt(cursor.getColumnIndex(max_id));
        }
        return GenericDatabase.DATA_NOT_FOUND;
    }

    public Cursor runQuery(SQLiteDatabase database, String script)
    {
        Log.info("script=" + script);
        return database.rawQuery(script, null);
    }

    private Cursor select(SQLiteDatabase database, String fieldFilter, Object valueFilter)
    {
        return select(database, fieldFilter, valueFilter, EQUAL);
    }

    private Cursor select(String fieldFilter, Object valueFilter)
    {
        return select(getReadableDatabase(), fieldFilter, valueFilter);
    }

    private Cursor select(String fieldFilter, Object valueFilter, String operator)
    {
        return select(getReadableDatabase(), fieldFilter, valueFilter, operator);
    }

    private Cursor selectById(Object valueFilter)
    {
        return select(ID, valueFilter);
    }

    private Cursor selectMoreThanId(Object valueFilter)
    {
        return select(ID, valueFilter, MORE_THAN);
    }

    private Cursor selectLessThanId(Object valueFilter)
    {
        return select(ID, valueFilter, LESS_THAN);
    }

    private Cursor selectNotEqualId(Object valueFilter)
    {
        return select(ID, valueFilter, NOT_EQUAL);
    }

    private Cursor selectFirst()
    {
        return select(getReadableDatabase(), ID, 1);
    }

    private Cursor selectAll()
    {
        return selectAll(getReadableDatabase());
    }

    private Cursor selectAll(SQLiteDatabase database)
    {
        String script = selectColumnsQueryPart();
        return runQuery(database, script);
    }

    private Cursor selectLast(SQLiteDatabase database)
    {
        String script = selectColumnsQueryPart();
        String tableName = getTableName();
        script += " LEFT JOIN sync ON sync.table_name=\'" + tableName + "\' AND " + tableName + ".id > sync.sync_id";
        //script += " WHERE " + tableName + ".id "+ MORE_THAN + " (select sync.sync_id from sync where table_name=\'" + tableName + "\')";
        return runQuery(database, script);
    }

    private Cursor selectLast()
    {
        return selectLast(getReadableDatabase());
    }

    private Set<T> getActualBySync(SQLiteDatabase readableDatabase)
    {
        return getDataSetFromCursor(readableDatabase, selectLast(readableDatabase));
    }

    @Override
    public T filterBy(String key, Object value)
    {
        return getDataFromCursorWithClosing(select(key, value));
    }

    @Override
    public Set<T> moreThanId(Object id)
    {
        return getDataSetFromCursor(readableDatabase, selectMoreThanId(id));
    }

    @Override
    public Set<T> lessThanId(Object id)
    {
        return getDataSetFromCursor(readableDatabase, selectLessThanId(id));
    }

    @Override
    public Set<T> notEqualId(Object id)
    {
        return getDataSetFromCursor(readableDatabase, selectNotEqualId(id));
    }

    @Override
    public T byId(Object id)
    {
        return getDataFromCursorWithClosing(selectById(id));
    }

    @Override
    public Set<T> getActualBySync()
    {
        return getActualBySync(getReadableDatabase());
    }

    public GenericSync getSyncForUpdate(GenericAccount account)
    {
        int maxId = getMaxId();
        String tableName = getTableName();
        if (Log.isInfoEnabled()) Log.info("maxId=" + maxId + " for table " + tableName);
        if (maxId > 0)
            return new Sync(account.getId(), maxId, tableName);
        else
            return new Sync();
    }

    @Override
    public T getFirst()
    {
        return getDataFromCursorWithClosing(selectFirst());
    }

    private Set<T> getAll(SQLiteDatabase readableDatabase)
    {
        return getDataSetFromCursor(readableDatabase, selectAll(readableDatabase));
    }

    public Set<T> getAll()
    {
        return getAll(getReadableDatabase());
    }

    @NonNull
    private Set<T> getDataSetFromCursor(SQLiteDatabase readableDatabase, Cursor cursor)
    {
        if (!checkCursor(cursor)) return Collections.emptySet();
        Set<T> dataSet = new LinkedHashSet<>();
        do
        {
            T data = getDataFromCursor(cursor);
            setDeviceIfNeeeded(data);
            if (!data.isEmpty()) dataSet.add(data);
            Log.debug("data=" + data + "; cursor" + EQUAL + cursor);
            if (!cursor.isLast()) cursor.moveToNext();
            else break;
        } while (!cursor.isClosed());

        cursor.close();
        return dataSet;
    }

    private boolean checkCursor(Cursor cursor)
    {
        int count = cursor.getCount();
        if (Log.isInfoEnabled()) Log.info("cursorCount=" + count);
        if (!cursor.isClosed() && count > 0){
            cursor.moveToFirst();
            return true;
        }
        else
        {
            Log.warn("close empty cursor: " + cursor);
            if (!cursor.isClosed()) cursor.close();
            return false;
        }
    }

    @NonNull
    private GenericDevice initDevices(SQLiteDatabase readableDatabase)
    {
        SqlLiteApi<GenericDevice> api = (SqlLiteApi<GenericDevice>) SqlLiteDatabaseHelper.DEVICE;
        api.setReadableDatabase(readableDatabase);
        return api.getFirst();
    }

    private T getDataFromCursorWithClosing(Cursor cursor)
    {
        if (checkCursor(cursor))
        {
            T data = getDataFromCursor(cursor);
            setDeviceIfNeeeded(data);
            cursor.close();
            return data;
        }
        else
            return emptyData();
    }

    private void setDeviceIfNeeeded(T data)
    {
        if (data instanceof DeviceDependable)
            ((DeviceDependable) data).setDevice(device);
    }

    @NonNull
    private String selectColumnsQueryPart()
    {
        String script = "SELECT ";
        Set<String> fields = new LinkedHashSet<>(getFields());
        fields.addAll(SqlLiteData.fields.keySet());
        String tableName = getTableName();

        for (String field : fields)
            script += tableName + "." + field + ", ";

        script = script.substring(0, script.lastIndexOf(",")) + " FROM " + tableName;
        return script;
    }

    protected Object delete(SQLiteDatabase database, String fieldFilter, String valueFilter, String operator)
    {
        String script = "delete from " + getTableName() + " where " + fieldFilter + " " + operator + " " + valueFilter;
        if (Log.isDebugEnabled()) Log.debug(script);
        return database.delete(getTableName(), fieldFilter, new String[]{valueFilter});
    }

    protected Object delete(String fieldFilter, String valueFilter, String operator)
    {
        return delete(getWritableDatabase(), fieldFilter, valueFilter, operator);
    }

    protected Object delete(String fieldFilter, String valueFilter)
    {
        return delete(fieldFilter, valueFilter, EQUAL);
    }

    protected Object deleteById(Object valueFilter)
    {
        return delete(ID, String.valueOf(valueFilter));
    }

    protected Object deleteMoreThaId(Object valueFilter)
    {
        return delete(ID, String.valueOf(valueFilter), MORE_THAN);
    }

    protected Object deleteLessThaId(Object valueFilter)
    {
        return delete(ID, String.valueOf(valueFilter), LESS_THAN);
    }

    protected Object deleteNotEqualId(Object valueFilter)
    {
        return delete(ID, String.valueOf(valueFilter), NOT_EQUAL);
    }

    protected Object deleteFirst()
    {
        return delete(ID, String.valueOf(1));
    }

    protected Object deleteAll()
    {
        return delete(ID, String.valueOf(0), MORE_THAN);
    }

    protected void fillGenericByCursor(T data, Cursor cursor)
    {
        data.setId(cursor.getInt(cursor.getColumnIndex(ID)));
        data.setCreatedWhen(cursor.getString(cursor.getColumnIndex(CREATED_WHEN)));
        data.setTimezone(cursor.getString(cursor.getColumnIndex(TIMEZONE)));
        data.setDescription(cursor.getString(cursor.getColumnIndex(DESCRIPTION)));

        if (data instanceof SqlLiteApi)
        {
            SqlLiteApi sqlLiteData = (SqlLiteApi) data;
            if (this.withReadableDatabase() && !sqlLiteData.withReadableDatabase())
                sqlLiteData.setReadableDatabase(getReadableDatabase());
            if (this.withWritableDatabase() && !sqlLiteData.withWritableDatabase())
                sqlLiteData.setWritableDatabase(getWritableDatabase());
        }
    }
}
