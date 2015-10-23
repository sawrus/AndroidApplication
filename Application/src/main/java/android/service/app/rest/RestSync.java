package android.service.app.rest;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.service.app.db.DatabaseHelper;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class RestSync<Input> extends AsyncTask<Input, Void, DatabaseSyncOutput>
{
    private final DatabaseHelper localDatabase;
    private final Context context;
    private final CallbackHandler<DatabaseSyncOutput> handler;

    public RestSync(DatabaseHelper localDatabase, Context context, CallbackHandler<DatabaseSyncOutput> handler)
    {
        this.localDatabase = localDatabase;
        this.context = context;
        this.handler = handler;
    }

    @SafeVarargs
    protected final DatabaseSyncOutput doInBackground(Input... voids)
    {
        //todo, you can use the part of implementation from AndroidApplication\Application\src\main\java\android\service\app\db\mongo\MongoClient.java

        return null;
    }

    @Override
    protected void onPostExecute(DatabaseSyncOutput result)
    {
        handler.handle(result);
    }
}
