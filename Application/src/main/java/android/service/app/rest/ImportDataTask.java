package android.service.app.rest;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.data.Gps;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.db.user.Account;
import android.service.app.json.DataFilter;
import android.service.app.utils.AndroidUtils;
import android.service.app.utils.Log;

import java.util.Set;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class ImportDataTask<Input> extends GenericDataTask<Input>
{
    public ImportDataTask(DatabaseHelper localDatabase, Context context, CallbackHandler<SyncOutput> handler)
    {
        super(localDatabase, context, handler);
    }

    @Override
    protected final SyncOutput doInBackground(Input... voids)
    {
        try
        {
            Set<Message> messages = restBridge.getMessages(DataFilter.BY_ID);
            if (Log.isInfoEnabled()) Log.info("messages=" + messages);

            Set<Gps> coordinates = restBridge.getCoordinates(DataFilter.BY_ID);
            if (Log.isInfoEnabled()) Log.info("coordinates=" + coordinates);

            Set<Device> devices = restBridge.getDevices(DataFilter.ALL);
            if (Log.isInfoEnabled()) Log.info("devices=" + devices);

            Account account = restBridge.getAccount(DataFilter.ALL);
            if (Log.isInfoEnabled()) Log.info("account=" + account);

            localDatabase.messages().insert(messages);
            localDatabase.coordinates().insert(coordinates);
            localDatabase.devices().insert(devices);
            localDatabase.account().insert(account);
        }
        catch (Exception e)
        {
            AndroidUtils.handleException(e);
            return new SyncOutput(e);
        }

        return buildSuccessSyncOutput();
    }
}
