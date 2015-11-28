package android.service.app.rest;

import android.content.Context;
import android.service.app.db.data.GenericAccount;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.data.GenericGps;
import android.service.app.db.data.GenericMessage;
import android.service.app.db.data.GenericSync;
import android.service.app.db.data.impl.Gps;
import android.service.app.db.data.impl.Message;
import android.service.app.db.data.impl.Sync;
import android.service.app.db.sqllite.SqlLiteDatabaseHelper;
import android.service.app.json.DataFilter;
import android.service.app.utils.AndroidUtils;
import android.service.app.utils.Log;

import com.loopj.android.http.RequestParams;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

public class ImportDataTask<Input> extends GenericDataTask<Input>
{
    public static final int DAYS_HISTORY_PERIOD = 10;
    public static final int DATA_LIMIT = -1;
    private final Context context;

    public ImportDataTask(SqlLiteDatabaseHelper localDatabase, Context context, CallbackHandler<SyncOutput> handler)
    {
        super(localDatabase, context, handler);

        this.context = context;
    }

    @Override
    protected final SyncOutput doInBackground(Input... voids)
    {
        try
        {
            GenericAccount account = accounts().getFirst();
            if (account.isEmpty()) return buildSyncOutput(EMPTY_ACCOUNT);

            RequestParams requestDeviceParams = new RequestParams();
            requestDeviceParams.put("account", account.getEmail());
            Set<GenericDevice> devicesAfterImport = restBridge.getDevices(DataFilter.BY_VALUE.setFilter(account.getEmail()));
            Set<GenericDevice> devicesFromDatabase = devices().getAll();
            for (GenericDevice device: devicesAfterImport){
                device.setAccountId(account.getId());
                if (!isAlreadyExist(devicesFromDatabase, device)) insert(device);
            }
            if (Log.isInfoEnabled()) Log.info(devicesAfterImport.size()  + " # devicesAfterImport: " + devicesAfterImport);

            //TODO: Need to support multiple devices sync schema
            Set<GenericDevice> devices = new LinkedHashSet<>();
            devices.add(devicesFromDatabase.iterator().next());

            GenericSync messageSync = points().filterBy(Sync.TABLE, Message.table_name);
            if (Log.isInfoEnabled()) Log.info("last.messageSync: " + messageSync);
            for (GenericDevice device: devices)
            {
                RequestParams requestDataParams = new RequestParams();
                requestDataParams.put("device", device.getName());
                requestDataParams.put("direction", "gte");
                requestDataParams.put("n", DATA_LIMIT);
                requestDataParams.put("syncid", messageSync.getSyncId());
                DataFilter dataFilter = DataFilter.BY_DATE.setRequestParams(requestDataParams);
                dataFilter.setContext(context);
                restBridge.getMessages(dataFilter, device);
            }
            updateOrInsertSyncIfNeeded(messages().getSyncForUpdate(account));

            GenericSync gpsSync = points().filterBy(Sync.TABLE, Gps.table_name);
            if (Log.isInfoEnabled()) Log.info("last.gpsSync: " + gpsSync);
            for (GenericDevice device: devices)
            {
                RequestParams requestDataParams = new RequestParams();
                requestDataParams.put("device", device.getName());
                requestDataParams.put("direction", "gte");
                requestDataParams.put("n", DATA_LIMIT);
                requestDataParams.put("syncid", gpsSync.getSyncId());
                DataFilter dataFilter = DataFilter.BY_DATE.setRequestParams(requestDataParams);
                dataFilter.setContext(context);
                restBridge.getCoordinates(dataFilter, device);
            }
            updateOrInsertSyncIfNeeded(points().getSyncForUpdate(account));
        }
        catch (Exception e)
        {
            AndroidUtils.handleException(e);
            return new SyncOutput(e);
        }

        return buildSuccessSyncOutput();
    }

    private boolean isAlreadyExist(Set<GenericDevice> deviceSet, GenericDevice device)
    {
        boolean deviceExist = false;
        for (GenericDevice genericDevice: deviceSet)
        {
            if (genericDevice.getName().equals(device.getName()))
            {
                deviceExist = true;
                if ((Log.isInfoEnabled())) Log.info("isAlreadyExist.device: " + device);
                break;
            }
        }
        return deviceExist;
    }

    private Date getDateForTrackData()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -DAYS_HISTORY_PERIOD);
        return calendar.getTime();
    }
}
