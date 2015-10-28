package android.service.app.json;

import android.service.app.utils.AndroidUtils;
import android.service.app.utils.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

public class RestHttpResponseHandler extends JsonHttpResponseHandler
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

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse)
    {
        Log.error("statusCode=" + statusCode);
        Log.error("headers=" + Arrays.toString(headers));
        Log.error("throwable=" + throwable.getMessage());
        Log.error("errorResponse=" + errorResponse);
        AndroidUtils.handleExceptionWithoutThrow(throwable);
        setIsSuccessResponse(false);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse)
    {
        Log.error("statusCode=" + statusCode);
        Log.error("headers=" + Arrays.toString(headers));
        Log.error("throwable=" + throwable.getMessage());
        Log.error("errorResponse=" + errorResponse);
        AndroidUtils.handleExceptionWithoutThrow(throwable);
        setIsSuccessResponse(false);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable)
    {
        Log.error("statusCode=" + statusCode);
        Log.error("headers=" + Arrays.toString(headers));
        Log.error("throwable=" + throwable.getMessage());
        Log.error("responseString=" + responseString);
        AndroidUtils.handleExceptionWithoutThrow(throwable);
        setIsSuccessResponse(false);
    }
}
