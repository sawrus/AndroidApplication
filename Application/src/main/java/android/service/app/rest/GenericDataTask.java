package android.service.app.rest;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.service.app.db.DatabaseHelper;
import android.service.app.json.RestBridge;
import android.support.annotation.NonNull;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public abstract class GenericDataTask<Input> extends AsyncTask<Input, Void, SyncOutput>
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
}

