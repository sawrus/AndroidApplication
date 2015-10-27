package android.service.app.db.data;

import android.service.app.db.DeviceDependable;
import android.service.app.db.GenericData;

public interface GenericGps extends GenericData<GenericGps>, DeviceDependable
{
    int getDeviceId();
    double getLatitude();
    double getLongitude();
    void setDeviceId(int device_id);
    void setLatitude(double latitude);
    void setLongitude(double longitude);
}
