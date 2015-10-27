package android.service.app.db.user;

import android.service.app.db.GenericData;

public interface GenericAccount extends GenericData<GenericAccount>
{
    void setEmail(String email);
    String getEmail();
}
