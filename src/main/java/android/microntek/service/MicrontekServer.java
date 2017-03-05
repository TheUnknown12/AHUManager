package android.microntek.service;

import android.microntek.common.*;
import android.microntek.serial.SerialManager;
import android.microntek.serial.SerialReceiver;
import android.mtc.MTCData;
import android.widget.*;
import com.microntek.app.*;
import android.provider.*;
import android.app.*;
import android.microntek.*;
import java.util.*;
import android.net.*;
import java.io.*;
import java.lang.reflect.*;
import android.media.*;
import android.util.*;
import android.os.*;

import android.content.*;

public class MicrontekServer extends Service implements VolumeInterface
{
    final MicrontekServer_KeyFunctions keyFunctions = new MicrontekServer_KeyFunctions(this);
    private final MicrontekServer_SerialDataProcess serialDataProcess = new MicrontekServer_SerialDataProcess(this);
    String LOG_TAG = "MicrontekServer";
  static String GPSPKNAME;
  static int KEY_VOLMAX;
    private static int KEY_VOLMAX_DEFAULT;
  static boolean btLock;
  static int bufRevCount;
  static boolean deviceLock;
  static boolean gps_isfront;
  static boolean gps_open;
  static String ipod;
  static int mCurVolume;
  static boolean mPoweron;
  static long runtime;
  static String safe;
  static byte[] serialRevBuf;
  private boolean BackViewState;
  private BroadcastReceiver CarkeyProc;
  private BroadcastReceiver MTCAPPProc;
  private BroadcastReceiver MTCploy;
  private BroadcastReceiver MediaDetectReceiver;
  private Runnable PowerLongPress;
  private Runnable PowerRebootRunnable;
  private int PowerState;
  private boolean ScreenSaverEnable;
  private boolean ScreenSaverEnableLocal;
  private boolean ScreenSaverOn;
  private int ScreenSaverTimeOut;
  private int ScreenSaverTimer;
  private String mAjXTopPackage;
  int mAppMode;
  Context mContext;
  Handler mHandler;
  private ProgressDialog mProgressDialog;
  private SerialManager mSerialManager;
  Toast mToast;
  int mTouchCount;
  private Handler mVolHandler;
  private VolumeDialog mVolumeDialog;
  private int mVolumeTemp;

  static {
    MicrontekServer.bufRevCount = 0;
    MicrontekServer.serialRevBuf = new byte[1024];
    MicrontekServer.runtime = 0L;
    MicrontekServer.GPSPKNAME = null;
    MicrontekServer.gps_open = false;
    MicrontekServer.gps_isfront = false;
    MicrontekServer.mPoweron = false;
    MicrontekServer.btLock = false;
    MicrontekServer.deviceLock = true;
    MicrontekServer.KEY_VOLMAX = 30;
      MicrontekServer.KEY_VOLMAX_DEFAULT = 30;
    MicrontekServer.mCurVolume = -1;
  }

  public MicrontekServer() {
    final int n = -1;
    this.mToast = null;
    this.mSerialManager = null;
    this.mProgressDialog = null;
    this.mAjXTopPackage = null;
    this.ScreenSaverTimeOut = n;
    this.ScreenSaverEnableLocal = false;
    this.ScreenSaverEnable = false;
    this.ScreenSaverTimer = 0;
    this.ScreenSaverOn = false;
    this.BackViewState = false;
    this.PowerState = 0;
    this.mAppMode = n;
    this.mTouchCount = 5;
    this.mHandler = new MicrontekServer_Handler(this);
    this.PowerRebootRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(LOG_TAG, "PowerReboot: ctl_reset=0");
            setParameters("ctl_reset=0");
        }
    };

    this.PowerLongPress =  new Runnable() {
        @Override
        public void run() {
            Log.i(LOG_TAG, "PowerLongPress: PowerOffDialog()");
            PowerOffDialog();
        }
    };
    this.MediaDetectReceiver = new MicrontekServer_MediaDetectReceiver(this);
    this.CarkeyProc = new MicrontekServer_CarKeyReceiver(this);
    this.MTCploy = new MicrontekServer_PhoneReceiver(this);

    this.MTCAPPProc = new MicrontekServer_ApplicationReceiver(this);

    this.mVolumeDialog = null;
    this.mVolHandler = new Handler() {
        public void handleMessage(final Message message) {
            final long n = 35;
            final int n2 = 5;
            final int n3 = 1;
            super.handleMessage(message);
            if (message.what == 0) {
                if (mCurVolume > mVolumeTemp) {
                    mCurVolume = mCurVolume  -1;
                    setVolume( mCurVolume, false);
                    mHandler.removeMessages(n2);
                    mHandler.sendEmptyMessageDelayed(0, n);
                }
                else {
                    setVolume( mCurVolume, true);
                    SendVolStatus1( mCurVolume);
                }
            }
            else if (message.what == n3) {
                if (mCurVolume < mVolumeTemp) {
                    mCurVolume = mCurVolume  +1;
                    setVolume( mCurVolume, false);
                    mHandler.removeMessages(n2);
                    mHandler.sendEmptyMessageDelayed(1, 35);
                }
                else {
                    setVolume( mCurVolume, true);
                    SendVolStatus1(mCurVolume);
                }
            }
        }
    };

  }

  private void DataProc(final byte[] array, int n) {
      serialDataProcess.DataProc(array, n);
  }


  void EQSwitch() {
    int keyEQmode = Settings.System.getInt(this.getContentResolver(), "KeyEQmode", 0);
    if (keyEQmode > 6 || keyEQmode < 0) {
      keyEQmode = 0;
    }

    String avEqSettings;
    if (keyEQmode == 0) {
      avEqSettings = Settings.System.getString(this.getContentResolver(), "KeyCustomEQ");
      if (avEqSettings == null) {
        avEqSettings = "10,10,10";
      }
    }
    else {
      avEqSettings = MTCData.music_stytle_data[keyEQmode - 1][0] + "," + MTCData.music_stytle_data[keyEQmode - 1][1] + "," + MTCData.music_stytle_data[keyEQmode - 1][2];
    }
    Settings.System.putInt(this.getContentResolver(), "KeyEQmode", keyEQmode);
    this.setParameters("av_eq=" + avEqSettings);
    Settings.System.putInt(this.getContentResolver(), "mtc_soundmode", keyEQmode);
    this.mToast.setText((CharSequence)this.getResources().getString(Constant.music_Style[keyEQmode]).toString());
    this.mToast.show();
    this.sendBroadcastAsUser(new Intent("com.microntek.eqchange"), UserHandle.CURRENT_OR_SELF);
  }

  void IRsetBrightness() {
      int[] brightnessArray = new int[]{10,51,102,153,204,255};
      String cfgBrightness = this.getParameters("cfg_backlight=");
      int brightness = 100;
      try {
          if (Integer.parseInt(cfgBrightness) / 2 < 5) {
            brightness = brightnessArray[(Integer.parseInt(cfgBrightness) / 2)];
          }

      } catch (NumberFormatException ex) {
         brightness = Settings.System.getInt(this.getContentResolver(), "screen_brightness", 100);
      }
      setParameters("cfg_backlight=" + brightness);
      setBrightness(brightness);
      Settings.System.putInt(this.getContentResolver(), "screen_brightness", brightness);
      this.sendBroadcastAsUser(new Intent("android.intent.action.SHOW_BRIGHTNESS_DIALOG"), UserHandle.CURRENT_OR_SELF);

  }

  private void InitSystemData() {
        //VOLMAX
      try{
          KEY_VOLMAX = Integer.parseInt(getParameters("cfg_maxvolume="));
      } catch (NumberFormatException ex) {
            KEY_VOLMAX = 30;
      }
        //VOLMAXDEFAULT
      try{
           KEY_VOLMAX_DEFAULT = Integer.parseInt(getParameters("cfg_vol_max_default="));
      } catch (NumberFormatException ex) {
          KEY_VOLMAX_DEFAULT = 0;
      }
        //Brightness
      try{
          setBrightness(Integer.parseInt(getParameters("cfg_backlight=")));
      } catch (NumberFormatException ex) {
          setBrightness(Settings.System.getInt(this.getContentResolver(),"screen_brightness", 100));
      }


      //EQMode
      int keyEQmode = Settings.System.getInt(this.getContentResolver(), "KeyEQmode", 0);
      if (keyEQmode > 6 || keyEQmode < 0) {
          keyEQmode = 0;
      }

      String avEqSettings;
      if (keyEQmode == 0) {
          avEqSettings = Settings.System.getString(this.getContentResolver(), "KeyCustomEQ");
          if (avEqSettings == null) {
              avEqSettings = "10,10,10";
          }
      }
      else {
          avEqSettings = MTCData.music_stytle_data[keyEQmode - 1][0] + "," + MTCData.music_stytle_data[keyEQmode - 1][1] + "," + MTCData.music_stytle_data[keyEQmode - 1][2];
      }

      this.setParameters("av_eq=" + avEqSettings);

      //keyBalance
      String keyBalance = Settings.System.getString(this.getContentResolver(), "KeyBalance");
      if (keyBalance == null) {
          keyBalance = "14,14";
      } else {
          try {


          String[] keyBalanceArr = keyBalance.split(",");
          keyBalanceArr[1] = String.valueOf(28 - Integer.parseInt(keyBalanceArr[1]));
              keyBalance =  keyBalanceArr[0] +"," +  keyBalanceArr[1];
          } catch (NumberFormatException ex) {
              keyBalance = "14,14";
          }

      }
      this.setParameters("av_balance=" + keyBalance);

      //Loudness
      int loudness = Settings.System.getInt(this.getContentResolver(), "av_lud", 0);
        if (loudness == 1) {
            setParameters("av_lud=on");
        } else {
            setParameters("av_lud=off");
        }

      //CustomSUB
      int keyCustomsSUB = Settings.System.getInt(this.getContentResolver(), "KeyCustomSUB",10);
      setParameters("av_sub=" + keyCustomsSUB);


    //BackViewVolume
      int backViewVolume = Settings.System.getInt(this.getContentResolver(), "BackViewVolume", 11);
      setParameters("ctl_backview_vol="+ backViewVolume);

      // AV volume
      String avVolumeName = "av_volume=";
      if (btLock) {
          avVolumeName = "av_phone_volume=";
      }
      int avVolume = Settings.System.getInt(this.getContentResolver(), avVolumeName, KEY_VOLMAX / 2);

      if (SystemProperties.get((String)"ro.product.customer").equalsIgnoreCase("ZD")) {
          if (avVolume > (KEY_VOLMAX *2 /3) ){
                avVolume = (KEY_VOLMAX *2 /3);
          }
      } else {
          if (avVolume > (KEY_VOLMAX *2 /5) ){
              avVolume = (KEY_VOLMAX *2 /5);
          }
      }
      Settings.System.putInt(this.getContentResolver(), avVolumeName, avVolume);

      //GPSVolume
      String gpsVolume = Settings.System.getString(this.getContentResolver(), "av_gps_phone=");
      if (null == gpsVolume) {
          gpsVolume = "0";
      }
      this.setParameters("av_gps_phone=" + gpsVolume);

      this.MTCAdjVolume(2);

      safe = this.getParameters("sta_driving=");

      ipod = this.getParameters("sta_ipod=");
      String s7 = Settings.System.getString(getContentResolver(), "TouchcalResult");
      if (s7 != null) {
          setParameters("sta_touch_cal="+ s7);
      }

      this.initGps();

      initGPUgamma();
  }

  boolean IsSwitchToBT() {
    final boolean equals = SystemProperties.get("ro.product.customer", "HCT").equals("KYD");
    final boolean equals2 = HctUtil.getTopActivityPackageName(this.mContext).equals("com.microntek.sync");
    return !equals && !equals2;
  }

  void LoudSwitch() {
    if (Settings.System.getInt(this.getContentResolver(), "av_lud", 0) == 0) {
      Settings.System.putInt(this.getContentResolver(), "av_lud", 1);
      this.setParameters("av_lud=on");
      this.mToast.setText((CharSequence)this.getResources().getString(R.string.loudon));
    }
    else {
      Settings.System.putInt(this.getContentResolver(), "av_lud", 0);
      this.setParameters("av_lud=off");
      this.mToast.setText((CharSequence)this.getResources().getString(R.string.loudoff));
    }
    this.sendBroadcastAsUser(new Intent("com.microntek.loundchange"), UserHandle.CURRENT_OR_SELF);
    this.mToast.show();
  }

  void MTCAdjVolume(final int volumeAction) {

    int avVolume;
    if (volumeAction != 2) {
      String avVolumeName = "av_volume=";
      if (MicrontekServer.btLock) {
        avVolumeName = "av_phone_volume=";
      }

      avVolume = Settings.System.getInt(this.getContentResolver(), avVolumeName, KEY_VOLMAX / 2);
      if (mCurVolume != avVolume && mCurVolume > 0) {
        avVolume = mCurVolume;
      }
      if (volumeAction == 0) {
        if (avVolume > 0) {
          --avVolume;
          Settings.System.putInt(this.getContentResolver(), avVolumeName, avVolume);
        }
      }
      else if (volumeAction == 1 && avVolume < KEY_VOLMAX) {
        ++avVolume;
        Settings.System.putInt(this.getContentResolver(), avVolumeName, avVolume);
      }
      mCurVolume = avVolume;
      this.setParameters(avVolumeName + HctUtil.mtcGetRealVolume(avVolume, KEY_VOLMAX));
      this.ShowVolumeDalog(avVolume);
    }
    else {
      if (this.mVolumeDialog != null && this.mVolumeDialog.isShowing()) {
        this.mVolumeDialog.dismiss();
      }
      this.mVolHandler.removeMessages(0);
      this.mVolHandler.removeMessages(1);
      int avVolumeTemp = Settings.System.getInt(this.getContentResolver(), "av_volume=", KEY_VOLMAX / 2);
      this.setParameters("av_volume=" + HctUtil.mtcGetRealVolume(avVolumeTemp, KEY_VOLMAX));
        int   avVolumeTemp2 = Settings.System.getInt(this.getContentResolver(), "av_phone_volume=", KEY_VOLMAX / 2);
      this.setParameters("av_phone_volume=" + HctUtil.mtcGetRealVolume(avVolumeTemp2, KEY_VOLMAX));
      if (btLock) {
        avVolume = avVolumeTemp2;
      }
      else {
        avVolume = avVolumeTemp;
      }
      mCurVolume = avVolume;
    }
    this.SendVolStatus1(mCurVolume);
  }

    void MtcStartApp() {
    final String string = Settings.System.getString(this.getContentResolver(), "microntek.lastpackname");
    if (string != null) {
      new Thread((Runnable)new Runnable() {
          public void run() {
              final String[] split = string.split(",");
              Log.i(LOG_TAG, "MTC START APP:" + Arrays.toString(split));

              for (String pkgName : split) {
                  if ((pkgName.equals("com.microntek.travel"))) {
                      startRec(1);
                      try {
                          Thread.sleep(500L);
                      } catch (InterruptedException e) {
                          continue;
                      }
                  } else if (pkgName.equals(GPSPKNAME)) {

                      RunApp(GPSPKNAME);
                      try {
                          Thread.sleep(500L);
                      } catch (InterruptedException e) {
                          continue;
                      }
                  } else if ( null != pkgName){
                      RunApp(pkgName);

                  }
              }

          }
      });
    }
  }

  void MuteSwitch() {
    if (this.getParameters("av_mute=").equals("false")) {
      this.setParameters("av_mute=true");
      this.SendVolStatus(0);
      this.mToast.setText((CharSequence)this.getResources().getString(R.string.muteon));
    }
    else {
      String s;
      if (MicrontekServer.btLock) {
        s = "av_phone_volume=";
      }
      else {
        s = "av_volume=";
      }
      final int int1 = Settings.System.getInt(this.getContentResolver(), s, MicrontekServer.KEY_VOLMAX / 2);
      this.setParameters("av_mute=false");
      this.SendVolStatus(int1);
      this.mToast.setText((CharSequence)this.getResources().getString(R.string.muteoff));
    }
    this.mToast.show();
  }

  private void PowerOffAction() {
    final String[] array = new String[3];

    if (HctUtil.CheckIsRun(this.mContext, "com.microntek.travel")) {

      array[0] = "com.microntek.travel";

    }
    final String runMtcAppPackageName = this.getRunMtcAppPackageName();
    if (runMtcAppPackageName == null) {
      final String topActivityPackageName = HctUtil.getTopActivityPackageName(this.mContext);
      if (!topActivityPackageName.equals("com.microntek.travel")) {

        array[1] = topActivityPackageName;

      }
    }
    else {

      array[2] = runMtcAppPackageName;

      if (MicrontekServer.gps_isfront && MicrontekServer.gps_open) {
        array[2] = MicrontekServer.GPSPKNAME;
      }
    }
    Settings.System.putString(this.getContentResolver(), "microntek.lastpackname", array[0] + "," + array[1] + "," + array[2]);
    final Intent intent = new Intent("com.microntek.bootcheck");
    intent.putExtra("class", "poweroff");
    this.sendBroadcastAsUser(intent, UserHandle.ALL);
    this.startHome();
    ClearProcess.getInstance(this.mContext).ClearManange(0, MicrontekServer.GPSPKNAME);
    if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
      this.mProgressDialog.dismiss();
    }
  }

  private void PowerOffDialog() {
    if (this.mProgressDialog == null) {
      (this.mProgressDialog = new ProgressDialog((Context)this)).setProgressStyle(0);
      this.mProgressDialog.setTitle("Shutting down\\u2026");
      this.mProgressDialog.setMessage("Your Head Unit will shut down.");
      this.mProgressDialog.setIndeterminate(true);
      this.mProgressDialog.setCancelable(false);
      this.mProgressDialog.getWindow().setType(2009);
    }
    if (!this.mProgressDialog.isShowing()) {
      this.mProgressDialog.show();
    }
  }

  void RadioReport(final String s, final int n) {
    final Intent intent = new Intent("com.hct.radio.report");
    intent.putExtra(s, n);
    this.sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
  }

  void RadioReport(final String s, final String s2) {
    final Intent intent = new Intent("com.hct.radio.report");
    intent.putExtra(s, s2);
    this.sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
  }

  void RadioReport(final String s, final byte[] array) {
    final Intent intent = new Intent("com.hct.radio.report");
    intent.putExtra(s, array);
    this.sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
  }

  void ResetSystemData() {
    final int n = 112;
    final Calendar instance = Calendar.getInstance();
    if (instance.getTime().getYear() <= n) {
      instance.setTime(new Date(n, 0, 1, 0, 0));
      SystemClock.setCurrentTimeMillis(instance.getTimeInMillis());
      this.sendBroadcastAsUser(new Intent("android.intent.action.TIME_SET"), UserHandle.CURRENT_OR_SELF);
    }
    if (this.getParameters("sta_function=7").equals("1")) {
      Settings.System.putStringForUser(this.getContentResolver(), "time_12_24", "24", -2);
    }
  }

  void RunApp(final String s) {
    if (s != null) {
      if (s.equals("com.microntek.dvd")) {
        this.startDVD(0);
      }
      else if (s.equals("com.microntek.tv")) {
        this.startDTV(0);
      }
      else if (s.equals("com.microntek.bluetooth")) {
        this.startBT(0);
      }
      else if (s.equals("com.microntek.ipod")) {
        this.startIpod(0);
      }
      else {
        try {
          final Intent launchIntentForPackage = this.getPackageManager().getLaunchIntentForPackage(s);
          if (launchIntentForPackage != null) {
            launchIntentForPackage.addFlags(807534592);
            this.startActivityAsUser(launchIntentForPackage, UserHandle.CURRENT_OR_SELF);
          }
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  void SendHCTBoot() {
    final Intent intent = new Intent("com.microntek.bootcomplete", (Uri)null);
    intent.putExtra("android.intent.extra.user_handle", (Parcelable)UserHandle.ALL);
    intent.addFlags(134217728);
    this.sendBroadcastAsUser(intent, UserHandle.ALL);
  }

  void SendTouchUpdate(final String fileName) {
    final int what = 9;
    final File file = new File(fileName);
    if (file.isFile() && file.exists()) {
      Toast.makeText(this.mContext, (CharSequence)("updata touch config " + this.mTouchCount), Toast.LENGTH_SHORT).show();
      if (this.mTouchCount <= 0) {
        this.mHandler.removeMessages(what);
        new TouchUpdateAsyncTask(this.mContext, fileName).execute((Object[])new Void[0]);
      }
      else {
        --this.mTouchCount;
        this.mHandler.removeMessages(what);
        final Message obtainMessage = this.mHandler.obtainMessage();
        obtainMessage.what = what;
        obtainMessage.obj = fileName;
        this.mHandler.sendMessageDelayed(obtainMessage, 1000L);
      }
    }
  }

  void SendVolStatus(final int n) {
    final Intent intent = new Intent("com.microntek.VOLUME_CHANGED");
    intent.putExtra("volume", n);
    this.sendBroadcastAsUser(intent, UserHandle.ALL);
  }

  void SendVolStatus1(final int n) {
    final int n2 = 5;
    this.mHandler.removeMessages(n2);
    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(n2, n, n), (long)30);
  }

  void ShowVolumeDalog(final int n) {
    if (this.mVolumeDialog == null) {
      (this.mVolumeDialog = new VolumeDialog((Context)this)).setOnDismissListener(new DialogInterface.OnDismissListener() {
          public void onDismiss(DialogInterface paramDialogInterface)
          {
              mVolumeDialog = null;
          }

      });
    }
    else {
      this.mVolumeDialog.SetVolumeDialog(n);
    }

    if (this.mVolumeDialog != null && !this.mVolumeDialog.isShowing() && !getBackViewState())  {
      this.mVolumeDialog.show();
    }
  }

  void StartBtAcc(final boolean b) {
    final Intent intent = new Intent("com.microntek.systemacc");
    final String s = "type";
    String s2;
    if (b) {
      s2 = "on";
    }
    else {
      s2 = "off";
    }
    intent.putExtra(s, s2);
    this.sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
  }

  void TVReport(final String s, final int n) {
    final Intent intent = new Intent("com.hct.tv.report");
    intent.putExtra(s, n);
    this.sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
  }

  void TVReport(final String s, final String s2) {
    final Intent intent = new Intent("com.hct.tv.report");
    intent.putExtra(s, s2);
    this.sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
  }

   void TickTask() {
    ++this.ScreenSaverTimer;
    if (this.ScreenSaverEnableLocal) {
      this.ScreenSaverTimeOut = Settings.System.getInt(this.getContentResolver(), "musicscreen_timeout", 30);
    }
    if (MicrontekServer.btLock || MicrontekServer.gps_isfront || this.BackViewState || this.PowerState != 2 || this.ScreenSaverTimeOut <= 0 || !this.ScreenSaverEnable) {
      this.ScreenSaverTimer = 0;
    }
    if (this.ScreenSaverTimer < this.ScreenSaverTimeOut || this.ScreenSaverTimeOut <= 0) {
      this.sendBroadcastAsUser(new Intent("com.microntek.endclock"), UserHandle.CURRENT_OR_SELF);
      this.ScreenSaverOn = false;
    }
    else if (!this.ScreenSaverOn && this.ScreenSaverTimer >= this.ScreenSaverTimeOut) {
      this.ScreenSaverOn = true;
      if (this.ScreenSaverEnableLocal) {
        this.startMusicClock();
      }
      else {
        final Intent intent = new Intent("com.microntek.screensaver");
        intent.putExtra("timer", this.ScreenSaverTimer);
        this.sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
      }
    }
    this.mHandler.sendEmptyMessageDelayed(0, 1000L);
    this.sendBroadcastAsUser(new Intent("com.microntek.time.fresh"), UserHandle.CURRENT_OR_SELF);
  }

  void _setHctBacklight(int brightness) {
    if (brightness > 255) {
      brightness = 255;
    }
    if (brightness < 0) {
      brightness = 0;
    }
    this.setParameters("cfg_backlight=" + brightness);
    this.setBrightness(brightness);
    Settings.System.putInt(this.getContentResolver(), "screen_brightness", brightness);
  }

  private void backotherappTop() {
    final List<ActivityManager.RunningTaskInfo> runningTasks = ((ActivityManager)this.getSystemService(ACTIVITY_SERVICE)).getRunningTasks(3);
    if (runningTasks.size() < 2) {
      this.startHome();
    }
    else {
      final ComponentName topActivity = runningTasks.get(1).topActivity;
      final Intent intent = new Intent("android.intent.action.MAIN");
      intent.setComponent(topActivity);
      intent.addFlags(807534592);
      try {
        this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
      }
      catch (Exception ex) {
        this.startHome();
      }
    }
  }

    int checkModeAPPName(final String appNAme) {
    for (int i = 0; i < Constant.ModeAppList.length; ++i) {
      if (appNAme.equals(Constant.ModeAppList[i])) {
        return i;
      }
    }
    return -1;
  }

  private boolean checkPKName(final String s) {
    for (int i = 0; i < Constant.pknameList.length; ++i) {
      if (s.equals(Constant.pknameList[i])) {
        return true;
      }
    }
    return false;
  }

  void clearMusicClock() {
    final int int1 = Settings.System.getInt(this.getContentResolver(), "musicscreen_timeout", 30);
    this.mHandler.removeMessages(4);
    if (int1 != -1) {
      this.ScreenSaverTimer = 0;
    }
  }

  private void cmdProc(final byte[] array, final int n, final int n2) {
      serialDataProcess.cmdProc(array, n, n2);
  }

  void collapseStatusBar(final Context context) {

    try {
      //noinspection WrongConstant
      final Object systemService = context.getSystemService(STATUS_BAR_SERVICE);

      try {
        Method method;
        if (Build.VERSION.SDK_INT <= 16) {
          method = systemService.getClass().getMethod("collapse", (Class<?>[])new Class[0]);
        }
        else {
          method = systemService.getClass().getMethod("collapsePanels", (Class<?>[])new Class[0]);
        }
        method.invoke(systemService, new Object[0]);
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    catch (Exception ex2) {}
  }

  String getParameters(final String s) {
    return ((AudioManager)this.getSystemService(AUDIO_SERVICE)).getParameters(s);
  }

  private String getRunMtcAppPackageName() {
    String s = null;
    final Iterator<ActivityManager.RunningTaskInfo> iterator = (Iterator<ActivityManager.RunningTaskInfo>)((ActivityManager)this.getSystemService(ACTIVITY_SERVICE)).getRunningTasks(30).iterator();
    while (iterator.hasNext()) {
      final String packageName = iterator.next().topActivity.getPackageName();
      if (this.checkPKName(packageName)) {
        s = packageName;
      }
    }
    return s;
  }

  int getmediaAppflag() {
    int n = 0;
    for (final ActivityManager.RunningTaskInfo runningTaskInfo : ((ActivityManager)this.getSystemService(ACTIVITY_SERVICE)).getRunningTasks(10)) {
      if (runningTaskInfo.topActivity.getPackageName().equals("com.microntek.music")) {
        n = 1;
        break;
      }
      if (runningTaskInfo.topActivity.getPackageName().equals("com.microntek.media")) {
        n = 2;
        break;
      }
    }
    return n;
  }

  private void initGPUgamma() {

    final int contrast = Integer.parseInt(SystemProperties.get("sys.graphic.contrast", "100"));
    final int saturation = Integer.parseInt(SystemProperties.get("sys.graphic.saturation","100"));
    final int luminance = Integer.parseInt(SystemProperties.get("sys.graphic.luminance", "100"));
    final IBinder surfaceFlinger = ServiceManager.getService("SurfaceFlinger");
    if (surfaceFlinger == null) {
      return;
    }
    try {
      final Parcel obtain = Parcel.obtain();
      obtain.writeInterfaceToken("android.ui.ISurfaceComposer");
      final Parcel parcel = obtain;
      try {
        parcel.writeInt(luminance);
        final Parcel parcel2 = obtain;
        try {
          parcel2.writeInt(saturation);
          final Parcel parcel3 = obtain;
          try {
            parcel3.writeInt(contrast);
            surfaceFlinger.transact(2016, obtain, (Parcel)null, 0);
            obtain.recycle();
          }
          catch (RemoteException ex) {
            Log.e("MicrontekServer", "Failed to set color adjust", (Throwable)ex);
          }
        }
        catch (Exception ex2) {}
      }
      catch (Exception ex3) {}
    }
    catch (Exception ex4) {}
  }

  void initGps() {
    MicrontekServer.GPSPKNAME = Settings.System.getString(this.getContentResolver(), "gpspkname");
    if (MicrontekServer.GPSPKNAME == null) {
      MicrontekServer.GPSPKNAME = "";
    }
    this.setParameters("av_gps_package=" + MicrontekServer.GPSPKNAME);
    String string = Settings.System.getString(this.getContentResolver(), "av_gps_monitor=");
    if (string == null || string == "") {
      string = "on";
    }
    this.setParameters("av_gps_monitor=" + string);
    String string2 = Settings.System.getString(this.getContentResolver(), "av_gps_switch=");
    if (string2 == null || string2 == "") {
      string2 = "139";
    }
    this.setParameters("av_gps_switch=" + string2);
    String string3 = Settings.System.getString(this.getContentResolver(), "av_gps_gain=");
    if (string3 == null || string3 == "") {
      string3 = "on";
    }
    this.setParameters("av_gps_gain=" + string3);
  }

  void powerOff() {
    this.mHandler.removeCallbacks(this.PowerLongPress);
    this.mHandler.post(this.PowerLongPress);
    MicrontekServer.mPoweron = false;
    this.PowerOffAction();
    this.sendBroadcastAsUser(new Intent("android.intent.action.SYNC"), UserHandle.CURRENT_OR_SELF);
    this.sendBroadcastAsUser(new Intent("com.goodocom.gocsdk.INIT_SUCCEED"), UserHandle.CURRENT_OR_SELF);
    final Intent intent = new Intent("com.microntek.canbusdisplay");
    intent.putExtra("type", "off");
    this.sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
    this.mAppMode = -1;
    this.setParameters("rpt_power=false");
    final Intent intent2 = new Intent("com.microntek.power");
    intent2.putExtra("type", "off");
    this.sendBroadcastAsUser(intent2, UserHandle.CURRENT_OR_SELF);
  }

  void powerOn() {
    final int n = 3;
    final boolean b = true;
    this.mHandler.removeCallbacks(this.PowerLongPress);
    if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
      this.mProgressDialog.dismiss();
    }
    MicrontekServer.mPoweron = b;
    if (SystemProperties.get("ro.product.customer").startsWith("KLD")) {
      MicrontekServer.deviceLock = b;
      this.mHandler.removeMessages(n);
      this.mHandler.sendEmptyMessageDelayed(n, 8000L);
    }
    final Intent intent = new Intent("com.microntek.canbusdisplay");
    intent.putExtra("type", "on");
    this.sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
    this.setParameters("rpt_power=true");
    final Intent intent2 = new Intent("com.microntek.power");
    intent2.putExtra("type", "on");
    this.sendBroadcastAsUser(intent2, UserHandle.CURRENT_OR_SELF);
  }

  void powerReboot() {
    this.mHandler.removeCallbacks(this.PowerLongPress);
    this.mHandler.post(this.PowerLongPress);
    MicrontekServer.mPoweron = false;
    this.mHandler.removeCallbacks(this.PowerRebootRunnable);
    this.mHandler.postDelayed(this.PowerRebootRunnable, 3000L);
  }

  void powershutdown() {
       new Thread() {
          @Override
          public void run() {
              IPowerManager iPowerManager = IPowerManager.Stub.asInterface((IBinder) ServiceManager.getService(POWER_SERVICE));
              try {
                  iPowerManager.shutdown(false, false);
                  return;
              } catch (RemoteException v0) {
                  return;
              }
          }


      };
  }

  private void setBackViewState(final int n) {
    Settings.System.putInt(this.getContentResolver(), "microntek.backview.state", n);
    if (n == 1 && this.mVolumeDialog != null && this.mVolumeDialog.isShowing()) {
      this.mVolumeDialog.dismiss();
    }
    final Intent intent = new Intent("com.hct.backview.report");
    intent.putExtra("state", n);
    this.sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
  }

  private void setBrightness(int temporaryScreenBrightnessSettingOverride) {
    if (temporaryScreenBrightnessSettingOverride < 10) {
      temporaryScreenBrightnessSettingOverride = 10;
    }
    final String s = "power";
    try {
      final IBinder service = ServiceManager.getService(s);
      try {
        final IPowerManager interface1 = IPowerManager.Stub.asInterface(service);
        if (interface1 != null) {
          interface1.setTemporaryScreenBrightnessSettingOverride(temporaryScreenBrightnessSettingOverride);
        }
      }
      catch (RemoteException ex) {}
    }
    catch (Exception ex2) {}
  }

  void setParameters(final String parameters) {
    ((AudioManager)this.getSystemService(AUDIO_SERVICE)).setParameters(parameters);
  }

  private void setVolume(final int n, final boolean b) {
    String s;
    if (MicrontekServer.btLock) {
      s = "av_phone_volume=";
    }
    else {
      s = "av_volume=";
    }
    if (b) {
      Settings.System.putInt(this.getContentResolver(), s, n);
    }
    this.setParameters(s + HctUtil.mtcGetRealVolume(n, MicrontekServer.KEY_VOLMAX));
  }

  int startAux(final int n) {
    final int n2 = 1;
    boolean b = false;
    final Intent intent = new Intent("android.intent.action.MAIN");
    intent.setComponent(new ComponentName("com.microntek.avin", "com.microntek.avin.AVINActivity"));
    if (n == n2 && (MicrontekServer.gps_isfront ? 1 : 0) == n2) {
      intent.putExtra("start", n2);
      b = true;
    }
    intent.addFlags(807600128);
    try {
      this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
      return b ? 1 : 0;
    }
    catch (Exception ex) {
      return b ? 1 : 0;
    }
  }

  int startBT(final int n) {
    final int n2 = 1;
    int n3 = 0;
    if (!HctUtil.getTopActivityClassName(this.mContext).equals("com.microntek.bluetooth.BlueToothActivity")) {
      final Intent intent = new Intent("android.intent.action.MAIN");
      intent.setComponent(new ComponentName("com.microntek.bluetooth", "com.microntek.bluetooth.BlueToothActivity"));
      if (n != 0) {
        intent.putExtra("nowapplication", ((ActivityManager)this.getSystemService(ACTIVITY_SERVICE)).getRunningTasks(n2).get(0).topActivity.getPackageName());
      }
      intent.addFlags(807534592);
      while (true) {
        try {
          this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
          n3 = n2;
        }
        catch (Exception ex) {
          continue;
        }
        break;
      }
    }
    return n3;
  }

  void startBackView(final String s) {
    final Intent intent = new Intent("android.intent.action.MAIN");
    intent.setComponent(new ComponentName("com.microntek.backview", "com.microntek.backview.BackViewActivity"));
    intent.addFlags(807600128);
    intent.putExtra("rightBackview", s);
    try {
      this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
    }
    catch (Exception ex) {}
  }

  int startDTV(final int n) {
    int n2;
    if (HctUtil.getTopActivityClassName(this.mContext).equals("com.microntek.tv.MainActivity")) {
      n2 = 0;
    }
    else {
      final Intent intent = new Intent("android.intent.action.MAIN");
      intent.setComponent(new ComponentName("com.microntek.tv", "com.microntek.tv.MainActivity"));
      intent.addFlags(807534592);
      while (true) {
        try {
          this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
          n2 = 1;
        }
        catch (Exception ex) {
          continue;
        }
        break;
      }
    }
    return n2;
  }

  int startDVD(final int n) {
    final int n2 = 1;
    int n3 = 0;
    int n4;
    if (HctUtil.getTopActivityClassName(this.mContext).equals("com.microntek.dvd.DVDActivity")) {
      n4 = 0;
    }
    else {
      final Intent intent = new Intent("android.intent.action.MAIN");
      intent.setComponent(new ComponentName("com.microntek.dvd", "com.microntek.dvd.DVDActivity"));
      if (n == n2 && (MicrontekServer.gps_isfront ? 1 : 0) == n2) {
        intent.putExtra("start", n2);
        n3 = 1;
      }
      intent.addFlags(807600128);
      while (true) {
        try {
          this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
          n4 = n3;
        }
        catch (Exception ex) {
          continue;
        }
        break;
      }
    }
    return n4;
  }

  void startGFsel() {
    if (HctUtil.getTopActivityClassName(this.mContext).equals("com.microntek.ampsetup.MainActivity")) {
      this.sendBroadcastAsUser(new Intent("com.microntek.ampclose"), UserHandle.CURRENT_OR_SELF);
    }
    else {
      final Intent intent = new Intent("android.intent.action.MAIN");
      intent.addCategory("android.intent.category.LAUNCHER");
      intent.setComponent(new ComponentName("com.microntek.ampsetup", "com.microntek.ampsetup.MainActivity"));
      intent.addFlags(807534592);
      try {
        this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
      }
      catch (Exception ex) {}
    }
  }

  void startGPS() {
    if (MicrontekServer.gps_isfront) {
      if (!MicrontekServer.gps_open) {
        if (MicrontekServer.GPSPKNAME == null || MicrontekServer.GPSPKNAME.length() == 0 || !HctUtil.isPackageApplicationEnabled(this.mContext, MicrontekServer.GPSPKNAME)) {
          this.RunApp("com.microntek.navgear");
        }
        else {
          this.RunApp(MicrontekServer.GPSPKNAME);
        }
      }
      else {
        final List runningTasks = ((ActivityManager)this.getSystemService(ACTIVITY_SERVICE)).getRunningTasks(40);
        int checkModeAPPName = -1;
        final Iterator<ActivityManager.RunningTaskInfo> iterator = runningTasks.iterator();
        while (iterator.hasNext()) {
          checkModeAPPName = this.checkModeAPPName(iterator.next().topActivity.getPackageName());
          if (checkModeAPPName >= 0) {
            break;
          }
        }
        switch (checkModeAPPName) {
          default: {
            this.backotherappTop();
            break;
          }
          case 0: {
            this.startRadio(0);
            break;
          }
          case 1: {
            this.startDVD(0);
            break;
          }
          case 2: {
            this.startMusic(null, 0);
            break;
          }
          case 3: {
            this.startMovie(0);
            break;
          }
          case 4: {
            this.startIpod(0);
            break;
          }
          case 5: {
            this.startAux(0);
            break;
          }
        }
      }
    }
    else if (MicrontekServer.GPSPKNAME == null || MicrontekServer.GPSPKNAME.length() == 0 || !HctUtil.isPackageApplicationEnabled(this.mContext, MicrontekServer.GPSPKNAME)) {
      this.RunApp("com.microntek.navgear");
    }
    else {
      this.RunApp(MicrontekServer.GPSPKNAME);
    }
  }

  void startHome() {
    final Intent intent = new Intent("android.intent.action.MAIN");
    intent.addCategory("android.intent.category.HOME");
    intent.addFlags(270532608);
    try {
      this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
    }
    catch (Exception ex) {}
  }

  int startIpod(final int n) {
    final int n2 = 1;
    int n3 = 0;
    int n4;
    if (HctUtil.getTopActivityClassName(this.mContext).equals("com.microntek.ipod.IPODActivity")) {
      n4 = 0;
    }
    else {
      final Intent intent = new Intent("android.intent.action.MAIN");
      intent.setComponent(new ComponentName("com.microntek.ipod", "com.microntek.ipod.IPODActivity"));
      if ((n == n2 && (MicrontekServer.gps_isfront ? 1 : 0) == n2) || n == 2) {
        intent.putExtra("start", n2);
        n3 = 1;
      }
      intent.addFlags(807600128);
      while (true) {
        try {
          this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
          n4 = n3;
        }
        catch (Exception ex) {
          continue;
        }
        break;
      }
    }
    return n4;
  }

  int startMovie(final int n) {
    final int n2 = 1;
    boolean b = false;
    final Intent intent = new Intent("android.intent.action.MAIN");
    intent.setComponent(new ComponentName("com.microntek.media", "com.microntek.media.MediaActivity"));
    if (n == n2 && (MicrontekServer.gps_isfront ? 1 : 0) == n2) {
      intent.putExtra("start", n2);
      b = true;
    }
    intent.addFlags(807600128);
    try {
      this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
      return b ? 1 : 0;
    }
    catch (Exception ex) {
      return b ? 1 : 0;
    }
  }

  int startMusic(final String s, final int n) {
    final int n2 = 1;
    boolean b = false;
    final Intent intent = new Intent("android.intent.action.MAIN");
    intent.setComponent(new ComponentName("com.microntek.music", "com.microntek.music.MusicActivity"));
    if (s != null) {
      intent.putExtra("dev", s);
    }
    if ((n == n2 && (MicrontekServer.gps_isfront ? 1 : 0) == n2) || n == 2) {
      intent.putExtra("start", n2);
      b = true;
    }
    intent.addFlags(807600128);
    try {
      this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
      return b ? 1 : 0;
    }
    catch (Exception ex) {
      return b ? 1 : 0;
    }
  }

  void startMusicClock() {
    if (SystemProperties.get("ro.product.customer").startsWith("RM")) {
      if (MicrontekServer.btLock || MicrontekServer.gps_isfront) {
        this.sendBroadcastAsUser(new Intent("com.microntek.active"), UserHandle.CURRENT_OR_SELF);
      }
      else {
        final String topActivityPackageName = HctUtil.getTopActivityPackageName(this.mContext);
        if (topActivityPackageName.equals("com.microntek.music") || topActivityPackageName.equals("com.microntek.radio") || topActivityPackageName.equals("com.microntek.ipod") || topActivityPackageName.equals("com.microntek.bluetooth") || topActivityPackageName.equals("com.android.settings") || topActivityPackageName.startsWith("com.android.launcher")) {
          final Intent intent = new Intent("android.intent.action.MAIN");
          intent.setComponent(new ComponentName("com.microntek.screenclock", "com.microntek.screenclock.MainActivity"));
          intent.addFlags(807600128);
          try {
            final Context mContext = this.mContext;
            try {
              mContext.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
            }
            catch (Exception ex) {}
          }
          catch (Exception ex2) {}
        }
      }
    }
  }

  int startRadio(final int n) {


    int radioStatus = 0;
    if (HctUtil.getTopActivityClassName(this.mContext).equals("com.microntek.radio.RadioActivity")) {
        return radioStatus;
    }
    else {
      final Intent intent = new Intent("android.intent.action.MAIN");
      intent.setComponent(new ComponentName("com.microntek.radio", "com.microntek.radio.RadioActivity"));
      if ((n == 1 && (MicrontekServer.gps_isfront) || n == 2)) {
        intent.putExtra("start", 1);
          radioStatus = 1;
      }
      intent.addFlags(807600128);
      while (true) {
        try {
          this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);

        }
        catch (Exception ex) {
          continue;
        }
        break;
      }
    }
    return radioStatus;
  }

  int startRadio(final int n, final String s) {
    final int n2 = 1;
    boolean b = false;
    final Intent intent = new Intent("android.intent.action.MAIN");
    intent.setComponent(new ComponentName("com.microntek.radio", "com.microntek.radio.RadioActivity"));
    if ((n == n2 && (MicrontekServer.gps_isfront ? 1 : 0) == n2) || n == 2) {
      intent.putExtra("start", n2);
      b = true;
    }
    intent.putExtra("freq", s);
    intent.addFlags(807600128);
    try {
      this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
      return b ? 1 : 0;
    }
    catch (Exception ex) {
      return b ? 1 : 0;
    }
  }

  private void startRec(final int n) {
    final Intent intent = new Intent("android.intent.action.MAIN");
    intent.addCategory("android.intent.category.LAUNCHER");
    intent.putExtra("start", n);
    intent.setComponent(new ComponentName("com.microntek.travel", "com.microntek.travel.MainActivity"));
    intent.addFlags(807534592);
    try {
      this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
    }
    catch (Exception ex) {}
  }

  void startSYNC() {
    final Intent intent = new Intent("android.intent.action.MAIN");
    intent.setComponent(new ComponentName("com.microntek.sync", "com.microntek.sync.MainActivity"));
    intent.addFlags(807600128);
    try {
      this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
    }
    catch (Exception ex) {}
  }

  void startScreenShot() {
    final Intent intent = new Intent();
    intent.setAction("rk.android.screenshot.action");
    this.sendBroadcastAsUser(intent, UserHandle.ALL);
  }

  void startSettings() {
    final Intent intent = new Intent("android.intent.action.MAIN");
    intent.addCategory("android.intent.category.LAUNCHER");
    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings"));
    intent.addFlags(807534592);
    try {
      this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
    }
    catch (Exception ex) {}
  }

  void startSpeech() {
    final Intent intent = new Intent("com.microntek.prompt");
    intent.putExtra("speech", "on");
    this.sendBroadcastAsUser(intent, UserHandle.ALL);
  }

  void startTouchKeyStudy() {
    final Intent intent = new Intent();
    intent.setComponent(new ComponentName("com.hct.factory", "com.hct.factory.TouchKeyStudy"));
    intent.putExtra("common", "hcttouch");
    try {
      this.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
    }
    catch (Exception ex) {}
  }

  public int GetVolume() {
    String s;
    if (MicrontekServer.btLock) {
      s = "av_phone_volume=";
    }
    else {
      s = "av_volume=";
    }
    return MicrontekServer.mCurVolume = Settings.System.getInt(this.getContentResolver(), s, MicrontekServer.KEY_VOLMAX / 2);
  }

  public void OnChangeVolume(final int mVolumeTemp) {
    this.mVolHandler.removeMessages(0);
    this.mVolHandler.removeMessages(1);
    this.mVolumeTemp = mVolumeTemp;
    if (mVolumeTemp == MicrontekServer.mCurVolume) {
      String s;
      if (MicrontekServer.btLock) {
        s = "av_phone_volume=";
      }
      else {
        s = "av_volume=";
      }
      Settings.System.putInt(this.getContentResolver(), s, mVolumeTemp);
    }
    else {
      int n;
      if (mVolumeTemp < MicrontekServer.mCurVolume) {
        n = 0;
      }
      else {
        n = 1;
      }
      this.mVolHandler.sendEmptyMessage(n);
    }
  }

  public boolean getBackViewState() {
    int int1 = Settings.System.getInt(this.getContentResolver(), "microntek.backview.state", 0);
    if (int1 < 1) {
      return false;
    } else {
      return true;
    }
  }

  public IBinder onBind(final Intent intent) {
    return null;
  }

  public void onCreate() {
    final boolean mPoweron = true;
    super.onCreate();
    this.mContext = this.getApplicationContext();
    (this.mToast = Toast.makeText((Context)this, (CharSequence)"", Toast.LENGTH_SHORT)).setGravity(17, 0, 0);
    this.setBackViewState(0);
    this.mHandler.sendEmptyMessageDelayed(3, 8000L);
    if (SystemProperties.get("ro.product.screenclock").equals("true")) {
      this.ScreenSaverEnableLocal = mPoweron;
      this.ScreenSaverEnable = mPoweron;
      this.ScreenSaverTimeOut = Settings.System.getInt(this.getContentResolver(), "musicscreen_timeout", 30);
    }
    this.mHandler.sendEmptyMessageDelayed(0, 1000L);
    try {
      try {
        final SerialManager mSerialManager = new SerialManager();
          this.mSerialManager = mSerialManager;
        try {
            this.mSerialManager.openPort(6, 38400);
          this.mSerialManager.updateReceiver(6, new SerialReceiver() {
              @Override
              public void onSerialReceived(byte[] bytes) {
                  serialDataProcess.DataProc(bytes, bytes.length);
              }
          });
          this.InitSystemData();
          final IntentFilter intentFilter = new IntentFilter();
          intentFilter.addAction("android.intent.action.MEDIA_MOUNTED");
          intentFilter.addAction("android.intent.action.MEDIA_EJECT");
          intentFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
          intentFilter.addDataScheme("file");
          this.registerReceiver(this.MediaDetectReceiver, intentFilter);
          final IntentFilter intentFilter2 = new IntentFilter();
          intentFilter2.addAction("com.microntek.irkeyDown");
          this.registerReceiver(this.CarkeyProc, intentFilter2);
          final IntentFilter intentFilter3 = new IntentFilter();
          intentFilter3.addAction("com.microntek.beep");
          intentFilter3.addAction("com.microntek.setVolume");
          intentFilter3.addAction("com.microntek.active");
          intentFilter3.addAction("com.microntek.clear");
          intentFilter3.addAction("com.microntek.radio.power");
          intentFilter3.addAction("com.microntek.playmusic");
          intentFilter3.addAction("com.microntek.ipod.play");
          intentFilter3.addAction("com.microntek.widget.btplay");
          intentFilter3.addAction("com.microntek.prompt");
          intentFilter3.addAction("com.microntek.app");
          intentFilter3.addAction("com.microntek.VOLUME_SET");
          intentFilter3.addAction("com.microntek.BLIGHT_SET");
          intentFilter3.addAction("com.microntek.closepackage");
          intentFilter3.addAction("com.microntek.beepclearscreen");
          this.registerReceiver(this.MTCAPPProc, intentFilter3);
          final IntentFilter intentFilter4 = new IntentFilter();
          intentFilter4.addAction("com.microntek.actstart");
          intentFilter4.addAction("com.microntek.actdestroy");
          intentFilter4.addAction("com.microntek.gpschange");
          intentFilter4.addAction("com.microntek.biglight.state");
          intentFilter4.addAction("com.microntek.bt.report");
          intentFilter4.addAction("ACTION_IVCAR_OPERATION");
          intentFilter4.addAction("com.microntek.hctreboot");
          this.registerReceiver(this.MTCploy, intentFilter4);
          MicrontekServer.mPoweron = mPoweron;
        }
        catch (Exception ex) {}
      }
      catch (Exception ex2) {}
    }
    catch (Exception ex3) {}
  }

  public void onStart(final Intent intent, final int n) {
    final int n2 = 8;
    MicrontekServer.runtime = SystemClock.uptimeMillis();
    this.setParameters("rpt_boot_complete=true");
    this.mHandler.removeMessages(n2);
    this.mHandler.sendEmptyMessageDelayed(n2, 4000L);
  }

    public boolean isBackViewState() {
        return BackViewState;
    }

    public Context getmContext() {
        return mContext;
    }

    public String getLOG_TAG() {
        return LOG_TAG;
    }

    public int getPowerState() {
        return PowerState;
    }

    public String getmAjXTopPackage() {
        return mAjXTopPackage;
    }

    public void setBackViewState(boolean backViewState) {
        BackViewState = backViewState;
    }

    public void setPowerState(int powerState) {
        PowerState = powerState;
    }

    public void setmAjXTopPackage(String mAjXTopPackage) {
        this.mAjXTopPackage = mAjXTopPackage;
    }
}
