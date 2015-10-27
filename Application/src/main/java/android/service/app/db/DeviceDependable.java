package android.service.app.db;

import android.service.app.db.inventory.Device;

/**
 * Defines methods for entities which could be linked with watched device
 */
public interface DeviceDependable
{
    Device getDevice();
    void setDevice(Device device);
}
