package android.service.app.json;

import android.service.app.db.DataBridge;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.data.Gps;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.db.user.Account;
import android.service.app.http.HttpClient;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.Set;

public class RestBridge implements DataBridge<String, JsonHttpResponseHandler>
{
    @Override
    public Set<Message> getMessages(String s)
    {
        return null;
    }

    @Override
    public JsonHttpResponseHandler postMessages(Set<Message> messages)
    {
        RequestParams requestParams = new RequestParams();
        JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler();
        HttpClient.post(DatabaseHelper.MESSAGE.getTableName(), requestParams, responseHandler);
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
