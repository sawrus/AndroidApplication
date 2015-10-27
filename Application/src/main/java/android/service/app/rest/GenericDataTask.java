package android.service.app.rest;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.GenericData;
import android.service.app.db.GenericDatabase;
import android.service.app.db.data.GenericGps;
import android.service.app.db.data.GenericMessage;
import android.service.app.db.inventory.GenericDevice;
import android.service.app.db.sync.GenericSync;
import android.service.app.db.user.GenericAccount;
import android.service.app.json.RestBridge;
import android.support.annotation.NonNull;

import java.util.Set;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public abstract class GenericDataTask<Input> extends AsyncTask<Input, Void, SyncOutput> implements GenericDatabase
{
    protected final DatabaseHelper localDatabase;
    private final CallbackHandler<SyncOutput> handler;
    protected final RestBridge restBridge;

    public GenericDataTask(DatabaseHelper localDatabase, Context context, CallbackHandler<SyncOutput> handler)
    {
        this.localDatabase = localDatabase;
        this.handler = handler;
        this.restBridge = new RestBridge(context);
    }

    protected abstract SyncOutput doInBackground(Input... voids);

    @NonNull
    protected SyncOutput buildSuccessSyncOutput()
    {
        return new SyncOutput("success for " + getClass().getSimpleName());
    }

    @Override
    protected void onPostExecute(SyncOutput result)
    {
        handler.handle(result);
    }

    @Override
    public String toString()
    {
        return "GenericDataTask{" +
                "localDatabase=" + localDatabase +
                ", handler=" + handler +
                ", restBridge=" + restBridge +
                '}';
    }

    @Override
    public <T extends GenericData> int insert(T data)
    {
        return localDatabase.insert(data);
    }

    @Override
    public <T extends GenericData> int insert(Set<T> data)
    {
        return localDatabase.insert(data);
    }

    @Override
    public GenericDevice devices()
    {
        return localDatabase.devices();
    }

    @Override
    public GenericAccount account()
    {
        return localDatabase.account();
    }

    @Override
    public GenericMessage messages()
    {
        return localDatabase.messages();
    }

    @Override
    public GenericSync sync_points()
    {
        return localDatabase.sync_points();
    }

    @Override
    public GenericGps coordinates()
    {
        return localDatabase.coordinates();
    }

    @Override
    public void updateOrInsertSyncIfNeeded(GenericSync newSync)
    {
        localDatabase.updateOrInsertSyncIfNeeded(newSync);
    }
}

