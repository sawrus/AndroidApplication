package android.service.app.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.service.app.db.data.GenericGps;
import android.service.app.db.inventory.impl.Device;
import android.service.app.db.inventory.GenericDevice;
import android.service.app.db.sync.GenericSync;
import android.service.app.db.sync.impl.Sync;
import android.service.app.db.user.Account;
import android.service.app.db.user.GenericAccount;
import android.service.app.utils.Log;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Data<T extends GenericData> implements GenericData<T>
{
    protected static final String INTEGER = "INTEGER";
    protected static final String DOUBLE = "DOUBLE";
    protected static final String INTEGER_PRIMARY_KEY = "INTEGER PRIMARY KEY";
    protected static final String INTEGER_PRIMARY_KEY_AUTOINCREMENT = "INTEGER PRIMARY KEY AUTOINCREMENT";

    protected static final String TEXT = "TEXT";
    protected static final String DATETIME = "DATETIME";

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

    private SQLiteDatabase readableDatabase = null;
    private SQLiteDatabase writableDatabase = null;

    private int id = GenericDatabase.EMPTY_DATA;
    private String description = "";
    private String created_when = getCoordinatedUniversalDateTime();
    private String timezone = TimeZone.getDefault().getID() ;

    private static final Map<String, String> fields = Collections.unmodifiableMap(new LinkedHashMap<String, String>(){
        {put(ID, INTEGER_PRIMARY_KEY);}
        {put(DESCRIPTION, TEXT);}
        {put(TIMEZONE, TEXT);}
        {put(CREATED_WHEN, TEXT);}
    });

    public static GenericDevice device = new Device();
    private static AtomicBoolean wasInitiatedEarlier = new AtomicBoolean(false);

    public boolean isWasInitiatedEarlier()
    {
        return wasInitiatedEarlier.get();
    }

    public synchronized void init()
    {
        if (readableDatabase != null && device.isEmpty() && this instanceof DeviceDependable)
        {
            device = initDevices(readableDatabase).getFirst();
            if (!device.isEmpty() && !isWasInitiatedEarlier()) wasInitiatedEarlier.set(true);
        }
    }


    @Override
    public void setId(Integer id)
    {
        this.id = id;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public String getTimezone()
    {
        return timezone;
    }

    @Override
    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }

    @Override
    public String getCreatedWhen()
    {
        return created_when;
    }

    @Override
    public void setCreatedWhen(String created_when)
    {
        this.created_when = created_when;
    }

    private String getCoordinatedUniversalDateTime()
    {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone(GMT_TIME_ZONE));
        Date date = c.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }

    public Data(SQLiteDatabase readableDatabase, SQLiteDatabase writableDatabase)
    {
        this.readableDatabase = readableDatabase;
        this.writableDatabase = writableDatabase;
    }

    public Data(SQLiteDatabase readableDatabase)
    {
        this.readableDatabase = readableDatabase;
    }

    public synchronized GenericData<T> setReadableDatabase(SQLiteDatabase readableDatabase)
    {
        this.readableDatabase = readableDatabase;
        return this;
    }

    public synchronized GenericData<T> setWritableDatabase(SQLiteDatabase writableDatabase)
    {
        this.writableDatabase = writableDatabase;
        return this;
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

    public Data()
    {
        //init();
    }

    protected static String generateCreateTableScript(String tableName, Map<String, String> fields)
    {
        LinkedHashMap<String, String> _fields = new LinkedHashMap<>(fields);
        _fields.putAll(Data.fields);
        String script = "CREATE TABLE " + tableName + " (";
        for (Map.Entry<String, String> filed : _fields.entrySet())
            script += filed.getKey() + " " + filed.getValue() + ",";
        return script.substring(0, script.lastIndexOf(",")) + ")";
    }

    public abstract String generateCreateTableScript();

    protected static String generateDropTableScript(String tableName)
    {
        return "DROP TABLE " + tableName;
    }

    public abstract String generateDropTableScript();

    protected static int insert(SQLiteDatabase database, String tableName, ContentValues values)
    {
        return (int) database.insert(tableName, null, values);
    }

    private int insert(SQLiteDatabase database)
    {
        return insert(database, getTableName(), getContentValues());
    }

    @Override
    public int insert()
    {
        return insert(getWritableDatabase(), getTableName(), getContentValues());
    }

    public int insert(T data)
    {
        if (!data.withReadableDatabase())
            data.setReadableDatabase(getReadableDatabase());
        if (!data.withWritableDatabase())
            data.setWritableDatabase(getWritableDatabase());
        return data.insert();
    }

    public int insert(Set<T> dataSet)
    {
        int result = GenericDatabase.DATA_NOT_FOUND;
        for (T data: dataSet)
        {
            data.setReadableDatabase(getReadableDatabase());
            data.setWritableDatabase(getWritableDatabase());
            result = data.insert();
        }

        return result;
    }

    @Override
    public Map<String, Object> getData()
    {
        //checkOnEmptyAndThrowException();
        return new LinkedHashMap<String, Object>()
        {
            //{put(ID, getId());}
            {put(DESCRIPTION, getDescription());}
            {put(TIMEZONE, getTimezone());}
            {put(CREATED_WHEN, getCreatedWhen());}
        };
    }

    private void checkOnEmptyAndThrowException()
    {
        if (isEmpty()) throw new IllegalStateException("stub mode for data: " + this.toString());
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
        if (Log.isDebugEnabled()) Log.debug("script=" + script);
        return database.rawQuery(script, null);
    }

    private int getMaxId()
    {
        String max_id = "max_id";
        String script = "SELECT max(" + ID + ") " + max_id + " FROM " + getTableName();
        if (Log.isDebugEnabled()) Log.debug("script=" + script);
        Cursor cursor = getReadableDatabase().rawQuery(script, null);
        if (checkCursor(cursor))
        {
            return cursor.getInt(cursor.getColumnIndex(max_id));
        }
        return GenericDatabase.DATA_NOT_FOUND;
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
        if (Log.isDebugEnabled()) Log.debug("script=" + script);
        return database.rawQuery(script, null);
    }

    private Cursor selectLast(SQLiteDatabase database)
    {
        String script = selectColumnsQueryPart();
        String tableName = getTableName();
        script += " LEFT JOIN sync ON sync.table_name=\'" + tableName + "\' AND " + tableName + ".id > sync.sync_id";
        //script += " WHERE " + tableName + ".id "+ MORE_THAN + " (select sync.sync_id from sync where table_name=\'" + tableName + "\')";
        if (Log.isInfoEnabled()) Log.info("script=" + script);
        return database.rawQuery(script, null);
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

    @Override
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
    private Device initDevices(SQLiteDatabase readableDatabase)
    {
        return new Device(readableDatabase);
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

    protected abstract T emptyData();

    protected abstract T getDataFromCursor(Cursor cursor);

    @NonNull
    private String selectColumnsQueryPart()
    {
        String script = "SELECT ";
        Set<String> fields = new LinkedHashSet<>(getFields());
        fields.addAll(Data.fields.keySet());
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

    protected Object deleteAll(SQLiteDatabase database)
    {
        return delete(ID, String.valueOf(0), MORE_THAN);
    }

    protected Object deleteAll()
    {
        return delete(ID, String.valueOf(1));
    }

    protected abstract Set<String> getFields();

    public abstract String getTableName();

    public boolean isEmpty()
    {
        return GenericDatabase.EMPTY_DATA == getId();
    }

    public Integer getId()
    {
        return id;
    }

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

    protected void fillGenericByCursor(T data, Cursor cursor)
    {
        data.setId(cursor.getInt(cursor.getColumnIndex(ID)));
        data.setCreatedWhen(cursor.getString(cursor.getColumnIndex(CREATED_WHEN)));
        data.setTimezone(cursor.getString(cursor.getColumnIndex(TIMEZONE)));
        data.setDescription(cursor.getString(cursor.getColumnIndex(DESCRIPTION)));
        if (this.withReadableDatabase() && !data.withReadableDatabase()) data.setReadableDatabase(getReadableDatabase());
        if (this.withWritableDatabase() && !data.withWritableDatabase()) data.setWritableDatabase(getWritableDatabase());
    }

    @Override
    public String toString()
    {
        return "Data{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", created_when='" + created_when + '\'' +
                ", timezone='" + timezone + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Data<?> data = (Data<?>) o;

        return getId() == data.getId();

    }

    @Override
    public int hashCode()
    {
        return getId();
    }
}
