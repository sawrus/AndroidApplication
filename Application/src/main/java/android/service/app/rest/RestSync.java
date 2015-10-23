package android.service.app.rest;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.data.Message;
import android.service.app.db.sync.Sync;
import android.service.app.json.RestBridge;
import android.service.app.utils.Log;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class RestSync<Input> extends AsyncTask<Input, Void, SyncOutput>
{
    private final DatabaseHelper localDatabase;
    private final CallbackHandler<SyncOutput> handler;
    private final RestBridge restBridge;
    public RestSync(DatabaseHelper localDatabase, Context context, CallbackHandler<SyncOutput> handler)
    {
        this.localDatabase = localDatabase;
        this.handler = handler;
        this.restBridge = new RestBridge(context);
    }

    @SafeVarargs
    protected final SyncOutput doInBackground(Input... voids)
    {
        Log.v("RestSync:voids=" + Arrays.toString(voids));
        //todo, you can use the part of implementation from AndroidApplication\Application\src\main\java\android\service\app\db\mongo\MongoClient.java

        try
        {
            syncMessages();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new SyncOutput("e: " + e.getMessage());
        }

        return new SyncOutput("success");
    }

    private void syncMessages()
    {
        Integer accountId = localDatabase.selectFirstDevice().getAccountId();

        Set<Message> dataSet = localDatabase.getMessages();
        if (dataSet.isEmpty())
        {
            Log.v("no messages in local database");
            return;
        }

        String tableName = DatabaseHelper.MESSAGE.getTableName();
        Sync sync = localDatabase.selectSyncByTableName(tableName);

        Integer syncId = sync.getSyncId();
        Integer newSyncId = -1;

        //todo: need to use guava
        Set<Message> newDataSet = new LinkedHashSet<>();
        for (Message data : dataSet)
        {
            if (!data.isEmpty() && (data.getId() > syncId))
            {
                newDataSet.add(data);
                newSyncId = data.getId();
            }
        }

        Log.v("newDataSet=" + newDataSet);

        restBridge.postMessages(newDataSet);
        localDatabase.updateOrInsertSyncIfNeeded(new Sync(accountId, newSyncId, tableName));
    }

    @Override
    protected void onPostExecute(SyncOutput result)
    {
        handler.handle(result);
    }
}
