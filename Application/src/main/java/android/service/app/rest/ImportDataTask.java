package android.service.app.rest;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.service.app.db.Data;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.data.Gps;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.db.sync.Sync;
import android.service.app.db.user.Account;
import android.service.app.json.DataFilter;
import android.service.app.json.RestBridge;
import android.service.app.utils.Log;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class ImportDataTask<Input> extends AsyncTask<Input, Void, SyncOutput>
{
    private final DatabaseHelper localDatabase;
    private final CallbackHandler<SyncOutput> handler;
    private final RestBridge restBridge;

    public ImportDataTask(DatabaseHelper localDatabase, Context context, CallbackHandler<SyncOutput> handler)
    {
        this.localDatabase = localDatabase;
        this.handler = handler;
        this.restBridge = new RestBridge(context);
    }

    @SafeVarargs
    protected final SyncOutput doInBackground(Input... voids)
    {
        try
        {
            Set<Message> messages = restBridge.getMessages(DataFilter.BY_ID);
            Log.v("messages=" + messages);

            Set<Gps> coordinates = restBridge.getCoordinates(DataFilter.BY_ID);
            Log.v("coordinates=" + coordinates);

            Set<Device> devices = restBridge.getDevices(DataFilter.ALL);
            Log.v("devices=" + devices);

            Account account = restBridge.getAccount(DataFilter.ALL);
            Log.v("account=" + account);

            localDatabase.messages().insert(messages);
            localDatabase.coordinates().insert(coordinates);
            localDatabase.devices().insert(devices);
            localDatabase.account().insert(account);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new SyncOutput("e: " + e.getMessage());
        }

        return new SyncOutput("success");
    }

    @Override
    protected void onPostExecute(SyncOutput result)
    {
        handler.handle(result);
    }
}
