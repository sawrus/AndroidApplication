package android.service.app.db;

public interface GenericData
{
    Integer getId();
    void setId(Integer id);
    String getDescription();
    void setDescription(String description);
    String getTimezone();
    void setTimezone(String timezone);
    String getCreatedWhen();
    void setCreatedWhen(String created_when);
}
