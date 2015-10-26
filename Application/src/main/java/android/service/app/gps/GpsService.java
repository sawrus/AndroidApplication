package android.service.app.gps;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.service.app.AndroidApplication;
import android.service.app.RemoteService;
import android.service.app.db.Database;
import android.service.app.db.DatabaseHelper;
import android.service.app.db.data.Gps;
import android.service.app.db.data.Message;
import android.service.app.db.inventory.Device;
import android.service.app.utils.Android;
import android.service.app.utils.Log;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

public class GpsService extends Service implements LocationListener
{
    private Context context = null;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location = null;
    double latitude = 0;
    double longitude = 0;
    public AndroidApplication app;
    private RemoteService.Stub remoteServiceStub;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = AndroidApplication.DELAY_MILLIS;
    protected LocationManager locationManager;

    private Device device = null;

    public static final int GPS_CAPACITY = 10;
    private static final Set<Gps> GPSES = new LinkedHashSet<>(GPS_CAPACITY);

    @Override
    public void onCreate()
    {
        app = (AndroidApplication) getApplication();
        app.setGpsService(this);
        app.setGpsServiceOnCreate(true);

        this.context = app.getApplicationContext();

        fillDevice();
    }

    @Override
    public void onDestroy()
    {
        stopUsingGPS();

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.v("call onStartCommand");

        app.setGpsServiceOnCreate(true);
        this.context = app.getApplicationContext();
        remoteServiceStub = new RemoteService.Stub()
        {
            public void sendString(String data) throws RemoteException
            {
                Log.v("remoteServiceStub:sendString" + data);
            }
        };

        app.setGpsService(this);

        try
        {
            geoLocation();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        trackGps();

        return super.onStartCommand(intent, flags, startId);
    }

    private Gps getGpsCoordinates() throws Exception
    {
        if(canGetLocation())
        {
            double latitude = getLatitude();
            double longitude = getLongitude();

            Gps gps = new Gps(device.getId(), latitude, longitude);
            if (!GPSES.contains(gps))
                Android.printDataOnScreen("latitude=" + latitude + "; longitude=" + longitude, this);
            return gps;
        }

        else
        {
            return new Gps();
        }
    }

    private void saveGpsSetToDatabase()
    {
        DatabaseHelper androidDatabase = getAndroidDatabase(getApplicationContext());
        SQLiteDatabase database = androidDatabase.getWritableDatabase();
        database.beginTransaction();
        try
        {
            for (Gps gps: GPSES) androidDatabase.addData(gps);
            database.setTransactionSuccessful();
        }
        finally
        {
            database.endTransaction();
        }

        androidDatabase.close();
    }

    private void trackGps()
    {
        try
        {
            Gps gps = getGpsCoordinates();
            addGpsIfNeeded(gps);
        } catch (Exception e)
        {
            e.printStackTrace();
            Android.printDataOnScreen(e.getMessage(), this);
        }
    }

    private void addGpsIfNeeded(Gps gps)
    {
        GPSES.add(gps);

        if (GPS_CAPACITY == GPSES.size())
        {
            saveGpsSetToDatabase();
            GPSES.clear();
        }
    }

    public GpsService(){}

    private void fillDevice()
    {
        DatabaseHelper androidDatabase = getAndroidDatabase(getApplicationContext());
        device = androidDatabase.device();
        androidDatabase.close();
    }

    @NonNull
    private static DatabaseHelper getAndroidDatabase(Context context)
    {
        return new DatabaseHelper(context, Database.ANDROID_V_1_5);
    }

    public void setContext(Context context)
    {
        this.context = context;
    }

    public GpsService(Context context) throws Exception
    {
        this.context = context;
        geoLocation();
    }

    private Location geoLocation() throws Exception
    {
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        // getting GPS status
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPSEnabled && !isNetworkEnabled)
        {
        } else
        {
            this.canGetLocation = true;
            if (isNetworkEnabled)
            {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Log.v("Network");
                if (locationManager != null)
                {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null)
                    {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
            if (isGPSEnabled)
            {
                if (location == null)
                {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.v("GPS Enabled");
                    if (locationManager != null)
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null)
                        {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }
        }
        return location;
    }

    public double getLatitude()
    {
        if (location != null)
        {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public boolean canGetLocation()
    {
        return this.canGetLocation;
    }

    public void showSettingsAlert()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    public double getLongitude()
    {
        if (location != null)
        {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public void stopUsingGPS()
    {
        if (locationManager != null)
        {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(GpsService.this);
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        this.location = location;
        trackGps();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        Log.v("status changed to " + provider + " [" + status + "]");
        trackGps();
    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }


    @Override
    public void onProviderDisabled(String provider)
    {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return remoteServiceStub;
    }
}
