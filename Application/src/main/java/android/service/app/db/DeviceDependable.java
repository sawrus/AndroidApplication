package android.service.app.db;

import android.service.app.db.inventory.GenericDevice;

/**
 * Defines methods for entities which could be linked with watched device
 */
public interface DeviceDependable
{
    GenericDevice getDevice();
    void setDevice(GenericDevice device);
}
