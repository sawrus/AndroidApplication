package android.service.app.http;

import android.content.Context;
import android.os.Looper;
import android.service.app.db.DatabaseHelper;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.StringEntity;

public enum  HttpClient
{
    instance;

    private static final String HOST = DatabaseHelper.ACCOUNT.h();
    private static final String PORT = DatabaseHelper.ACCOUNT.i();
    private static final String PROTOCOL = DatabaseHelper.ACCOUNT.k();
    private static final String BASE_URL = PROTOCOL + "://" + HOST + ":" + PORT + "/";
    public static final String CHARSET = "charset=utf-8";
    private static AsyncHttpClient syncHttpClient= new SyncHttpClient();
    private static AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void postJson(Context context, String url, JSONObject data, AsyncHttpResponseHandler responseHandler)
    {
        try
        {
            String contentType = RequestParams.APPLICATION_JSON + "; " + CHARSET;
            getClient().post(context, getAbsoluteUrl(url), new StringEntity(String.valueOf(data)), contentType, responseHandler);
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static AsyncHttpClient getClient()
    {
        // Return the synchronous HTTP client when the thread is not prepared
        if (Looper.myLooper() == null)
            return syncHttpClient;
        return asyncHttpClient;
    }


    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
