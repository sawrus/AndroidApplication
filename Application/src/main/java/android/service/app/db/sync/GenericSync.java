package android.service.app.db.sync;

import android.database.sqlite.SQLiteDatabase;
import android.service.app.db.GenericData;

public interface GenericSync extends GenericData<GenericSync>
{
    int getAccountId();
    int getSyncId();
    String getTable();
    void setAccountId(int account_id);
    void setSyncId(int sync_id);
    void setTable(String table);
    void update(SQLiteDatabase database, GenericSync oldSync, GenericSync newSync);
}
