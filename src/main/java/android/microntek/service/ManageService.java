package android.microntek.service;

import android.app.*;
import android.content.*;
import android.microntek.serial.*;

import android.os.*;
import android.provider.*;
import android.util.*;

public class ManageService extends Service
{
  String LOG_TAG = "MTCManageService";
    private void StartAdbOn() {
  }

  private void StartBTService() {
    final Intent intent = new Intent();
    intent.setComponent(new ComponentName("android.microntek.mtcser", "android.microntek.mtcser.BTSerialService"));
    this.startServiceAsUser(intent, UserHandle.OWNER);

  }

  private void StartCanBusService() {
    final Intent intent = new Intent();
    intent.setComponent(new ComponentName("android.microntek.canbus", "android.microntek.canbus.CanBusServer"));
    this.startServiceAsUser(intent, UserHandle.OWNER);

  }

  private void StartCarService() {
    final Intent intent = new Intent();
    intent.setComponent(new ComponentName("android.microntek.service", "android.microntek.service.MicrontekServer"));
    this.startServiceAsUser(intent, UserHandle.OWNER);

  }



  public IBinder onBind(final Intent intent) {
    return null;
  }

  public void onCreate() {
    final String s = "mtcserial";

    final SerialService serialService = new SerialService();
    final String s2 = s;
    try {
      ServiceManager.addService(s2, (IBinder) serialService);
      Log.i(LOG_TAG, "Added serialService");
    } catch (RuntimeException ex) {
      Log.e(LOG_TAG, "Failed to add SerialService : e = " + ex);
    }
    try {
      this.StartBTService();
      Log.i(LOG_TAG, "Started mtcser.BTSerialService");
    } catch (RuntimeException ex) {
      Log.e(LOG_TAG, "Failed to add BTSerialService : e = " + ex);
    }
    try {
      this.StartCarService();
      Log.i(LOG_TAG, "Started service.MicrontekServer(CarService)");
    } catch (RuntimeException ex) {
      Log.e(LOG_TAG, "Failed to add MicrontekServer : e = " + ex);
    }
    try {
      this.StartCanBusService();
      Settings.System.putInt(this.getContentResolver(), "show_touches", 0);
      Log.i(LOG_TAG, "Started canbus.CanBusServer");
    } catch (RuntimeException ex) {
      Log.e(LOG_TAG, "Failed to add CanBusServer : e = " + ex);
    }
    try {
      this.StartAdbOn();
      Log.i(LOG_TAG, "Started AdbOn");
    } catch (RuntimeException ex) {
      Log.e(LOG_TAG, "Failed to add AdbOn : e = " + ex);

    }
  }


  public int onStartCommand(final Intent intent, final int n, final int n2) {
    return super.onStartCommand(intent, n, n2);
  }
}
