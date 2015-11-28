package android.service.app.json;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
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
import org.json.JSONException;
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
    public Set<GenericMessage> getMessages(final DataFilter filter, final GenericDevice device)
    {
        final RestHttpJsonResponseHandler responseHandler = newRestHttpJsonResponseHandlerInstance();
        responseHandler.setAction(new RestHttpJsonResponseHandler.AfterCompleteAction() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public Object execute(Object response)
            {
                if (Log.isInfoEnabled()) Log.info("messages.response: " + response);
                Set<JSONObject> jsonObjects = JsonUtils.getJsonObjects(responseHandler.getJsonArray());
                final Set<GenericMessage> dataSet = new LinkedHashSet<>();
                for (JSONObject jsonObject: jsonObjects){
                    Message data = JsonUtils.getData(Message.class, jsonObject);
                    data.setDeviceId(device.getId());
                    dataSet.add(data);
                }
                if (!dataSet.isEmpty())
                {
                    SqlLiteDatabase.DatabaseWork databaseWork = new SqlLiteDatabase.DatabaseWork(filter.getContext())
                    {
                        @Override
                        public Object execute()
                        {
                            return insert(dataSet);
                        }
                    };
                    databaseWork.runInTransaction();
                }
                return dataSet;
            }
        });

        HttpClient.get(Message.table_name, filter.getRequestParams(), responseHandler);
        return Collections.emptySet();
    }

    @Override
    public Set<GenericGps> getCoordinates(final DataFilter filter, final GenericDevice device)
    {
        final RestHttpJsonResponseHandler responseHandler = newRestHttpJsonResponseHandlerInstance();
        responseHandler.setAction(new RestHttpJsonResponseHandler.AfterCompleteAction() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public Object execute(Object response)
            {
                if (Log.isInfoEnabled()) Log.info("coordinates.response: " + response);
                Set<JSONObject> jsonObjects = JsonUtils.getJsonObjects(responseHandler.getJsonArray());
                final Set<GenericGps> dataSet = new LinkedHashSet<>();
                for (JSONObject jsonObject: jsonObjects){
                    Gps data = JsonUtils.getData(Gps.class, jsonObject);
                    data.setDeviceId(device.getId());
                    dataSet.add(data);
                }
                if (!dataSet.isEmpty())
                {
                    SqlLiteDatabase.DatabaseWork databaseWork = new SqlLiteDatabase.DatabaseWork(filter.getContext())
                    {
                        @Override
                        public Object execute()
                        {
                            return insert(dataSet);
                        }
                    };
                    databaseWork.runInTransaction();
                }
                return dataSet;
            }
        });
        HttpClient.get(Gps.table_name, filter.getRequestParams(), responseHandler);
        return Collections.emptySet();
    }

    @Override
    public GenericAccount getAccount(DataFilter emailFilter)
    {
        JSONArray jsonArray;
        try
        {
            jsonArray = new JSONArray(HttpClient.get(Account.table_name, "account", emailFilter.getFilter()));
        }
        catch (JSONException e)
        {
            throw new IllegalStateException(e);
        }
        if (Log.isInfoEnabled()) Log.info("jsonArray: " + jsonArray);
        if (jsonArray.length() == 0) return new Account();
        Set<JSONObject> jsonObjects = JsonUtils.getJsonObjects(jsonArray);
        return JsonUtils.getData(Account.class, jsonObjects.iterator().next());
    }

    @Override
    public boolean checkAccountOnExist(DataFilter emailFilter)
    {
        JSONArray jsonArray = null;
        try
        {
            jsonArray = new JSONArray(HttpClient.get(Account.table_name, "account", emailFilter.getFilter()));
        }
        catch (JSONException e)
        {
            throw new IllegalStateException(e);
        }
        if (Log.isInfoEnabled()) Log.info("jsonArray: " + jsonArray);
        if (jsonArray.length() == 0) return false;
        Set<JSONObject> jsonObjects = JsonUtils.getJsonObjects(jsonArray);
        return jsonObjects.iterator().next().has("email");
    }

    @Override
    public Set<GenericDevice> getDevices(DataFilter filter)
    {
        String deviceIds = HttpClient.get(Device.table_name, "account", filter.getFilter());
        deviceIds = deviceIds.substring(1, deviceIds.length()-1);
        String[] ids = deviceIds.split(",");
        Set<GenericDevice> dataSet = new LinkedHashSet<>();
        for (String id1 : ids)
        {
            String id = id1;
            id = id.substring(1, id.length() - 1);
            Device device = new Device(id, -2);
            device.setDescription(id);
            dataSet.add(device);
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
