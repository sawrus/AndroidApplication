package android.service.app.db.data.impl;

import android.service.app.db.GenericDatabase;
import android.service.app.db.data.GenericData;
import android.service.app.db.sqllite.SqlLiteData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public abstract class Data<T extends GenericData> extends SqlLiteData<T> implements GenericData
{
    private int id = GenericDatabase.EMPTY_DATA;
    private String description = "";
    private String created_when = getCoordinatedUniversalDateTime();
    private String timezone = TimeZone.getDefault().getID() ;

    public Integer getId()
    {
        return id;
    }

    @Override
    public void setId(Integer id)
    {
        this.id = id;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public String getTimezone()
    {
        return timezone;
    }

    @Override
    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }

    @Override
    public String getCreatedWhen()
    {
        return created_when;
    }

    @Override
    public void setCreatedWhen(String created_when)
    {
        this.created_when = created_when;
    }

    private String getCoordinatedUniversalDateTime()
    {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone(GMT_TIME_ZONE));
        Date date = c.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }

    public boolean isEmpty()
    {
        return GenericDatabase.EMPTY_DATA == getId();
    }

    @Override
    public Map<String, Object> getData()
    {
        //checkOnEmptyAndThrowException();
        return new LinkedHashMap<String, Object>()
        {
            //{put(ID, getId());}
            {put(DESCRIPTION, getDescription());}
            {put(TIMEZONE, getTimezone());}
            {put(CREATED_WHEN, getCreatedWhen());}
        };
    }

    @Override
    public String toString()
    {
        return "Data{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", created_when='" + created_when + '\'' +
                ", timezone='" + timezone + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Data<?> data = (Data<?>) o;

        return getId() == data.getId();

    }

    @Override
    public int hashCode()
    {
        return getId();
    }

    public native String a();
    public native String b();
    public native String c();
    public native String d();
    public native String e();
    public native String f();
    public native String g();
    public native String h();
    public native String i();
    public native String k();

    static {
        System.loadLibrary("Application");
    }
}
