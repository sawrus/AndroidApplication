package android.service.app.db.data;

import java.util.Map;
import java.util.Set;

public interface GenericData extends GenericDataInsertApi
{
    Integer getId();
    String getDescription();
    void setDescription(String description);
    String getTimezone();
    void setTimezone(String timezone);
    String getCreatedWhen();
    void setCreatedWhen(String created_when);
    void setId(Integer id);
    boolean isEmpty();
    Map<String, Object> getData();
    void setData(Map<String, Object> data);
    Set<String> getFields();
}
