package android.service.app.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.service.app.db.data.GenericAccount;
import android.service.app.db.data.impl.Account;
import android.service.app.db.data.impl.Device;
import android.service.app.db.sqllite.SqlLiteDatabase;
import android.service.app.json.DataFilter;
import android.service.app.json.RestBridge;

import java.util.concurrent.atomic.AtomicBoolean;

public enum AndroidUtils
{
    utils;

    public static final String SUBJECT = "subject";
    public static final String OBJECT = "object";

    public static void registerOrReuseAccount(Context context, final String email)
    {
        final RestBridge restBridge = new RestBridge(context);
        DataFilter filter = DataFilter.BY_VALUE.setFilter(email);

        final AtomicBoolean accountExistOnInternalStorage = new AtomicBoolean(false);
        SqlLiteDatabase.DatabaseWork databaseWork = new SqlLiteDatabase.DatabaseWork(context)
        {
            @Override
            public Object runInTransaction()
            {
                GenericAccount account = accounts().getFirst();
                if (!account.isEmpty() && account.getEmail().equals(email)) accountExistOnInternalStorage.set(true);
                return accountExistOnInternalStorage;
            }
        };
        databaseWork.run();

        if (accountExistOnInternalStorage.get())
        {
            if (Log.isInfoEnabled()) Log.info("account is already exist");
            return;
        }

        boolean accountExistOnExternalStorage = restBridge.checkAccountOnExist(filter);
        if (!accountExistOnExternalStorage)
        {
            // you are subject
            final Account account = new Account(email);
            restBridge.postAccount(account);

            databaseWork = new SqlLiteDatabase.DatabaseWork(context)
            {
                @Override
                public Object runInTransaction()
                {
                    account.setDescription(SUBJECT);
                    account.setId(insert(account));
                    updateOrInsertSyncIfNeeded(messages().getSyncForUpdate(account));
                    updateOrInsertSyncIfNeeded(coordinates().getSyncForUpdate(account));
                    updateOrInsertSyncIfNeeded(devices().getSyncForUpdate(account));
                    return account.getId();
                }
            };

            databaseWork.run();
        }
        else
        {
            // you are object
            final GenericAccount account = restBridge.getAccount(filter);

            databaseWork = new SqlLiteDatabase.DatabaseWork(context)
            {
                @Override
                public Object runInTransaction()
                {
                    account.setDescription(OBJECT);
                    account.setId(insert(account));

                    Integer accountId = account.getId();
                    insert(new Device(AndroidUtils.getDeviceName(), accountId));

                    updateOrInsertSyncIfNeeded(messages().getSyncForUpdate(account));
                    updateOrInsertSyncIfNeeded(coordinates().getSyncForUpdate(account));
                    updateOrInsertSyncIfNeeded(devices().getSyncForUpdate(account));
                    return accountId;
                }
            };

            databaseWork.run();
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

    public static void handleExceptionWithoutThrow(Exception e)
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
