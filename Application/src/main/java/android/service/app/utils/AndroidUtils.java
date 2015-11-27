package android.service.app.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.app.ShortcutActivity;
import android.service.app.db.data.GenericAccount;
import android.service.app.db.data.GenericDevice;
import android.service.app.db.data.impl.Account;
import android.service.app.db.data.impl.Device;
import android.service.app.db.sqllite.SqlLiteDatabase;
import android.service.app.json.DataFilter;
import android.service.app.json.RestBridge;
import android.service.app.json.RestHttpTextResponseHandler;
import android.service.app.ui.SettingsActivity;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.concurrent.atomic.AtomicBoolean;

public enum AndroidUtils
{
    utils;

    public static final String SUBJECT = "subject";
    public static final String OBJECT = "object";

    public static boolean isObjectAccount(Context context)
    {
        Account account = AndroidUtils.getAccount(context);
        return !account.isEmpty() && account.getDescription().contains(AndroidUtils.OBJECT);
    }

    public static Account getAccount(Context context)
    {
        SqlLiteDatabase.DatabaseWork databaseWork = new SqlLiteDatabase.DatabaseWork(context)
        {
            @Override
            public Object execute()
            {
                return accounts().getFirst();
            }
        };
        return (Account) databaseWork.run();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void registerOrReuseAccount(Context context, final String email)
    {
        final RestBridge restBridge = new RestBridge(context);
        DataFilter filter = DataFilter.BY_VALUE.setFilter(email);
        RequestParams requestParams = new RequestParams();
        requestParams.put("account", email);
        filter.setRequestParams(requestParams);

        final AtomicBoolean accountExistOnInternalStorage = new AtomicBoolean(false);
        SqlLiteDatabase.DatabaseWork databaseWork = new SqlLiteDatabase.DatabaseWork(context)
        {
            @Override
            public Object execute()
            {
                GenericAccount account = accounts().getFirst();
                if (!account.isEmpty()) accountExistOnInternalStorage.set(true);
                return accountExistOnInternalStorage;
            }
        };
        databaseWork.run();

        boolean accountExist = accountExistOnInternalStorage.get();
        if (accountExist)
        {
            Log.info("account is already exist");
            return;
        }

        boolean accountExistOnExternalStorage = restBridge.checkAccountOnExist(filter);
        final String checkResult = "; get_result: " + restBridge.isSuccessLastResponse();
        Log.info("accountExistOnExternalStorage: " + accountExistOnExternalStorage + checkResult);

        if (!accountExistOnExternalStorage)
        {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(SettingsActivity.EMAIL_FIELD_NAME, "");
            editor.commit();

            // you are object
            final Account account = new Account(email);
            restBridge.postAccount(account);
            if (Log.isInfoEnabled()) Log.info("account was posted successfully on server: " + account);
            final String postResult = "; post_result: " + restBridge.isSuccessLastResponse();

            databaseWork = new SqlLiteDatabase.DatabaseWork(context)
            {
                @Override
                public Object execute()
                {
                    account.setDescription(OBJECT + postResult);
                    account.setId(insert(account));
                    updateOrInsertSyncIfNeeded(messages().getSyncForUpdate(account));
                    updateOrInsertSyncIfNeeded(coordinates().getSyncForUpdate(account));
                    updateOrInsertSyncIfNeeded(devices().getSyncForUpdate(account));
                    return account.getId();
                }
            };
            databaseWork.runInTransaction();
            final Device device = new Device(AndroidUtils.getDeviceName(), account.getId());
            restBridge.postDevice(device, email);
        }
        else
        {
            // you are subject
            final GenericAccount account = restBridge.getAccount(filter);
            final String getResult = "; get_result=" + restBridge.isSuccessLastResponse();

            databaseWork = new SqlLiteDatabase.DatabaseWork(context)
            {
                @Override
                public Object execute()
                {
                    account.setDescription(SUBJECT + getResult);
                    account.setId(insert(account));
                    Integer accountId = account.getId();
                    updateOrInsertSyncIfNeeded(messages().getSyncForUpdate(account));
                    updateOrInsertSyncIfNeeded(coordinates().getSyncForUpdate(account));
                    updateOrInsertSyncIfNeeded(devices().getSyncForUpdate(account));
                    return accountId;
                }
            };
            databaseWork.runInTransaction();
            ShortcutActivity.addShortcut(context);
        }

        Log.info("registration was passed");
    }

    private static void threadSleep(long sleepTime)
    {
        try
        {
            Thread.sleep(sleepTime);
        }
        catch (InterruptedException e)
        {
            Log.error(e);
        }
    }

    public static void printDataOnScreen(String message, ContextWrapper wrapper)
    {
        Intent i = new Intent("EVENT_UPDATED");
        i.putExtra("<Key>", message);
        wrapper.sendBroadcast(i);

        if (Log.isInfoEnabled()) Log.info(message);
    }

    public static void handleException(Exception e)
    {
        handleExceptionWithoutThrow(e);

        throw new AndroidApplicationException(e);
    }

    public static void handleExceptionWithoutThrow(Throwable e)
    {
        Log.error(e);
        e.printStackTrace();
    }

    public static class AndroidApplicationException extends RuntimeException
    {
        public AndroidApplicationException(Throwable throwable)
        {
            super(throwable);
        }
    }

    public static String getDeviceName()
    {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer))
        {
            return capitalize(model);
        } else
        {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s)
    {
        if (s == null || s.length() == 0)
        {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first))
        {
            return s;
        } else
        {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
