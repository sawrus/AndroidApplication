package android.service.app.json;

import android.content.Context;
import android.service.app.db.GenericDatabase;
import android.service.app.db.data.impl.Data;
import android.service.app.db.DataBridge;
import android.service.app.db.data.GenericData;
import android.service.app.db.data.GenericGps;
import android.service.app.db.data.GenericMessage;
import android.service.app.db.data.impl.Gps;
import android.service.app.db.data.impl.Message;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.data.impl.Device;
import android.service.app.db.data.impl.Account;
import android.service.app.db.data.GenericAccount;
import android.service.app.db.sqllite.SqlLiteDatabase;
import android.service.app.http.HttpClient;
import android.service.app.utils.AndroidUtils;
import android.service.app.utils.JsonUtils;
import android.service.app.utils.Log;
import android.support.annotation.NonNull;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class RestBridge implements DataBridge<DataFilter, AsyncHttpResponseHandler>
{
    private final Context context;

    public RestBridge(Context context)
    {
        this.context = context;
    }

    private boolean isSuccessLastResponse = true;

    public boolean isSuccessLastResponse()
    {
        return isSuccessLastResponse;
    }

    private void setIsSuccessLastResponse(boolean isSuccessLastResponse)
    {
        this.isSuccessLastResponse = isSuccessLastResponse;
    }

    @Override
    public Set<GenericMessage> getMessages(DataFilter filter)
    {
        RestHttpJsonResponseHandler responseHandler = newRestHttpJsonResponseHandlerInstance();
        HttpClient.get(Message.table_name, filter.getRequestParams(), responseHandler);
        if (!isSuccessLastResponse()) return Collections.emptySet();
        Set<JSONObject> jsonObjects = JsonUtils.getJsonObjects(responseHandler.getJsonArray());
        Set<GenericMessage> dataSet = new LinkedHashSet<>();
        for (JSONObject jsonObject: jsonObjects) dataSet.add(JsonUtils.getData(Message.class, jsonObject));
        return dataSet;
    }

    @Override
    public Set<GenericGps> getCoordinates(DataFilter filter)
    {
        RestHttpJsonResponseHandler responseHandler = newRestHttpJsonResponseHandlerInstance();
        HttpClient.get(Gps.table_name, filter.getRequestParams(), responseHandler);
        if (!isSuccessLastResponse()) return Collections.emptySet();
        Set<JSONObject> jsonObjects = JsonUtils.getJsonObjects(responseHandler.getJsonArray());
        Set<GenericGps> dataSet = new LinkedHashSet<>();
        for (JSONObject jsonObject: jsonObjects) dataSet.add(JsonUtils.getData(Gps.class, jsonObject));
        return dataSet;
    }

    @Override
    public GenericAccount getAccount(DataFilter emailFilter)
    {
        RestHttpJsonResponseHandler responseHandler = newRestHttpJsonResponseHandlerInstance();
        HttpClient.get(Account.table_name, emailFilter.getRequestParams(), responseHandler);
        if (!isSuccessLastResponse()) return new Account();
        Set<JSONObject> jsonObjects = JsonUtils.getJsonObjects(responseHandler.getJsonArray());
        return JsonUtils.getData(Account.class, jsonObjects.iterator().next());
    }

    @Override
    public boolean checkAccountOnExist(DataFilter emailFilter)
    {
        RestHttpJsonResponseHandler responseHandler = newRestHttpJsonResponseHandlerInstance();
        HttpClient.get(Account.table_name, emailFilter.getRequestParams(), responseHandler);
        if (!isSuccessLastResponse()) return false;
        JSONArray jsonArray = responseHandler.getJsonArray();
        if (Log.isInfoEnabled()) Log.info("jsonArray: " + jsonArray);
        Set<JSONObject> jsonObjects = JsonUtils.getJsonObjects(jsonArray);
        if (Log.isInfoEnabled()) Log.info("jsonObjects: " + jsonObjects);
        if (jsonObjects.isEmpty()) return false;
        else return jsonObjects.iterator().next().has("email");
    }

    @Override
    public Set<GenericDevice> getDevices(DataFilter filter)
    {
        RestHttpJsonResponseHandler responseHandler = newRestHttpJsonResponseHandlerInstance();
        RequestParams params = filter.getRequestParams();
        HttpClient.get(Device.table_name, params, responseHandler);
        if (!isSuccessLastResponse()) return Collections.emptySet();
        Set<JSONObject> jsonObjects = JsonUtils.getJsonObjects(responseHandler.getJsonArray());
        Set<GenericDevice> dataSet = new LinkedHashSet<>();
        for (JSONObject jsonObject: jsonObjects){
            String deviceId = jsonObject.toString();
            dataSet.add(new Device(deviceId, -2));
        }
        return dataSet;
    }

    @Override
    public AsyncHttpResponseHandler postMessages(Set<GenericMessage> messages)
    {
        RestHttpTextResponseHandler responseHandler = newRestHttpTextResponseHandlerInstance();

        Set<JSONObject> jsonObjects = new LinkedHashSet<>();
        for (GenericMessage message: messages)
        {
            Map<String, Object> data = message.getData();
            if (message.getDeviceId() == GenericDatabase.EMPTY_DATA) continue;
            GenericDevice device = message.getDevice();
            data.put(Message.DEVICE_ID, device.getDescription());
            setSyncId(message, data);

            jsonObjects.add(new JSONObject(data));
            responseHandler = newRestHttpTextResponseHandlerInstance();
        }

        if (Log.isInfoEnabled()) Log.info("Message.jsonObjects: " + jsonObjects);
        if (jsonObjects.isEmpty()) return null;
        HttpClient.postJson(context, Message.table_name, jsonObjects, responseHandler);
        setIsSuccessLastResponse(responseHandler.isSuccessResponse());
        return responseHandler;
    }

    @NonNull
    private RestHttpJsonResponseHandler newRestHttpJsonResponseHandlerInstance()
    {
        return new RestHttpJsonResponseHandler();
    }

    private void setSyncId(GenericData bean, Map<String, Object> data)
    {
        data.put(Data.SYNCID, bean.getId());
    }

    @Override
    public AsyncHttpResponseHandler postGps(Set<GenericGps> gpsSets)
    {
        RestHttpTextResponseHandler responseHandler = newRestHttpTextResponseHandlerInstance();

        Set<JSONObject> jsonObjects = new LinkedHashSet<>();
        for (GenericGps gps: gpsSets)
        {
            Map<String, Object> data = gps.getData();
            if (gps.getDeviceId() == GenericDatabase.EMPTY_DATA) continue;
            data.put(Gps.DEVICE_ID, gps.getDevice().getDescription());
            setSyncId(gps, data);
            jsonObjects.add(new JSONObject(data));
            responseHandler = newRestHttpTextResponseHandlerInstance();
        }

        if (Log.isInfoEnabled()) Log.info("Gps.jsonObjects: " + jsonObjects);
        if (jsonObjects.isEmpty()) return null;
        HttpClient.postJson(context, Gps.table_name, jsonObjects, responseHandler);
        setIsSuccessLastResponse(responseHandler.isSuccessResponse());
        return responseHandler;
    }

    @Override
    public AsyncHttpResponseHandler postAccount(GenericAccount account)
    {
        Map<String, Object> data = account.getData();
        JSONObject jsonObject = new JSONObject(data);
        if (Log.isInfoEnabled()) Log.info("Account.jsonObject: " + jsonObject);
        RestHttpTextResponseHandler responseHandler = newRestHttpTextResponseHandlerInstance();
        HttpClient.postJson(context, Account.table_name, jsonObject, responseHandler);
        setIsSuccessLastResponse(responseHandler.isSuccessResponse());
        return responseHandler;
    }

    @Override
    public AsyncHttpResponseHandler postDevice(GenericDevice device, String email)
    {
        Map<String, Object> data = device.getData();
        if (Log.isInfoEnabled()) Log.info("email: " + email);
        data.put("account", email);
        JSONObject jsonObject = new JSONObject(data);
        if (Log.isInfoEnabled()) Log.info("Device.jsonObject: " + jsonObject);
        RestHttpTextResponseHandler responseHandler = newRestHttpTextResponseHandlerInstance();
        responseHandler.setAction(new RestHttpTextResponseHandler.AfterCompleteAction() {
            @Override
            public Object execute(final String response)
            {
                SqlLiteDatabase.DatabaseWork databaseWork = new SqlLiteDatabase.DatabaseWork(context)
                {
                    @Override
                    public Object execute()
                    {
                        GenericAccount account = accounts().getFirst();
                        Device device = new Device(AndroidUtils.getDeviceName(), account.getId());
                        device.setDescription(response.substring(2, response.length()-2));
                        device.setId(insert(device));
                        return devices().getFirst();
                    }
                };

                return databaseWork.runInTransaction();
            }
        });

        HttpClient.postJson(context, Device.table_name, jsonObject, responseHandler);
        setIsSuccessLastResponse(responseHandler.isSuccessResponse());
        return responseHandler;
    }

    @NonNull
    private RestHttpTextResponseHandler newRestHttpTextResponseHandlerInstance()
    {
        return new RestHttpTextResponseHandler();
    }
}
