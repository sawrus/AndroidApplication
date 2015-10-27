package android.service.app.db;

import android.service.app.db.data.GenericGps;
import android.service.app.db.data.GenericMessage;
import android.service.app.db.data.impl.Gps;
import android.service.app.db.data.impl.Message;
import android.service.app.db.inventory.GenericDevice;
import android.service.app.db.inventory.impl.Device;
import android.service.app.db.user.Account;
import android.service.app.db.user.GenericAccount;

import java.util.Set;

public interface DataBridge<Criteria, ResponseHandler>
{
    Set<GenericMessage> getMessages(Criteria criteria);
    ResponseHandler postMessages(Set<GenericMessage> messages);

    Set<GenericGps> getCoordinates(Criteria criteria);
    ResponseHandler postGps(Set<GenericGps> gpsSets);

    GenericAccount getAccount(Criteria criteria);
    ResponseHandler postAccount(GenericAccount account);

    Set<GenericDevice> getDevices(Criteria criteria);
    ResponseHandler postDevice(GenericDevice device);
}
