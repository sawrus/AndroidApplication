package android.service.app.db.data.impl;

import android.database.Cursor;
import android.service.app.db.Data;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.DeviceDependable;
import android.service.app.db.data.GenericGps;
import android.service.app.db.inventory.impl.Device;
import android.service.app.db.inventory.GenericDevice;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Gps extends Data<GenericGps> implements DeviceDependable, GenericGps
{
    private int device_id = -3;
    private double latitude = -3;
    private double longitude = -3;
    private GenericDevice device = new Device();

    private static final String table_name = "gps";
    public static final String DEVICE_ID = "device_id";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    private static final Map<String, String> fields = Collections.unmodifiableMap(new LinkedHashMap<String, String>()
    {
        {
            put(ID, INTEGER_PRIMARY_KEY);
        }

        {
            put(LATITUDE, DOUBLE);
        }

        {
            put(LONGITUDE, DOUBLE);
        }

        {
            put(DEVICE_ID, INTEGER);
        }
    });

    public Gps()
    {
        super();
    }

    public Gps(int device_id, double latitude, double longitude)
    {
        this.device_id = device_id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GenericDevice getDevice()
    {
        return device;
    }

    public void setDevice(GenericDevice device)
    {
        this.device = device;
    }

    @Override
    public String generateCreateTableScript()
    {
        return generateCreateTableScript(getTableName(), fields);
    }

    @Override
    public String generateDropTableScript()
    {
        return generateDropTableScript(getTableName());
    }

    @Override
    protected Set<String> getFields()
    {
        return fields.keySet();
    }

    @Override
    public String getTableName()
    {
        return table_name;
    }

    @Override
    public int getDeviceId()
    {
        return device_id;
    }

    @Override
    public double getLatitude()
    {
        return latitude;
    }

    @Override
    public double getLongitude()
    {
        return longitude;
    }

    @Override
    public void setDeviceId(int device_id)
    {
        this.device_id = device_id;
    }

    @Override
    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    @Override
    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    @Override
    public Map<String, Object> getData()
    {
        final Map<String, Object> data = super.getData();
        return new LinkedHashMap<String, Object>()
        {
            {put(LATITUDE, getLatitude());}
            {put(LONGITUDE, getLongitude());}
            {put(DEVICE_ID, getDeviceId());}
            {putAll(data);}
        };
    }

    @NonNull
    protected GenericGps getDataFromCursor(Cursor cursor)
    {
        Gps data = new Gps();
        data.setId(cursor.getInt(cursor.getColumnIndex(ID)));
        data.setDeviceId(cursor.getInt(cursor.getColumnIndex(DEVICE_ID)));
        data.setLongitude(cursor.getDouble(cursor.getColumnIndex(LONGITUDE)));
        data.setLatitude(cursor.getDouble(cursor.getColumnIndex(LATITUDE)));
        fillGenericByCursor(data, cursor);
        return data;
    }

    @Override
    protected GenericGps emptyData()
    {
        return new Gps();
    }

    @Override
    public String toString()
    {
        return "Gps{" +
                "device_id=" + device_id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", device=" + device +
                '}' + " - " + super.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Gps gps = (Gps) o;

        if (device_id != gps.device_id) return false;
        if (Double.compare(gps.getLatitude(), getLatitude()) != 0) return false;
        return Double.compare(gps.getLongitude(), getLongitude()) == 0;

    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        long temp;
        result = 31 * result + device_id;
        temp = Double.doubleToLongBits(getLatitude());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getLongitude());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
