package android.service.app.db;

import android.service.app.db.data.GenericGps;
import android.service.app.db.data.GenericMessage;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.data.GenericAccount;

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
