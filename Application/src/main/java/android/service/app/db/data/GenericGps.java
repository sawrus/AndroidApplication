package android.service.app.db.data;

public interface GenericGps extends GenericData, DeviceDependable
{
    int getDeviceId();
    double getLatitude();
    double getLongitude();
    void setDeviceId(int device_id);
    void setLatitude(double latitude);
    void setLongitude(double longitude);
}
