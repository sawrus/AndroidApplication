package android.service.app.json;

import android.content.Context;
import android.service.app.db.DataBridge;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.data.Gps;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.db.user.Account;
import android.service.app.http.HttpClient;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

public class RestBridge implements DataBridge<String, JsonHttpResponseHandler>
{
    private final Context context;

    public RestBridge(Context context)
    {
        this.context = context;
    }

    @Override
    public Set<Message> getMessages(String s)
    {
        return null;
    }

    @Override
    public JsonHttpResponseHandler postMessages(Set<Message> messages)
    {
        JsonHttpResponseHandler responseHandler = null;
        for (Message message: messages)
        {
            Map<String, Object> data = message.getData();
            //todo: need to refactor
            data.put(Message.DEVICE_ID, message.getDevice().getName());

            JSONObject jsonMessage = new JSONObject(data);
            responseHandler = new JsonHttpResponseHandler();
            HttpClient.postJson(context, DatabaseHelper.MESSAGE.getTableName(), jsonMessage, responseHandler);
        }

        return responseHandler;
    }

    @Override
    public Set<Gps> getGps(String s)
    {
        return null;
    }

    @Override
    public JsonHttpResponseHandler postGps(Set<Gps> gpsSets)
    {
        return null;
    }

    @Override
    public Account getAccount(String s)
    {
        return null;
    }

    @Override
    public JsonHttpResponseHandler postAccount(Account account)
    {
        return null;
    }

    @Override
    public Device getDevice(String s)
    {
        return null;
    }

    @Override
    public JsonHttpResponseHandler postDevice(Device device)
    {
        return null;
    }
}
