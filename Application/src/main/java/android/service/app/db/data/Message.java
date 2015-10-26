package android.service.app.db.data;

import android.database.Cursor;
import android.service.app.db.Data;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.DeviceDependable;
import android.service.app.db.inventory.Device;
import android.support.annotation.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Message extends Data<Message> implements DeviceDependable
{
    private String phone = "";
    private boolean incoming = false;
    private String data = "";
    private int device_id = -2;
    private Device device = new Device();

    private static final String table_name = "message";
    public static final String ADDRESS = "tofrom";
    public static final String DATA = "data";
    public static final String INCOMING = "incoming";
    public static final String DEVICE_ID = "device_id";
    private static final Map<String, String> fields = new LinkedHashMap<String, String>(){
        {put(ADDRESS, TEXT);}
        {put(INCOMING, INTEGER);}
        {put(DATA, TEXT);}
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

    public Device getDevice()
    {
        return device;
    }

    public Map<String, Object> getData()
    {
        final Map<String, Object> data = super.getData();
        return new LinkedHashMap<String, Object>()
        {
            {put(ADDRESS, getPhone());}
            {put(INCOMING, isIncoming());}
            {put(DATA, getText());}
            {put(DEVICE_ID, getDeviceId());}
            {putAll(data);}
        };
    }

    @NonNull
    public Message getDataFromCursor(Cursor cursor)
    {
        Message data = new Message();
        data.setPhone(cursor.getString(cursor.getColumnIndex(ADDRESS)));
        data.setIncoming(cursor.getInt(cursor.getColumnIndex(INCOMING)) == 1);
        data.setData(cursor.getString(cursor.getColumnIndex(DATA)));
        data.setDeviceId(cursor.getInt(cursor.getColumnIndex(DEVICE_ID)));
        fillGenericByCursor(data, cursor);
        return data;
    }

    @Override
    protected Message emptyData()
    {
        return DatabaseHelper.MESSAGE;
    }

    public Message(){
        super();
    }

    public Message(String phone, boolean incoming, String data, int device_id)
    {
        this.phone = phone;
        this.incoming = incoming;
        this.data = data;
        this.device_id = device_id;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
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

    public String getPhone()
    {
        return phone;
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

    @Override
    public String toString()
    {
        return "Message{" +
                "phone='" + phone + '\'' +
                ", incoming=" + incoming +
                ", text='" + data + '\'' +
                ", device_id=" + device_id +
                ", device=" + device +
                '}' + " - " + super.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Message message = (Message) o;

        if (isIncoming() != message.isIncoming()) return false;
        if (device_id != message.device_id) return false;
        if (!getPhone().equals(message.getPhone())) return false;
        return getText().equals(message.getText());

    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + getPhone().hashCode();
        result = 31 * result + (isIncoming() ? 1 : 0);
        result = 31 * result + getText().hashCode();
        result = 31 * result + device_id;
        return result;
    }
}
