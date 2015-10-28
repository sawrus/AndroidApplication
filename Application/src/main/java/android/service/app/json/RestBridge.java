package android.service.app.json;

import android.content.Context;
import android.service.app.db.data.impl.Data;
import android.service.app.db.DataBridge;
import android.service.app.db.sqllite.SqlLiteDatabaseHelper;
import android.service.app.db.data.GenericData;
import android.service.app.db.data.GenericGps;
import android.service.app.db.data.GenericMessage;
import android.service.app.db.data.impl.Gps;
import android.service.app.db.data.impl.Message;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.data.impl.Device;
import android.service.app.db.data.impl.Account;
import android.service.app.db.data.GenericAccount;
import android.service.app.http.HttpClient;
import android.service.app.utils.Log;

import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class RestBridge implements DataBridge<DataFilter, RestHttpResponseHandler>
{
    private final Context context;

    public RestBridge(Context context)
    {
        this.context = context;
    }

    @Override
    public Set<GenericMessage> getMessages(DataFilter s)
    {
        return Collections.emptySet();
    }

    @Override
    public Set<GenericGps> getCoordinates(DataFilter s)
    {
        return Collections.emptySet();
    }

    @Override
    public GenericAccount getAccount(DataFilter s)
    {
        return new Account();
    }

    @Override
    public Set<GenericDevice> getDevices(DataFilter s)
    {
        return Collections.emptySet();
    }

    @Override
    public RestHttpResponseHandler postMessages(Set<GenericMessage> messages)
    {
        RestHttpResponseHandler responseHandler = null;
        Set<JSONObject> jsonObjects = new LinkedHashSet<>();
        for (GenericMessage message: messages)
        {
            Map<String, Object> data = message.getData();
            data.put(Message.DEVICE_ID, message.getDevice().getName());
            setSyncId(message, data);

            jsonObjects.add(new JSONObject(data));
            responseHandler = new RestHttpResponseHandler();
        }

        if (Log.isInfoEnabled()) Log.info("Message.jsonObjects=" + jsonObjects);
        HttpClient.postJson(context, Message.table_name, jsonObjects, responseHandler);
        return responseHandler;
    }

    private void setSyncId(GenericData bean, Map<String, Object> data)
    {
        data.put(Data.SYNCID, bean.getId());
    }

    @Override
    public RestHttpResponseHandler postGps(Set<GenericGps> gpsSets)
    {
        RestHttpResponseHandler responseHandler = null;
        Set<JSONObject> jsonObjects = new LinkedHashSet<>();
        for (GenericGps gps: gpsSets)
        {
            Map<String, Object> data = gps.getData();
            //todo: need to refactor
            data.put(Gps.DEVICE_ID, gps.getDevice().getName());
            setSyncId(gps, data);
            jsonObjects.add(new JSONObject(data));
            responseHandler = new RestHttpResponseHandler();
        }

        if (Log.isInfoEnabled()) Log.info("Gps.jsonObjects=" + jsonObjects);
        HttpClient.postJson(context, Gps.table_name, jsonObjects, responseHandler);
        return responseHandler;
    }

    @Override
    public RestHttpResponseHandler postAccount(GenericAccount account)
    {
        Map<String, Object> data = account.getData();
        JSONObject jsonObject = new JSONObject(data);
        if (Log.isInfoEnabled()) Log.info("Account.jsonObject=" + jsonObject);

        RestHttpResponseHandler responseHandler = new RestHttpResponseHandler();
        HttpClient.postJson(context, Account.table_name, jsonObject, responseHandler);
        return responseHandler;
    }

    @Override
    public RestHttpResponseHandler postDevice(GenericDevice device)
    {
        Map<String, Object> data = device.getData();
        data.put(Device.ACCOUNT_ID, device.getAccount().getEmail());
        JSONObject jsonObject = new JSONObject(data);
        if (Log.isInfoEnabled()) Log.info("Device.jsonObject=" + jsonObject);
        RestHttpResponseHandler responseHandler = new RestHttpResponseHandler();
        HttpClient.postJson(context, Device.table_name, jsonObject, responseHandler);
        return responseHandler;
    }
}
