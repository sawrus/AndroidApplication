package android.service.app.utils;

import android.text.TextUtils;

public enum Log
{
    logger;

    private static final String TAG_LOGGER = "LOGGER";

    public static boolean isDebugEnabled()
    {
        return android.util.Log.isLoggable(TAG_LOGGER, android.util.Log.DEBUG);
    }

    public static boolean isInfoEnabled()
    {
        return android.util.Log.isLoggable(TAG_LOGGER, android.util.Log.INFO);
    }

    public static boolean isWarnEnabled()
    {
        return android.util.Log.isLoggable(TAG_LOGGER, android.util.Log.WARN);
    }

    public static boolean isVerboseEnabled()
    {
        return android.util.Log.isLoggable(TAG_LOGGER, android.util.Log.VERBOSE);
    }

    public static void debug(String message) {
        android.util.Log.d(TAG_LOGGER, getLocation() + message);
    }

    public static void info(String message) {
        android.util.Log.i(TAG_LOGGER, message);
    }

    public static void verbose(String message) {
        android.util.Log.v(TAG_LOGGER, getLocation() + message);
    }

    public static void warn(String message) {
        android.util.Log.w(TAG_LOGGER, message);
    }

    public static void error(String message) {
        android.util.Log.e(TAG_LOGGER, getLocation() + message);
    }

    public static void error(String message, Throwable throwable) {
        android.util.Log.e(TAG_LOGGER, getLocation() + message, throwable);
    }

    public static void error(Throwable throwable) {
        android.util.Log.e(TAG_LOGGER, getLocation(), throwable);
    }

    private static String getLocation() {
        if (Boolean.valueOf(System.getProperty("fastlog")))
        {
            info("fastlog is turning on");
            return "";
        }

        final String className = Log.class.getName();
        final StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        boolean found = false;

        for (int i = 0; i < traces.length; i++) {
            StackTraceElement trace = traces[i];

            try {
                if (found) {
                    if (!trace.getClassName().startsWith(className)) {
                        Class<?> clazz = Class.forName(trace.getClassName());
                        return "[" + getClassName(clazz) + ":" + trace.getMethodName() + ":" + trace.getLineNumber() + "]: ";
                    }
                }
                else if (trace.getClassName().startsWith(className)) {
                    found = true;
                    continue;
                }
            }
            catch (ClassNotFoundException e) {
            }
        }

        return "[]: ";
    }

    private static String getClassName(Class<?> clazz) {
        if (clazz != null) {
            if (!TextUtils.isEmpty(clazz.getSimpleName())) {
                return clazz.getSimpleName();
            }

            return getClassName(clazz.getEnclosingClass());
        }

        return "";
    }
}
