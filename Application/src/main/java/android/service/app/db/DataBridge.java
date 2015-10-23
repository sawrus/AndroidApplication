package android.service.app.db;

import android.service.app.db.data.Gps;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.db.user.Account;

import java.util.Set;

public interface DataBridge<Criteria, ResponseHandler>
{
    Set<Message> getMessages(Criteria criteria);
    ResponseHandler postMessages(Set<Message> messages);

    Set<Gps> getGps(Criteria criteria);
    ResponseHandler postGps(Set<Gps> gpsSets);

    Account getAccount(Criteria criteria);
    ResponseHandler postAccount(Account account);

    Device getDevice(Criteria criteria);
    ResponseHandler postDevice(Device device);
}
