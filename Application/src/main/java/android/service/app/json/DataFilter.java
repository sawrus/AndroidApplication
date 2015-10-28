package android.service.app.json;

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
}
