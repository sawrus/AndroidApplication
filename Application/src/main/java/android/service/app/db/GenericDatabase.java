package android.service.app.db;

import android.service.app.db.data.GenericAccount;
import android.service.app.db.data.GenericData;
import android.service.app.db.data.GenericDataApi;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.data.GenericGps;
import android.service.app.db.data.GenericMessage;
import android.service.app.db.data.GenericSync;

import java.util.Set;

public interface GenericDatabase
{
    int EMPTY_DATA = -2;
    int DATA_NOT_FOUND = -1;
    String EMPTY_EMAIL = "";

    <T extends GenericData> int insert(T data);
    <T extends GenericData> int insert(Set<T> data);

    GenericDataApi<GenericDevice> devices();

    GenericDataApi<GenericAccount> accounts();

    GenericDataApi<GenericMessage> messages();

    GenericDataApi<GenericGps> coordinates();

    GenericDataApi<GenericSync> points();

    void updateOrInsertSyncIfNeeded(GenericSync newSync);
}
