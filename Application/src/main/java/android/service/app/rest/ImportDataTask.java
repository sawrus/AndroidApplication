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
            GenericAccount account = accounts().getFirst();
            if (account.isEmpty()) return buildSyncOutput(EMPTY_ACCOUNT);

            String deviceFilter = "?account=" + account.getEmail();
            Set<GenericDevice> devices = restBridge.getDevices(DataFilter.BY_VALUE.setFilter(deviceFilter));
            if (Log.isInfoEnabled()) Log.info("devices=" + devices);
            insert(devices);

            for (GenericDevice device: devices().getAll())
            {
                String deviceId = String.valueOf(device.getId());
                String date = device.getCreatedWhen();
                int limit = -1;

                String filter = String.format("?device=%s&date=%s&n=%s", deviceId, date, limit);
                DataFilter dataFilter = DataFilter.BY_DATE.setFilter(filter);

                Set<GenericMessage> messages = restBridge.getMessages(dataFilter);
                if (Log.isInfoEnabled()) Log.info("messages=" + messages);
                insert(messages);

                Set<GenericGps> coordinates = restBridge.getCoordinates(dataFilter);
                if (Log.isInfoEnabled()) Log.info("messages=" + messages);
                insert(coordinates);
            }
        }
        catch (Exception e)
        {
            AndroidUtils.handleException(e);
            return new SyncOutput(e);
        }

        return buildSuccessSyncOutput();
    }
}
