package android.service.app.db.data;

import android.service.app.db.DeviceDependable;
import android.service.app.db.GenericData;

public interface GenericMessage extends GenericData<GenericMessage>, DeviceDependable
{
    void setPhone(String phone);
    void setIncoming(boolean incoming);
    void setData(String data);
    void setDeviceId(int device_id);
    String getPhone();
    boolean isIncoming();
    String getText();
    int getDeviceId();
}
