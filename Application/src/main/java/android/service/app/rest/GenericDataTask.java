package android.service.app.rest;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.service.app.db.GenericDatabase;
import android.service.app.db.data.GenericAccount;
import android.service.app.db.data.GenericData;
import android.service.app.db.data.GenericDataApi;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.data.GenericGps;
import android.service.app.db.data.GenericMessage;
import android.service.app.db.data.GenericSync;
import android.service.app.db.sqllite.SqlLiteDatabaseHelper;
import android.service.app.json.RestBridge;
import android.support.annotation.NonNull;

import java.util.Set;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public abstract class GenericDataTask<Input> extends AsyncTask<Input, Void, SyncOutput> implements GenericDatabase
{
    protected final SqlLiteDatabaseHelper sqlLiteDatabaseHelper;
    private final CallbackHandler<SyncOutput> handler;
    protected final RestBridge restBridge;

    public GenericDataTask(SqlLiteDatabaseHelper localDatabase, Context context, CallbackHandler<SyncOutput> handler)
    {
        this.sqlLiteDatabaseHelper = localDatabase;
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
                "localDatabase=" + sqlLiteDatabaseHelper +
                ", handler=" + handler +
                ", restBridge=" + restBridge +
                '}';
    }

    @Override
    public <T extends GenericData> int insert(T data)
    {
        return sqlLiteDatabaseHelper.insert(data);
    }

    @Override
    public <T extends GenericData> int insert(Set<T> data)
    {
        return sqlLiteDatabaseHelper.insert(data);
    }

    @Override
    public GenericDataApi<GenericDevice> devices()
    {
        return sqlLiteDatabaseHelper.devices();
    }

    @Override
    public GenericDataApi<GenericAccount> accounts()
    {
        return sqlLiteDatabaseHelper.accounts();
    }

    @Override
    public GenericDataApi<GenericMessage> messages()
    {
        return sqlLiteDatabaseHelper.messages();
    }

    @Override
    public GenericDataApi<GenericGps> coordinates()
    {
        return sqlLiteDatabaseHelper.coordinates();
    }

    @Override
    public GenericDataApi<GenericSync> points()
    {
        return sqlLiteDatabaseHelper.points();
    }

    @Override
    public void updateOrInsertSyncIfNeeded(GenericSync newSync)
    {
        sqlLiteDatabaseHelper.updateOrInsertSyncIfNeeded(newSync);
    }
}

