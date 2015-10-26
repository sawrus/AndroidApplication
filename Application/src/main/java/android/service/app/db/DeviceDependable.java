package android.service.app.db;

import android.service.app.db.inventory.Device;

public interface DeviceDependable
{
    Device getDevice();
    void setDevice(Device device);
}
