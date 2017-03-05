package android.microntek.service;

import android.app.*;
import android.content.pm.PackageManager;
import android.location.*;
import android.provider.*;
import android.os.*;
import android.content.*;

import java.util.Calendar;
import java.util.Date;

public class MicrontekTimeUpdateServer extends Service {
  private static Context mContext;
  private static int timeupdatecount;
  private BroadcastReceiver UpdateTime;
  private GpsStatus.Listener gpsStatusListener;
  private LocationListener locationListener;
  private LocationManager locationManager;
  private Handler mHandler;

  static {

    MicrontekTimeUpdateServer.timeupdatecount = 0;

  }

  public MicrontekTimeUpdateServer() {
      this.locationManager = null;
      this.mHandler = (Handler) new Handler() {
          public void handleMessage(final Message message) {
              super.handleMessage(message);
              if (message.what == 0 && locationManager == null) {
                  InitLoc();
              }
          }
      };
      this.locationListener = new LocationListener() {
          @Override
          public void onLocationChanged(Location location) {
              if (location != null) {
                  if (timeupdatecount > 0) {
                      timeupdatecount = timeupdatecount - 1;
                      if (timeupdatecount == 0) {
                          final long time = location.getTime();
                          final Date time2 = new Date(time);
                          SystemClock.setCurrentTimeMillis(time);
                          Calendar.getInstance().setTime(time2);
                          mContext.sendBroadcast(new Intent("android.intent.action.TIME_SET"));
                      }
                  }
              } else {
                  timeupdatecount = 8;
              }
          }

          @Override
          public void onProviderDisabled(String provider) {

          }

          @Override
          public void onProviderEnabled(String provider) {

          }

          @Override
          public void onStatusChanged(String provider, int status, Bundle extras) {

          }
      };
      this.gpsStatusListener = new GpsStatus.Listener() {
          @Override
          public void onGpsStatusChanged(int event) {
              switch (event) {
                  case 3: {
                      timeupdatecount = 8;
                      break;
                  }
              }

          }
      };
      this.UpdateTime = new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
              final String action = intent.getAction();
              if (action.equals("com.microntek.gpsautoupdate")) {
                  if (intent.getBooleanExtra("en", false)) {
                      if (locationManager == null) {
                          InitLoc();
                      }
                  } else if (locationManager != null) {
                      locationManager.removeGpsStatusListener(gpsStatusListener);
                      try {
                          locationManager.removeUpdates(locationListener);
                      } catch (SecurityException ex) {

                      }

                      locationManager = null;
                  }
              } else if (action.equals("com.microntek.freshtime")) {
                  context.sendBroadcast(new Intent("android.intent.action.TIME_SET"));
              }
          }

      };
  }

  private void InitLoc() {
      if (!Settings.Secure.isLocationProviderEnabled(this.getContentResolver(), "gps")) {
          Settings.Secure.setLocationProviderEnabled(this.getContentResolver(), "gps", true);
      }
      if (Settings.System.getInt(this.getContentResolver(), "gpsupdatetime", 0) != 0) {

          try {
              final Object systemService = this.getSystemService(LOCATION_SERVICE);


              this.locationManager = (LocationManager) systemService;
              final LocationManager locationManager = this.locationManager;
              final String bestProvider = locationManager.getBestProvider(this.getCriteria(), true);
              this.locationManager.requestLocationUpdates(bestProvider, 1000L, 0.0f, this.locationListener);
              locationManager.addGpsStatusListener(this.gpsStatusListener);
              this.updateLocation(this.locationManager.getLastKnownLocation(bestProvider));
              MicrontekTimeUpdateServer.timeupdatecount = 8;

          } catch (SecurityException ex) {
              ex.printStackTrace();
          }
      }
  }

    private Criteria getCriteria() {

        final Criteria criteria = new Criteria();
        criteria.setAccuracy(1);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(false);
        criteria.setBearingRequired(false);
        criteria.setAltitudeRequired(true);
        criteria.setPowerRequirement(1);
        return criteria;
    }

    private void updateLocation(final Location location) {
        final int int1 = Settings.System.getInt(this.getContentResolver(), "gpsupdatetime", 0);
        if (location != null) {
            if (MicrontekTimeUpdateServer.timeupdatecount > 0) {
                --MicrontekTimeUpdateServer.timeupdatecount;
                if (MicrontekTimeUpdateServer.timeupdatecount == 0) {
                    final long time = location.getTime();
                    if (int1 == 1) {
                        final Date time2 = new Date(time);
                        SystemClock.setCurrentTimeMillis(time);
                        Calendar.getInstance().setTime(time2);
                        MicrontekTimeUpdateServer.mContext.sendBroadcast(new Intent("android.intent.action.TIME_SET"));
                    }
                }
            }
        }
        else {
            MicrontekTimeUpdateServer.timeupdatecount = 8;
        }
    }
  private boolean isGpsOn() {
      return Settings.Secure.getInt(this.getContentResolver(), "location_mode", 0) == 3;
  }

  private void openGps() {
    final Intent intent = new Intent("com.android.settings.location.MODE_CHANGING");
    intent.putExtra("NEW_MODE", 3);
    this.sendBroadcast(intent, "android.permission.WRITE_SECURE_SETTINGS");
    Settings.Secure.putInt(this.getContentResolver(), "location_mode", 3);
  }

  public IBinder onBind(final Intent intent) {
    return null;
  }

  public void onCreate() {
    final long n = 3000L;
    super.onCreate();
    MicrontekTimeUpdateServer.mContext = (Context) this;

      if (Build.VERSION.SDK_INT >= 19) {
        if (!this.isGpsOn()) {
          this.openGps();
          this.mHandler.sendEmptyMessageDelayed(0, n);
        } else {
          this.InitLoc();
        }
      } else {
        this.mHandler.sendEmptyMessageDelayed(0, n);

    }
    final IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction("com.microntek.gpsautoupdate");
    intentFilter.addAction("com.microntek.freshtime");
    this.registerReceiver(this.UpdateTime, intentFilter);
  }

  public void onDestroy() {
    if (this.locationManager != null) {
      this.locationManager.removeGpsStatusListener(this.gpsStatusListener);
      try {
        this.locationManager.removeUpdates(this.locationListener);
      } catch (SecurityException ex) {}

      this.locationManager = null;
    }
    this.unregisterReceiver(this.UpdateTime);
    super.onDestroy();
  }

  public void onStart(final Intent intent, final int n) {
    super.onStart(intent, n);
  }
}
