package android.service.app.db.data;

public interface GenericDevice extends GenericData
{
    void setName(String name);
    void setAccountId(int account_id);
    String getName();
    int getAccountId();
    GenericAccount getAccount();
}
