package android.service.app.json;

import android.service.app.utils.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

public class RestHttpResponseHandler extends JsonHttpResponseHandler
{
    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse)
    {
        Log.v("statusCode=" + statusCode);
        Log.v("headers=" + Arrays.asList(headers));
        Log.v("throwable=" + throwable.getMessage());
        Log.v("errorResponse=" + errorResponse);
        throw new RuntimeException(throwable);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse)
    {
        Log.v("statusCode=" + statusCode);
        Log.v("headers=" + Arrays.asList(headers));
        Log.v("throwable=" + throwable.getMessage());
        Log.v("errorResponse=" + errorResponse);
        throw new RuntimeException(throwable);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable)
    {
        Log.v("statusCode=" + statusCode);
        Log.v("headers=" + Arrays.asList(headers));
        Log.v("throwable=" + throwable.getMessage());
        Log.v("responseString=" + responseString);
        throw new RuntimeException(throwable);
    }
}
