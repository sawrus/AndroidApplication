package android.service.app.rest;

import android.content.Context;
import android.service.app.db.data.GenericAccount;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.data.GenericGps;
import android.service.app.db.data.GenericMessage;
import android.service.app.db.sqllite.SqlLiteDatabaseHelper;
import android.service.app.json.DataFilter;
import android.service.app.utils.AndroidUtils;
import android.service.app.utils.Log;

import com.loopj.android.http.RequestParams;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

public class ImportDataTask<Input> extends GenericDataTask<Input>
{
    public static final int DAYS_HISTORY_PERIOD = 10;
    public static final int DATA_LIMIT = -1;

    public ImportDataTask(SqlLiteDatabaseHelper localDatabase, Context context, CallbackHandler<SyncOutput> handler)
    {
        super(localDatabase, context, handler);
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
            Set<GenericDevice> devices = restBridge.getDevices(DataFilter.BY_VALUE.setRequestParams(requestDeviceParams));
            if (Log.isInfoEnabled()) Log.info("devices=" + devices);
            insert(devices);

            Date dateForTrackData = getDateForTrackData();
            if (Log.isInfoEnabled()) Log.info("dateForTrackData=" + dateForTrackData);

            for (GenericDevice device: devices().getAll())
            {
                RequestParams requestDataParams = new RequestParams();
                requestDataParams.put("device", device.getId());
                requestDataParams.put("date", dateForTrackData);
                requestDataParams.put("n", DATA_LIMIT);
                DataFilter dataFilter = DataFilter.BY_DATE.setRequestParams(requestDataParams);

                Set<GenericMessage> messages = restBridge.getMessages(dataFilter);
                if (Log.isInfoEnabled()) Log.info("messages=" + messages);
                insert(messages);

                Set<GenericGps> coordinates = restBridge.getCoordinates(dataFilter);
                if (Log.isInfoEnabled()) Log.info("messages=" + messages);
                insert(coordinates);
            }
        }
        catch (Exception e)
        {
            AndroidUtils.handleException(e);
            return new SyncOutput(e);
        }

        return buildSuccessSyncOutput();
    }

    private Date getDateForTrackData()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -DAYS_HISTORY_PERIOD);
        return calendar.getTime();
    }
}
