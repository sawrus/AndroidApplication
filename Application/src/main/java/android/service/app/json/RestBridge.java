package android.service.app.json;

import android.content.Context;
import android.service.app.db.data.impl.Data;
import android.service.app.db.DataBridge;
import android.service.app.db.sqllite.SqlLiteDatabase;
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
    public Set<GenericMessage> getMessages(DataFilter s)
    {
        final Set<GenericMessage> messages = new LinkedHashSet<>();
        //used only for testing
        SqlLiteDatabase.DatabaseWork databaseWork = new SqlLiteDatabase.DatabaseWork(context){
            @Override
            public Object execute()
            {
                messages.addAll(messages().getActualBySync());
                return null;
            }
        };
        databaseWork.runInTransaction();
        //used only for testing

        setIsSuccessLastResponse(true);        
        return messages;
    }

    @Override
    public Set<GenericGps> getCoordinates(DataFilter s)
    {
        final Set<GenericGps> coordinates = new LinkedHashSet<>();
        //used only for testing
        SqlLiteDatabase.DatabaseWork databaseWork = new SqlLiteDatabase.DatabaseWork(context){
            @Override
            public Object execute()
            {
                coordinates.addAll(coordinates().getActualBySync());
                return null;
            }
        };
        databaseWork.runInTransaction();
        //used only for testing

        setIsSuccessLastResponse(true);
        return coordinates;
    }

    @Override
    public GenericAccount getAccount(DataFilter emailFilter)
    {
        setIsSuccessLastResponse(true);
        return new Account(emailFilter.getFilter());
    }

    @Override
    public boolean checkAccountOnExist(DataFilter email)
    {
        setIsSuccessLastResponse(true);
        //used only for testing
        return false;
    }

    @Override
    public Set<GenericDevice> getDevices(DataFilter s)
    {
        final Set<GenericDevice> devices = new LinkedHashSet<>();
        //used only for testing
        SqlLiteDatabase.DatabaseWork databaseWork = new SqlLiteDatabase.DatabaseWork(context){
            @Override
            public Object execute()
            {
                devices.addAll(devices().getActualBySync());
                return null;
            }
        };
        databaseWork.runInTransaction();
        //used only for testing

        setIsSuccessLastResponse(true);
        return devices;
    }

    @Override
    public RestHttpResponseHandler postMessages(Set<GenericMessage> messages)
    {
        RestHttpResponseHandler responseHandler = new RestHttpResponseHandler();
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

        setIsSuccessLastResponse(responseHandler.isSuccessResponse());
        return responseHandler;
    }

    private void setSyncId(GenericData bean, Map<String, Object> data)
    {
        data.put(Data.SYNCID, bean.getId());
    }

    @Override
    public RestHttpResponseHandler postGps(Set<GenericGps> gpsSets)
    {
        RestHttpResponseHandler responseHandler = new RestHttpResponseHandler();
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
        setIsSuccessLastResponse(responseHandler.isSuccessResponse());
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
        setIsSuccessLastResponse(responseHandler.isSuccessResponse());
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
        setIsSuccessLastResponse(responseHandler.isSuccessResponse());
        return responseHandler;
    }
}
