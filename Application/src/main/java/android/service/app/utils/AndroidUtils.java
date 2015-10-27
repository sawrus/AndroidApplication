package android.service.app.utils;

import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;

public enum AndroidUtils
{
    utils;

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
