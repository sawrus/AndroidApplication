package android.service.app.db.inventory;

import android.service.app.db.GenericData;
import android.service.app.db.user.GenericAccount;

public interface GenericDevice extends GenericData<GenericDevice>
{
    void setName(String name);
    void setAccountId(int account_id);
    String getName();
    int getAccountId();
    GenericAccount getAccount();
}
