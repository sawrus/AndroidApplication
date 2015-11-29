package android.service.app.http;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.os.StrictMode;
import android.service.app.db.sqllite.impl.SqlLiteDatabaseHelper;
import android.service.app.utils.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

import cz.msebera.android.httpclient.entity.StringEntity;

public enum HttpClient
{
    instance;

    private static final String HOST = SqlLiteDatabaseHelper.EMPTY.h();
    private static final String PORT = SqlLiteDatabaseHelper.EMPTY.i();
    private static final String PROTOCOL = SqlLiteDatabaseHelper.EMPTY.k();
    private static final String BASE_URL = PROTOCOL + "://" + HOST + ":" + PORT + "/";
    public static final String CHARSET = "charset=utf-8";
    public static final int MAX_CONNECTIONS = 1;
    public static final int RETRIES = 1;
    public static final int TIMEOUT = 1000;
    private static final SyncHttpClient syncHttpClient = new SyncHttpClient();
    private static final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler)
    {
        String absoluteUrl = getAbsoluteUrl(url);
        if (Log.isInfoEnabled()) Log.info("URL: " + AsyncHttpClient.getUrlWithQueryString(true, absoluteUrl, params));
        getClient().get(absoluteUrl, params, responseHandler);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static String get(final String _url, String key, String value)
    {
        try
        {
            allowOpenConnectionInMainThread();
            String address = BASE_URL + _url + "?" + key + "=" + value;
            URL url = new URL(address);
            URLConnection urlConnection = url.openConnection();
            String data = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            while ((inputLine = reader.readLine()) != null) data += inputLine;
            reader.close();
            if (data.isEmpty())
                throw new IllegalStateException("empty response by account");
            else
                return data;
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static void allowOpenConnectionInMainThread()
    {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler)
    {
        getClient().post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void postJson(Context context, String url, JSONObject data, AsyncHttpResponseHandler responseHandler)
    {
        try
        {
            String contentType = RequestParams.APPLICATION_JSON + "; " + CHARSET;
            getClient().post(context, getAbsoluteUrl(url), new StringEntity(String.valueOf(data)), contentType, responseHandler);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void postJson(Context context, String url, Set<JSONObject> data, AsyncHttpResponseHandler responseHandler)
    {
        try
        {
            String contentType = RequestParams.APPLICATION_JSON + "; " + CHARSET;
            getClient().post(context, getAbsoluteUrl(url), new StringEntity(String.valueOf(data)), contentType, responseHandler);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static AsyncHttpClient getClient()
    {
        AsyncHttpClient client;
        // Return the synchronous HTTP client when the thread is not prepared
        if (Looper.myLooper() == null)
            client = syncHttpClient;
        else
            client = asyncHttpClient;

        configureClient(client);

        return client;
    }

    private static SyncHttpClient getSyncClient()
    {
        SyncHttpClient client = syncHttpClient;

        configureClient(client);

        return syncHttpClient;
    }

    private static void configureClient(AsyncHttpClient client)
    {
        client.setMaxConnections(MAX_CONNECTIONS);
        client.setMaxRetriesAndTimeout(RETRIES, TIMEOUT);
    }


    private static String getAbsoluteUrl(String relativeUrl)
    {
        return BASE_URL + relativeUrl;
    }
}
