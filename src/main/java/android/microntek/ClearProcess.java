package android.microntek;

import android.microntek.service.R;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import android.content.*;
import android.content.pm.*;
import android.app.*;

import static android.content.Context.ACTIVITY_SERVICE;

public class ClearProcess
{
  static String gpspackage;
  private static boolean mBusy;
  static Context mContext;
  //added in Android 6.0
  private static int mMode;
  static Toast mToast;
  private static final String[] packagelist;
  static Object sGlobalLock;
  static ClearProcess sInstance;
  private static final String[] serverlist;

  static {
    final int n = 4;
    final int n2 = 3;
    final int n3 = 2;
    final int n4 = 1;
    ClearProcess.mBusy = false;
    ClearProcess.sGlobalLock = new Object();
    //Added "com.intel.thermal", "com.android.bluetooth", "com.hiworld.", "net.easyconn", "android.cn.ecar.cds.process.CoreService" "com.google.android" in Android 6.0
    serverlist = new String[]{"android.microntek.", "com.murtas.", "com.microntek.", "com.goodocom.gocsdk", "android.rockchip.update.service", "com.android.systemui", "com.hct.obdservice.OBDService", "com.unisound", "com.intel.thermal", "com.dpadnavi.assist", "cn.manstep.phonemirror", "com.android.bluetooth", "com.hiworld.", "net.easyconn", "android.cn.ecar.cds.process.CoreService", "com.google.android"};
    //ADDED A LOT IN aNDROID 6.0 :)
    packagelist = new String[]{"android.microntek.", "com.murtas.", "com.microntek.", "com.goodocom.gocsdk", "android.rockchip.update.service", "com.android.systemui", "com.hct.obdservice.OBDActivity", "com.unisound", "com.dpadnavi.assist","com.intel.thermal",  "cn.manstep.phonemirror", "com.hiworld.", "com.carboy.launch", "com.android.bluetooth", "net.easyconn", "com.android.launcher", "com.google.android"};
  }

  private ClearProcess(final Context mContext) {
    ClearProcess.mContext = mContext;
  }

  private int closeRunningAppProcess(final Context context) {
    final ArrayList<ProcessInfo> processInfos = new ArrayList<>();
    final ActivityManager activityManager = (ActivityManager)context.getSystemService(ACTIVITY_SERVICE);
    for (final ActivityManager.RunningAppProcessInfo runningAppProcessInfo : activityManager.getRunningAppProcesses()) {
      final int pid = runningAppProcessInfo.pid;
      final int uid = runningAppProcessInfo.uid;
      final String processName = runningAppProcessInfo.processName;
      final int dalvikPrivateDirty = activityManager.getProcessMemoryInfo(new int[] { pid })[0].dalvikPrivateDirty;
      final ProcessInfo processInfo = new ProcessInfo();
      processInfo.setPid(pid);
      processInfo.setUid(uid);
      processInfo.setMemSize(dalvikPrivateDirty);
      processInfo.setPocessName(processName);
      processInfo.pkgnameList = runningAppProcessInfo.pkgList;
      processInfos.add(processInfo);
      final String[] pkgList = runningAppProcessInfo.pkgList;
    }
    int n = 0;
    for (final ProcessInfo processInfo2 : processInfos) {
      if (processInfo2.getUid() >= 10000) {
        final String processName2 = processInfo2.getProcessName();
        if (processName2.indexOf(".") == -1 || this.getisdontclose2(processName2) || this.isInputServicePkgName(context, processName2) || this.isWallpaperPkgName(context, processName2)) {
          continue;
        }
        if (ClearProcess.mMode == 0 && !this.getisdontclose(processName2)) {
          this.closepackage(processName2);
          ++n;
        } else {
          final ActivityManager activityManager2 = activityManager;
          try {
            activityManager2.killBackgroundProcesses(processName2);
            ++n;
          } catch (Exception ex) {
            System.out.println(" deny the permission");
          }
        }
      }
    }
    return n;
  }

  private void closeRunningService(final Context context) {
    final List runningServices = ((ActivityManager)context.getSystemService(ACTIVITY_SERVICE)).getRunningServices(100);
    final ArrayList<RunServiceModel> runServiceModelArrayList = new ArrayList<>();

    for (Iterator<ActivityManager.RunningServiceInfo> iter = runningServices.iterator(); iter.hasNext(); ) {
      ActivityManager.RunningServiceInfo runningServiceInfo = iter.next();
      final int pid = runningServiceInfo.pid;
      final int uid = runningServiceInfo.uid;
      final String process = runningServiceInfo.process;
      final long activeSince = runningServiceInfo.activeSince;
      final int clientCount = runningServiceInfo.clientCount;
      final ComponentName componentName = runningServiceInfo.service;
      final String shortClassName = componentName.getShortClassName();
      final String packageName = componentName.getPackageName();

      final PackageManager packageManager = context.getPackageManager();
      try {
        final ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        final RunServiceModel runServiceModel = new RunServiceModel();
        final StringBuilder sb = new StringBuilder();
        final StringBuilder append = sb.append((Object) applicationInfo.loadLabel(packageManager)).append("");
        runServiceModel.setAppIcon(applicationInfo.loadIcon(packageManager));
        runServiceModel.setAppLabel(append.toString());
        runServiceModel.setServiceName(shortClassName);
        runServiceModel.setPkgName(packageName);
        final Intent intent = new Intent();
        intent.setComponent(componentName);
        runServiceModel.setIntent(intent);
        runServiceModel.setPid(pid);
        runServiceModel.setUid(uid);
        runServiceModel.setProcessName(process);
        runServiceModelArrayList.add(runServiceModel);
        continue;
      } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
      }

    }

    for ( RunServiceModel runServiceModel : runServiceModelArrayList) {
      if (runServiceModel.getUid() >= 10000) {
        final String pkgName = runServiceModel.getPkgName();
        if (this.getisdontclose(pkgName) || this.isInputServicePkgName(context, pkgName) || this.isWallpaperPkgName(context, pkgName)) {
          continue;
        }
        if (ClearProcess.mMode == 0 && !this.getisdontclose2(pkgName)) {
          this.closepackage(pkgName);
        }
        else {
          final Intent intent2 = runServiceModel.getIntent();
          try {
            context.stopService(intent2);
          }
          catch (SecurityException ex13) {
            System.out.println(" deny the permission");
          }
        }
      }
    }

  }

  private long getAvailMemory(final Context context) {
    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
    ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
    activityManager.getMemoryInfo(mi);
    return mi.availMem;
  }

  public static ClearProcess getInstance(final Context context) {
    synchronized (ClearProcess.sGlobalLock) {
      if (ClearProcess.sInstance == null) {
        ClearProcess.sInstance = new ClearProcess(context);
      }
      ClearProcess.mMode = -1;
      return ClearProcess.sInstance;
    }
  }

  private boolean getisdontclose(final String processName) {

    for (String serverProcessName : ClearProcess.serverlist) {
      if (processName.startsWith(serverProcessName)) {
        return true;
      }
    }

    return ClearProcess.gpspackage != null && processName.equals(ClearProcess.gpspackage);
  }

  private boolean getisdontclose2(final String processName) {
    for (String pkglistProcessName : ClearProcess.packagelist) {
      if (processName.startsWith(pkglistProcessName)) {
        return true;
      }
    }

    return ClearProcess.gpspackage != null && processName.equals(ClearProcess.gpspackage);
  }
  private boolean isInputServicePkgName(final Context context, final String serviceName) {
    boolean b = false;
    if (context != null && !TextUtils.isEmpty(serviceName)) {
      final Iterator<InputMethodInfo> iterator = ((InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE)).getInputMethodList().iterator();
      while (iterator.hasNext()) {
        if (serviceName.equalsIgnoreCase(iterator.next().getPackageName())) {
          b = true;
          break;
        }
      }
    }
    return b;
  }

  private boolean isWallpaperPkgName(Context context, String packageName) {


      PackageManager packageManager = context.getPackageManager();
      final List queryIntentServices = packageManager.queryIntentServices(new Intent("android.service.wallpaper.WallpaperService"), PackageManager.GET_META_DATA);
      final Iterator<ResolveInfo> iterator = queryIntentServices.iterator();
      while (iterator.hasNext()) {
        final ResolveInfo resolveInfo = iterator.next();
        ServiceInfo serviceInfo = resolveInfo.serviceInfo;
        if (packageName.equalsIgnoreCase(serviceInfo.packageName)) {
          return true;
        }
        continue;

      }
      return true;
  }

  public void ClearManange(final int mMode, final String gpspackage) {
    ClearProcess.mBusy = true;
    ClearProcess.mMode = mMode;
    ClearProcess.gpspackage = gpspackage;
    final long availMemory = this.getAvailMemory(ClearProcess.mContext);
    this.closeRunningService(ClearProcess.mContext);
    final int closeRunningAppProcess = this.closeRunningAppProcess(ClearProcess.mContext);
    final long availMemory2 = this.getAvailMemory(ClearProcess.mContext);
    if (mMode == 1) {
      final String string = ClearProcess.mContext.getString(R.string.clear_message, new Object[] { closeRunningAppProcess, Formatter.formatFileSize(ClearProcess.mContext, Math.abs(availMemory2 - availMemory)) });
      if (ClearProcess.mToast == null) {
        ClearProcess.mToast = Toast.makeText(ClearProcess.mContext, (CharSequence)string, Toast.LENGTH_SHORT);
      }
      else {
        ClearProcess.mToast.cancel();
        ClearProcess.mToast = Toast.makeText(ClearProcess.mContext, (CharSequence)string, Toast.LENGTH_LONG);
      }
      ClearProcess.mToast.show();
    }
    ClearProcess.mBusy = false;
  }

  public void closepackage(final String packageName) {
    if (packageName != null && packageName.length() != 0) {
      final ActivityManager activityManager = (ActivityManager)ClearProcess.mContext.getSystemService(ACTIVITY_SERVICE);

      try {
        activityManager.forceStopPackage(packageName);
      }
      catch (SecurityException e) {
        System.out.println(packageName + ":" + e.getMessage() + " deny the permission");
      }
    }
  }

  public boolean getBusy() {
    return ClearProcess.mBusy;
  }
}
