package android.service.app.db.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.service.app.db.Data;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.inventory.Device;
import android.service.app.utils.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Message extends Data
{
    private int id = -1;
    private String address = "";
    private boolean incoming = false;
    private String data = "";
    private int device_id = -2;
    private String date = "";
    private Device device = new Device();

    private static final String table_name = "message";
    public static final String ID = "id";
    public static final String ADDRESS = "address";
    public static final String DATA = "data";
    public static final String DATE = "date";
    public static final String INCOMING = "incoming";
    public static final String DEVICE_ID = "device_id";
    private static final Map<String, String> fields = new LinkedHashMap<String, String>(){
        {put(ID, INTEGER_PRIMARY_KEY);}
        {put(ADDRESS, TEXT);}
        {put(INCOMING, INTEGER);}
        {put(DATA, TEXT);}
        {put(DATE, DATETIME);}
        {put(DEVICE_ID, INTEGER);}
    };

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
    public String generateCreateTableScript()
    {
        return generateCreateTableScript(table_name, fields);
    }

    @Override
    public String generateDropTableScript()
    {
        return generateDropTableScript(table_name);
    }

    @Override
    public Object insert(SQLiteDatabase database)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ADDRESS, getAddress());
        contentValues.put(INCOMING, isIncoming() ? 1 : 0);
        contentValues.put(DATA, getText());
        contentValues.put(DATE, getDate());
        contentValues.put(DEVICE_ID, getDeviceId());
        return insert(database, table_name, contentValues);
    }

    public Device getDevice()
    {
        return device;
    }

    public Map<String, Object> getData()
    {
        return new LinkedHashMap<String, Object>()
        {
            {put(ADDRESS, getAddress());}
            {put(INCOMING, isIncoming());}
            {put(DATA, getText());}
            {put(DATE, getDate());}
            {put(DEVICE_ID, getDeviceId());}
        };
    }

    public Set<Message> selectAllMessages(SQLiteDatabase database)
    {
        Cursor cursor = selectAll(database);
        Set<Message> messages = new LinkedHashSet<>();

        if (cursor.getCount() > 0)
        {
            final Device device = DatabaseHelper.DEVICE.selectFirstDevice(database);

            cursor.moveToFirst();

            do
            {
                Message message = new Message();
                message.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                message.setAddress(cursor.getString(cursor.getColumnIndex(ADDRESS)));
                message.setIncoming(cursor.getInt(cursor.getColumnIndex(INCOMING)) == 1);
                message.setData(cursor.getString(cursor.getColumnIndex(DATA)));
                message.setDate(cursor.getString(cursor.getColumnIndex(DATE)));
                message.setDeviceId(cursor.getInt(cursor.getColumnIndex(DEVICE_ID)));
                message.setDevice(device);
                messages.add(message);
                Log.v("message=" + message + "; cursor=" + cursor);
                if (!cursor.isLast()) cursor.moveToNext();
                else break;
            } while (!cursor.isClosed());
        }

        return messages;
    }


    public Message(){}

    public Message(String address, boolean incoming, String data, int device_id)
    {
        this.address = address;
        this.incoming = incoming;
        this.data = data;
        this.device_id = device_id;
        this.date = getDateTime();
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public void setIncoming(boolean incoming)
    {
        this.incoming = incoming;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public void setDeviceId(int device_id)
    {
        this.device_id = device_id;
    }

    public void setDevice(Device device)
    {
        this.device = device;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public int getId()
    {
        return id;
    }

    public String getAddress()
    {
        return address;
    }

    public boolean isIncoming()
    {
        return incoming;
    }

    public String getText()
    {
        return data;
    }

    public int getDeviceId()
    {
        return device_id;
    }

    public String getDate()
    {
        return date;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (getId() != message.getId()) return false;
        if (isIncoming() != message.isIncoming()) return false;
        if (device_id != message.device_id) return false;
        if (!getAddress().equals(message.getAddress())) return false;
        if (!getData().equals(message.getData())) return false;
        return true;

    }

    @Override
    public int hashCode()
    {
        int result = getId();
        result = 31 * result + getAddress().hashCode();
        result = 31 * result + (isIncoming() ? 1 : 0);
        result = 31 * result + getData().hashCode();
        result = 31 * result + device_id;
        return result;
    }

    @Override
    public String toString()
    {
        return "Message{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", incoming=" + incoming +
                ", data='" + data + '\'' +
                ", device_id=" + device_id +
                ", date='" + date + '\'' +
                '}';
    }
}
