package android.service.app.db.data;

import android.database.sqlite.SQLiteDatabase;

public interface GenericSync extends GenericData
{
    int getAccountId();
    int getSyncId();
    String getTable();
    void setAccountId(int account_id);
    void setSyncId(int sync_id);
    void setTable(String table);
    void update(SQLiteDatabase database, GenericSync oldSync, GenericSync newSync);
}
