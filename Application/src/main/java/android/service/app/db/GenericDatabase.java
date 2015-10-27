package android.service.app.db;

import android.service.app.db.data.GenericGps;
import android.service.app.db.data.GenericMessage;
import android.service.app.db.inventory.GenericDevice;
import android.service.app.db.sync.GenericSync;
import android.service.app.db.user.GenericAccount;

import java.util.Set;

public interface GenericDatabase
{
    int EMPTY_DATA = -2;
    int DATA_NOT_FOUND = -1;
    String EMPTY_EMAIL = "";

    <T extends GenericData> int insert(T data);
    <T extends GenericData> int insert(Set<T> data);

    GenericDevice devices();

    GenericAccount account();

    GenericMessage messages();

    GenericGps coordinates();

    GenericSync sync_points();

    void updateOrInsertSyncIfNeeded(GenericSync newSync);
}
