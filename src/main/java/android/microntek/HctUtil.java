package android.microntek;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import java.util.*;

public class HctUtil
{
  public static boolean CheckIsRun(final Context context, final String s) {
    boolean processFound = false;
    boolean isTopActivity = false;
    final ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
    final Iterator<ActivityManager.RunningAppProcessInfo> iterator = activityManager.getRunningAppProcesses().iterator();
    while (iterator.hasNext()) {
      if (iterator.next().processName.equals(s)) {
        processFound = true;
        break;
      }
    }
    //Added in Android 6.0
    if (processFound) {
      for (final ActivityManager.RunningTaskInfo runningTaskInfo : activityManager.getRunningTasks(100)) {
        if (runningTaskInfo.topActivity.getPackageName().equals(s) && runningTaskInfo.baseActivity.getPackageName().equals(s)) {
          isTopActivity = true;
          break;
        }
      }
    }
    if (!processFound) {
      isTopActivity = false;
    }
    return isTopActivity;
  }

  public static byte[] getAsciiByteArray(final byte[] array, final int n, final int n2) {
    int i = 0;
    int n3 = n;
    while (true) {
      while (i < n2) {
        final int n4 = n3 + 1;
        if (array[n3] == 0) {
          final int n5 = i;
          final byte[] array2 = new byte[i];
          for (int j = 0; j < n5; ++j) {
            array2[j] = array[n + j];
          }
          return array2;
        }
        ++i;
        n3 = n4;
      }
      continue;
    }
  }

  public static String getAsciiString(final byte[] array, final int n, final int n2) {
    int i = 0;
    int n3 = n;
    while (true) {
      while (i < n2) {
        final int n4 = n3 + 1;
        if (array[n3] == 0) {
          final int n5 = i;
          final byte[] array2 = new byte[i];
          for (int j = 0; j < n5; ++j) {
            array2[j] = array[n + j];
          }
          try {
            return new String(array2, "gb2312");
          }
          catch (Exception ex) {
            return "null";
          }

        }
        ++i;
        n3 = n4;
      }
      continue;
    }
  }

  public static int getFramePos(final byte[] array, final int n, final int n2) {
    int n3 = -1;
    int i = n;
    while (i < n2) {
      if (array[i] == -6) {
        if (n2 - i < 5) {
          break;
        }
        final int n4 = getInt2(array, i + 3, 2) + 5;
        final int n5 = n4 + i - 1;
        if (n2 - i >= n4) {
          n3 = (n5 << 16 | i);
          break;
        }
        break;
      }
      else {
        ++i;
      }
    }
    return n3;
  }

  public static int getInt(final byte[] array, final int n, final int n2) {
    int n3 = 0;
    for (int i = 0; i < n2; ++i) {
      n3 = (n3 << 8) + (array[n + i] & 0xFF);
    }
    return n3;
  }

  public static int getInt2(final byte[] array, final int n, final int n2) {
    int n3 = 0;
    for (int i = 0; i < n2; ++i) {
      n3 = (n3 << 8) + (array[n + n2 - 1 - i] & 0xFF);
    }
    return n3;
  }

  public static String getTopActivityClassName(final Context context) {
    final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    final ActivityManager.RunningTaskInfo runningTaskInfo = activityManager.getRunningTasks(1).get(0);
    final ComponentName topActivity = runningTaskInfo.topActivity;
    return topActivity.getClassName();
  }


  public static String getTopActivityPackageName(final Context context) {
    for (final ActivityManager.RunningAppProcessInfo runningAppProcessInfo : ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses()) {
      if (runningAppProcessInfo.importance == 100) {
        return runningAppProcessInfo.processName;
      }
    }
    return null;
  }

  public static boolean isPackageApplicationEnabled(final Context context, final String s) {
    int applicationEnabledSetting = 2;
    final PackageManager packageManager = context.getPackageManager();
    if (isPackageInstalled(context, s)) {
      applicationEnabledSetting = packageManager.getApplicationEnabledSetting(s);
    }
    return (applicationEnabledSetting != 2 && applicationEnabledSetting != 3);
  }

  public static boolean isPackageInstalled(final Context context, final String s) {
    final List<PackageInfo> installedPackages = context.getPackageManager().getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
    for (int size = installedPackages.size(), i = 0; i < size; ++i) {
      if (s.equalsIgnoreCase(installedPackages.get(i).packageName)) {
        return true;
      }
    }
    return false;
  }

  public static int mtcGetRealVolume(final int newVolume, final int maxVolume) {
    final float minVolume = 20.0f;
    final float checkVolume = 100.0f * newVolume / maxVolume;
    float realVolume;
    if (checkVolume < minVolume) {
      realVolume = 3.0f * checkVolume / 2.0f;
    }
    else if (checkVolume < 50.0f) {
      realVolume = checkVolume + 10.0f;
    }
    else {
      realVolume = 4.0f * checkVolume / 5.0f + minVolume;
    }
    return (int)realVolume;
  }
}
