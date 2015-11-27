package android.service.app.utils;

import android.service.app.db.data.GenericData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public enum JsonUtils
{
    utils;

    public static Set<JSONObject> getJsonObjects(JSONArray jsonArray)
    {
        Set<JSONObject> jsonObjects = new LinkedHashSet<>();
        if (jsonArray == null || jsonArray.length() == 0) return jsonObjects;
        for (int i = 0; i < jsonArray.length(); i++)
        {
            JSONObject jsonObject;
            try
            {
                jsonObject = jsonArray.getJSONObject(i);
            }
            catch (JSONException e)
            {
                Log.error(e);
                throw new RuntimeException(e);
            }
            jsonObjects.add(jsonObject);
        }
        return jsonObjects;
    }

    public static <T extends GenericData> T getData(Class<T> dataClass, JSONObject jsonObject)
    {
        T dataInstance = newDataInstance(dataClass);
        Map<String, Object> data = new LinkedHashMap<>();
        for (String field: dataInstance.getFields())
        {
            Object value;
            try
            {
                value = jsonObject.get(field);
            }
            catch (JSONException e)
            {
                Log.error(e);
                throw new RuntimeException(e);
            }
            data.put(field, value);
        }

        dataInstance.setData(data);
        return dataInstance;
    }

    private static <T extends GenericData> T newDataInstance(Class<T> dataClass)
    {
        try
        {
            return dataClass.newInstance();
        }
        catch (Exception e)
        {
            Log.error(e);
            throw new RuntimeException(e);
        }
    }
}
