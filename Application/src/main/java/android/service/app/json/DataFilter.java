package android.service.app.json;

import com.loopj.android.http.RequestParams;

public enum DataFilter
{
    ALL,
    BY_ID,
    BY_VALUE,
    BY_DATE;

    private String filter;

    public String getFilter()
    {
        return filter;
    }

    public DataFilter setFilter(String filter)
    {
        this.filter = filter;
        return this;
    }

    private RequestParams requestParams;

    public DataFilter setRequestParams(RequestParams requestParams)
    {
        this.requestParams = requestParams;
        return this;
    }

    public RequestParams getRequestParams()
    {
        return requestParams;
    }
}
