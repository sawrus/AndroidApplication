package android.service.app.db;

import android.database.sqlite.SQLiteDatabase;
import android.service.app.db.sync.GenericSync;
import android.service.app.db.sync.impl.Sync;
import android.service.app.db.user.Account;
import android.service.app.db.user.GenericAccount;

import java.util.Map;
import java.util.Set;

public interface GenericData<T extends GenericData>
{
    Integer getId();
    String getDescription();
    void setDescription(String description);
    String getTimezone();
    void setTimezone(String timezone);
    String getCreatedWhen();
    void setCreatedWhen(String created_when);

    T filterBy(String key, Object value);
    Set<T> moreThanId(Object id);
    Set<T> lessThanId(Object id);
    Set<T> notEqualId(Object id);
    T byId(Object id);
    Set<T> getActualBySync();
    T getFirst();
    Set<T> getAll();
    GenericData<T> setReadableDatabase(SQLiteDatabase readableDatabase);
    GenericData<T> setWritableDatabase(SQLiteDatabase writableDatabase);
    boolean withReadableDatabase();
    boolean withWritableDatabase();
    boolean isEmpty();
    void setId(Integer id);
    String getTableName();
    int insert();
    Map<String, Object> getData();
    GenericSync getSyncForUpdate(GenericAccount account);
}
