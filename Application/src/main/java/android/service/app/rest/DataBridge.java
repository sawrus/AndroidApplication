package android.service.app.rest;

import android.service.app.db.data.GenericGps;
import android.service.app.db.data.GenericMessage;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.data.GenericAccount;

import java.util.Set;

public interface DataBridge<Criteria, ResponseHandler>
{
    Set<GenericMessage> getMessages(Criteria criteria, GenericDevice device);
    ResponseHandler postMessages(Set<GenericMessage> messages);

    Set<GenericGps> getCoordinates(Criteria criteria, GenericDevice device);
    ResponseHandler postGps(Set<GenericGps> gpsSets);

    boolean checkAccountOnExist(Criteria email);
    GenericAccount getAccount(Criteria criteria);
    ResponseHandler postAccount(GenericAccount account);

    Set<GenericDevice> getDevices(Criteria criteria);
    ResponseHandler postDevice(GenericDevice device, String email);
}
