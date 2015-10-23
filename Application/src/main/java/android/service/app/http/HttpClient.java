package android.service.app.http;

import android.service.app.db.DatabaseHelper;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public enum  HttpClient
{
    instance;

    public static final String HOST = DatabaseHelper.ACCOUNT.h();
    public static final String PORT = DatabaseHelper.ACCOUNT.i();
    public static final String PROTOCOL = DatabaseHelper.ACCOUNT.k();
    private static final String BASE_URL = PROTOCOL + "://" + HOST + ":" + PORT;

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
