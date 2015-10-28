package android.service.app.db.stub;

import android.database.Cursor;
import android.service.app.db.data.impl.Data;
import android.service.app.db.data.GenericData;
import android.service.app.db.data.GenericGps;
import android.service.app.db.data.GenericMessage;
import android.service.app.db.data.impl.Gps;
import android.service.app.db.data.impl.Message;
import android.service.app.db.data.impl.Device;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.data.GenericSync;
import android.service.app.db.data.impl.Sync;
import android.service.app.db.data.impl.Account;
import android.service.app.db.data.GenericAccount;

import java.util.Set;

public abstract class DataStub<T extends GenericData> extends Data<T> implements GenericData
{
    public static final class EmptyData extends Data<GenericData>
    {
        @Override
        public String generateCreateTableScript()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String generateDropTableScript()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public GenericData emptyData()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public GenericData getDataFromCursor(Cursor cursor)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public Set<String> getFields()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getTableName()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }
    }

    public static final class AccountStub extends Account implements GenericAccount {
        @Override
        public void setEmail(String email)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getEmail()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setId(Integer id)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getDescription()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setDescription(String description)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getTimezone()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setTimezone(String timezone)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getCreatedWhen()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setCreatedWhen(String created_when)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public boolean isEmpty()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public Integer getId()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }
    }

    public static final class DeviceStub extends Device implements GenericDevice{
        @Override
        public void setName(String name)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setAccountId(int account_id)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getName()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public int getAccountId()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public Account getAccount()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setId(Integer id)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getDescription()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setDescription(String description)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getTimezone()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setTimezone(String timezone)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getCreatedWhen()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setCreatedWhen(String created_when)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public boolean isEmpty()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public Integer getId()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }
    }

    public static final class MessageStub extends Message implements GenericMessage{
        @Override
        public void setPhone(String phone)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setIncoming(boolean incoming)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setData(String data)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setDeviceId(int device_id)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getPhone()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public boolean isIncoming()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getText()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public int getDeviceId()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public Device getDevice()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setDevice(GenericDevice device)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setId(Integer id)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getDescription()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setDescription(String description)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getTimezone()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setTimezone(String timezone)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getCreatedWhen()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setCreatedWhen(String created_when)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public boolean isEmpty()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public Integer getId()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }
    }
    public static final class GpsStub extends Gps implements GenericGps {
        @Override
        public int getDeviceId()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public double getLatitude()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public double getLongitude()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setDeviceId(int device_id)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setLatitude(double latitude)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setLongitude(double longitude)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public GenericDevice getDevice()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setDevice(GenericDevice device)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setId(Integer id)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getDescription()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setDescription(String description)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getTimezone()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setTimezone(String timezone)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getCreatedWhen()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setCreatedWhen(String created_when)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public Integer getId()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public boolean isEmpty()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }
    }
    public static final class SyncStub extends Sync implements GenericSync {
        @Override
        public int getAccountId()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public int getSyncId()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getTable()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setAccountId(int account_id)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setSyncId(int sync_id)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setTable(String table)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setId(Integer id)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getDescription()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setDescription(String description)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getTimezone()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setTimezone(String timezone)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public String getCreatedWhen()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public void setCreatedWhen(String created_when)
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public boolean isEmpty()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }

        @Override
        public Integer getId()
        {
            throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
        }
    }

    @Override
    public String generateCreateTableScript()
    {
        throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
    }

    @Override
    public String generateDropTableScript()
    {
        throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
    }

    @Override
    public T getDataFromCursor(Cursor cursor)
    {
        throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
    }

    @Override
    protected void fillGenericByCursor(GenericData data, Cursor cursor)
    {
        throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
    }

    @Override
    public Set<String> getFields()
    {
        throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
    }

    @Override
    public String getTableName()
    {
        throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
    }

    @Override
    public void setId(Integer id)
    {
        throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
    }

    @Override
    public String getDescription()
    {
        throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
    }

    @Override
    public void setDescription(String description)
    {
        throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
    }

    @Override
    public String getTimezone()
    {
        throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
    }

    @Override
    public void setTimezone(String timezone)
    {
        throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
    }

    @Override
    public String getCreatedWhen()
    {
        throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
    }

    @Override
    public void setCreatedWhen(String created_when)
    {
        throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
    }

    @Override
    public boolean isEmpty()
    {
        throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
    }

    @Override
    public Integer getId()
    {
        throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
    }

    @Override
    public T emptyData()
    {
        throw new UnsupportedOperationException("stub mode, class = " + getClass().getSimpleName());
    }
}
