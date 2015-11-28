package android.service.app.json;

import android.service.app.utils.AndroidUtils;
import android.service.app.utils.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

public class RestHttpJsonResponseHandler extends JsonHttpResponseHandler
{
    private volatile boolean isSuccessResponse = true;

    protected boolean isSuccessResponse()
    {
        return isSuccessResponse;
    }

    private void setIsSuccessResponse(boolean isSuccessResponse)
    {
        this.isSuccessResponse = isSuccessResponse;
    }

    private JSONObject jsonObject = null;
    private JSONArray jsonArray = null;

    public JSONObject getJsonObject()
    {
        return jsonObject;
    }

    public JSONArray getJsonArray()
    {
        return jsonArray;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject response)
    {
        Log.info("statusCode: " + statusCode);
        Log.info("headers: " + Arrays.toString(headers));
        Log.info("response: " + response);

        jsonObject = response;
        super.onSuccess(statusCode, headers, response);
        setIsSuccessResponse(true);

        runActionIfNeeded(response);
    }


    public interface AfterCompleteAction
    {
        Object execute(Object response);
    }

    private AfterCompleteAction action = null;

    public void setAction(AfterCompleteAction action)
    {
        this.action = action;
    }

    private void runActionIfNeeded(Object responseString)
    {

        Log.info("before run action: " + action);

        Object result = null;
        try
        {
            if (action != null)
            {
                result = action.execute(responseString);
            }
        }
        catch (Exception e)
        {
            Log.error(e);
        }

        Log.info("after run action, result: " + result);
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONArray response)
    {
        Log.info("statusCode: " + statusCode);
        Log.info("headers: " + Arrays.toString(headers));
        Log.info("response: " + response);

        jsonArray = response;
        super.onSuccess(statusCode, headers, response);
        setIsSuccessResponse(true);

        runActionIfNeeded(response);
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, String responseString)
    {
        Log.info("statusCode: " + statusCode);
        Log.info("headers: " + Arrays.toString(headers));
        Log.info("responseString: " + responseString);
        super.onSuccess(statusCode, headers, responseString);
        setIsSuccessResponse(true);

        runActionIfNeeded(responseString);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse)
    {
        Log.error("statusCode: " + statusCode);
        Log.error("headers: " + Arrays.toString(headers));
        Log.error("throwable: " + throwable.getMessage());
        Log.error("errorResponse: " + errorResponse);
        AndroidUtils.handleExceptionWithoutThrow(throwable);
        setIsSuccessResponse(false);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse)
    {
        Log.error("statusCode: " + statusCode);
        Log.error("headers: " + Arrays.toString(headers));
        Log.error("throwable: " + throwable.getMessage());
        Log.error("errorResponse: " + errorResponse);
        AndroidUtils.handleExceptionWithoutThrow(throwable);
        setIsSuccessResponse(false);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable)
    {
        Log.error("statusCode: " + statusCode);
        Log.error("headers: " + Arrays.toString(headers));
        Log.error("throwable: " + throwable.getMessage());
        Log.error("responseString: " + responseString);
        AndroidUtils.handleExceptionWithoutThrow(throwable);
        setIsSuccessResponse(false);
    }
}
