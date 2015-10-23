package android.service.app.db.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.service.app.db.Data;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.inventory.Device;
import android.service.app.utils.Log;
import android.view.DragEvent;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Gps extends Data
{
    private int id = -1;
    private int device_id = -3;
    private double latitude = -3;
    private double longitude = -3;
    private String date = "";
    private Device device = new Device();

    private static final String table_name = "gps";
    public static final String ID = "id";
    public static final String DATE = "date";
    public static final String DEVICE_ID = "device_id";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    private static final Map<String, String> fields = new LinkedHashMap<String, String>(){
        {put(ID, INTEGER_PRIMARY_KEY);}
        {put(LATITUDE, DOUBLE);}
        {put(LONGITUDE, DOUBLE);}
        {put(DATE, DATETIME);}
        {put(DEVICE_ID, INTEGER);}
    };

    public Gps()
    {
    }

    public Gps(int device_id, double latitude, double longitude)
    {
        this.device_id = device_id;
        this.date = getDateTime();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Device getDevice()
    {
        return device;
    }

    public void setDevice(Device device)
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
    public int getId()
    {
        return id;
    }

    public int getDeviceId()
    {
        return device_id;
    }

    public String getDate()
    {
        return date;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setDeviceId(int device_id)
    {
        this.device_id = device_id;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    @Override
    public Map<String, Object> getData()
    {
        return new LinkedHashMap<String, Object>()
        {
            {put(LATITUDE, getLatitude());}
            {put(LONGITUDE, getLongitude());}
            {put(DATE, getDate());}
            {put(DEVICE_ID, getDeviceId());}
        };
    }

    public Set<Gps> selectAllGps(SQLiteDatabase database)
    {
        Cursor cursor = selectAll(database);
        Set<Gps> gpsSet = new LinkedHashSet<>();

        if (cursor.getCount() > 0)
        {
            final Device device = DatabaseHelper.DEVICE.selectFirstDevice(database);
            cursor.moveToFirst();

            do
            {
                Gps gps = new Gps();
                gps.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                gps.setDate(cursor.getString(cursor.getColumnIndex(DATE)));
                gps.setDeviceId(cursor.getInt(cursor.getColumnIndex(DEVICE_ID)));
                gps.setLongitude(cursor.getDouble(cursor.getColumnIndex(LONGITUDE)));
                gps.setLatitude(cursor.getDouble(cursor.getColumnIndex(LATITUDE)));
                gps.setDevice(device);

                gpsSet.add(gps);
                Log.v("gps=" + gps + "; cursor=" + cursor);
                if (!cursor.isLast()) cursor.moveToNext();
                else break;
            } while (!cursor.isClosed());
        }

        return gpsSet;
    }

    @Override
    public Object insert(SQLiteDatabase database)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LATITUDE, getLatitude());
        contentValues.put(LONGITUDE, getLongitude());
        contentValues.put(DATE, getDate());
        contentValues.put(DEVICE_ID, getDeviceId());
        return insert(database, table_name, contentValues);
    }

    @Override
    public String toString()
    {
        return "Gps{" +
                "id=" + id +
                ", device_id=" + device_id +
                ", date='" + date + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Gps gps = (Gps) o;

        if (getId() != gps.getId()) return false;
        if (device_id != gps.device_id) return false;
        if (Double.compare(gps.getLatitude(), getLatitude()) != 0) return false;
        if (Double.compare(gps.getLongitude(), getLongitude()) != 0) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = getId();
        result = 31 * result + device_id;
        temp = Double.doubleToLongBits(getLatitude());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getLongitude());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
