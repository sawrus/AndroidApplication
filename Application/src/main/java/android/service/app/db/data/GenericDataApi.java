package android.service.app.db.data;

import java.util.Set;

public interface GenericDataApi<T extends GenericData> extends GenericDataInsertApi
{
    Set<T> getAll();
    Set<T> getActualBySync();
    Set<T> moreThanId(Object id);
    Set<T> lessThanId(Object id);
    Set<T> notEqualId(Object id);
    T filterBy(String key, Object value);
    T byId(Object id);
    T getFirst();
    GenericSync getSyncForUpdate(GenericAccount account);
}
