package android.service.app;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.service.app.gps.GpsService;
import android.service.app.utils.Log;

public class AndroidApplication extends android.app.Application
{
    public static final long DELAY_MILLIS = 30000L;

    protected Service service = null;
    private GpsService gpsService = null;

    protected boolean serviceOnCreate = false;
    protected boolean gpsServiceOnCreate = false;

    private android.os.Handler serviceHandler = null;
    private android.os.Handler handleService = null;
    private Runnable trackService;

    @Override
    public void onCreate()
    {
        super.onCreate();

        serviceHandler = new ServiceHandler();
        handleService = new ServiceHandler();

        startService(new Intent(this, Service.class));
        startService(new Intent(this, GpsService.class));

        startCheckService();
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
        stopCheckService();
    }

    public void setGpsServiceOnCreate(boolean mGpsServiceOnCreate)
    {
        this.gpsServiceOnCreate = mGpsServiceOnCreate;
    }

    public void setGpsService(GpsService gpsService)
    {
        this.gpsService = gpsService;
    }

    public void handleException(Exception e)
    {
        Log.error(e);
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
                            if (Log.isInfoEnabled()) Log.info("start service ...");
                        } else
                        {
                            if (serviceOnCreate)
                            {
                                Message msg = handleService.obtainMessage(2, "restart");
                                handleService.sendMessage(msg);
                                if (Log.isInfoEnabled()) Log.info("restart service ...");
                            }
                        }
                    }

                }).start();
                serviceHandler.postDelayed(trackService, DELAY_MILLIS);
            }
        };
        serviceHandler.postDelayed(trackService, DELAY_MILLIS);
    }

    protected void stopCheckService()
    {
        serviceHandler.removeCallbacks(trackService);
    }

    protected synchronized boolean checkService()
    {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE))
        {
            String className = runningServiceInfo.service.getClassName();
            if (Service.class.getName().equals(className) || GpsService.class.getName().equals(className))
                if (Log.isInfoEnabled()) Log.info("Running service=" + className);
            return true;
        }
        if (Log.isInfoEnabled()) Log.info("Running service: False result");
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
}
