package android.service.app.rest.impl;

import android.service.app.utils.AndroidUtils;
import android.service.app.utils.Log;

import com.loopj.android.http.TextHttpResponseHandler;

import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

public class RestHttpTextResponseHandler extends TextHttpResponseHandler
{
    private volatile boolean isSuccessResponse = true;

    private String responseString = "";

    public String getResponseString()
    {
        return responseString;
    }

    protected boolean isSuccessResponse()
    {
        return isSuccessResponse;
    }

    private void setIsSuccessResponse(boolean isSuccessResponse)
    {
        this.isSuccessResponse = isSuccessResponse;
    }

    public interface AfterCompleteAction
    {
        Object execute(String response);
    }

    private AfterCompleteAction action = null;

    public void setAction(AfterCompleteAction action)
    {
        this.action = action;
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable)
    {
        Log.error("statusCode: " + statusCode);
        Log.error("headers: " + Arrays.toString(headers));
        Log.error("responseString: " + responseString);
        AndroidUtils.handleExceptionWithoutThrow(throwable);
        setIsSuccessResponse(false);

        this.responseString = responseString;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, String responseString)
    {
        if (Log.isInfoEnabled())
        {
            Log.info("statusCode: " + statusCode);
            Log.info("headers: " + Arrays.toString(headers));
            Log.info("responseString: " + responseString);
        }

        this.responseString = responseString;
        setIsSuccessResponse(true);

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
}
