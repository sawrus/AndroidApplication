package android.service.app.db;

import android.service.app.db.inventory.GenericDevice;

public interface DeviceDependable
{
    GenericDevice getDevice();
    void setDevice(GenericDevice device);
}
