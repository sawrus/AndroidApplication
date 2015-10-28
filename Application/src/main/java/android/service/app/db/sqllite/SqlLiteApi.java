package android.service.app.db.sqllite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.service.app.db.data.GenericData;
import android.service.app.db.data.GenericDataApi;

import java.util.Set;

public interface SqlLiteApi<T extends GenericData> extends GenericDataApi<T>
{
    T emptyData();
    T getDataFromCursor(Cursor cursor);

    String getTableName();
    Set<String> getFields();
    String generateCreateTableScript();
    String generateDropTableScript();

    void setReadableDatabase(SQLiteDatabase readableDatabase);
    void setWritableDatabase(SQLiteDatabase writableDatabase);
    boolean withReadableDatabase();
    boolean withWritableDatabase();
}
