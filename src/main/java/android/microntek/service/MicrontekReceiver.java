package android.microntek.service;

import android.content.*;
import android.os.*;
import android.telecom.Log;

public class MicrontekReceiver extends BroadcastReceiver
{
  String LOG_TAG = "MTCReceiver";
  private void GpsTimeService(final Context context) {
    final Intent intent = new Intent();
    intent.setComponent(new ComponentName("android.microntek.service", "android.microntek.service.MicrontekTimeUpdateServer"));
    context.startServiceAsUser(intent, UserHandle.OWNER);
  }

  private void RadioService(final Context context) {
    final Intent intent = new Intent();
    intent.setComponent(new ComponentName("com.microntek.radio", "com.microntek.radio.RadioService"));
    context.startServiceAsUser(intent, UserHandle.OWNER);
  }

  private void startMtcManage(final Context context) {
    final Intent intent = new Intent();
    intent.setComponent(new ComponentName("android.microntek.service", "android.microntek.service.ManageService"));
    context.startServiceAsUser(intent, UserHandle.OWNER);
  }

  private void WeatherService(final Context context) {
    final Intent intent = new Intent();
    intent.setComponent(new ComponentName("com.microntek.weather", "com.microntek.weather.Weather"));
    context.startServiceAsUser(intent, UserHandle.OWNER);

  }
  public void onReceive(final Context context, final Intent intent) {
    if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
      Log.i(LOG_TAG, "Received BOOT_COMPLETED");
      this.startMtcManage(context);
      Log.i(LOG_TAG, "Started ManageService");
      this.GpsTimeService(context);
      Log.i(LOG_TAG, "Started MicronTekTimeUpdateServer");
      this.RadioService(context);
      Log.i(LOG_TAG, "Started RadioService");
      this.WeatherService(context);
      Log.i(LOG_TAG, "Started WeatherService");
    }
  }
}
