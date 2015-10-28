package android.service.app.rest;

import android.content.Context;
import android.service.app.db.data.GenericAccount;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.data.GenericGps;
import android.service.app.db.data.GenericMessage;
import android.service.app.db.sqllite.SqlLiteDatabaseHelper;
import android.service.app.json.DataFilter;
import android.service.app.utils.AndroidUtils;
import android.service.app.utils.Log;

import java.util.Set;

public class ImportDataTask<Input> extends GenericDataTask<Input>
{
    public ImportDataTask(SqlLiteDatabaseHelper localDatabase, Context context, CallbackHandler<SyncOutput> handler)
    {
        super(localDatabase, context, handler);
    }

    @Override
    protected final SyncOutput doInBackground(Input... voids)
    {
        try
        {
            Set<GenericMessage> messages = restBridge.getMessages(DataFilter.BY_ID);
            if (Log.isInfoEnabled()) Log.info("messages=" + messages);

            Set<GenericGps> coordinates = restBridge.getCoordinates(DataFilter.BY_ID);
            if (Log.isInfoEnabled()) Log.info("coordinates=" + coordinates);

            Set<GenericDevice> devices = restBridge.getDevices(DataFilter.ALL);
            if (Log.isInfoEnabled()) Log.info("devices=" + devices);

            GenericAccount account = restBridge.getAccount(DataFilter.ALL);
            if (Log.isInfoEnabled()) Log.info("account=" + account);

//            insert(messages);
//            insert(coordinates);
//            insert(devices);
//            insert(account);
        }
        catch (Exception e)
        {
            AndroidUtils.handleException(e);
            return new SyncOutput(e);
        }

        return buildSuccessSyncOutput();
    }
}
