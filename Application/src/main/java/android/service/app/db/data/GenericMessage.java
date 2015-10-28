package android.service.app.db.data;

public interface GenericMessage extends GenericData, DeviceDependable
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
