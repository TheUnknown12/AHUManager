package android.microntek.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.microntek.ClearProcess;
import android.microntek.HctUtil;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by TheUnknown12 on 5.3.2017.
 */
class MicrontekServer_ApplicationReceiver extends BroadcastReceiver {
    private MicrontekServer microntekServer;

    public MicrontekServer_ApplicationReceiver(MicrontekServer microntekServer) {
        this.microntekServer = microntekServer;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals("com.microntek.beep")) {
            Log.i(microntekServer.LOG_TAG, "MTCAPPProc: " + action + " : setParam: ctl_beep=1");
            microntekServer.setParameters("ctl_beep=1");
            microntekServer.clearMusicClock();
        } else if (action.equals("com.microntek.beepclearscreen")) {
            Log.i(microntekServer.LOG_TAG, "MTCAPPProc: " + action + " : clearMusicClock()");
            microntekServer.clearMusicClock();
        } else if (action.equals("com.microntek.prompt")) {
            if (intent.hasExtra("package")) {
                Log.i(microntekServer.LOG_TAG, "MTCAPPProc: " + action + " : " + intent.getStringExtra("package") + " : setParam: av_voiceprompt_package");
                microntekServer.setParameters("av_voiceprompt_package=" + intent.getStringExtra("package"));
            }
            if (intent.hasExtra("state")) {
                Log.i(microntekServer.LOG_TAG, "MTCAPPProc: " + action + " : " + intent.getStringExtra("state") + " : setParam: av_voiceprompt_on=");
                if (intent.getStringExtra("state").equals("on")) {
                    microntekServer.setParameters("av_voiceprompt_on=true");
                    microntekServer.mHandler.removeMessages(7);
                    microntekServer.mHandler.sendEmptyMessageDelayed(7, 3000L);
                } else {
                    microntekServer.setParameters("av_voiceprompt_on=false");
                }
            }
        } else if (action.equals("com.microntek.VOLUME_SET")) {
            int n = MicrontekServer.mCurVolume;
            if (intent.hasExtra("type")) {
                Log.i(microntekServer.LOG_TAG, "MTCAPPProc: " + action + " : " + intent.getStringExtra("type") + " : addorsub volume");
                final String stringExtra = intent.getStringExtra("type");
                if (stringExtra.equals("add")) {
                    n = MicrontekServer.mCurVolume + MicrontekServer.KEY_VOLMAX / 10;
                    if (n > MicrontekServer.KEY_VOLMAX) {
                        n = MicrontekServer.KEY_VOLMAX;
                    }
                } else if (stringExtra.equals("sub")) {
                    n = MicrontekServer.mCurVolume - MicrontekServer.KEY_VOLMAX / 10;
                    if (n < 0) {
                        n = 0;
                    }
                }
            } else {
                if (!intent.hasExtra("volume")) {
                    return;
                }
                Log.i(microntekServer.LOG_TAG, "MTCAPPProc: " + action + " : " + intent.getIntExtra("volume", 50) + " : setvolume");
                n = MicrontekServer.KEY_VOLMAX * intent.getIntExtra("volume", 50) / 100;
            }
            microntekServer.OnChangeVolume(n);
        } else if (action.equals("com.microntek.BLIGHT_SET")) {
            Log.i(microntekServer.LOG_TAG, "MTCAPPProc: " + action + " : " + intent.getIntExtra("level", 100) + " : _setHctBacklight");
            microntekServer._setHctBacklight(intent.getIntExtra("level", 100));
        } else if (action.equals("com.microntek.app")) {
            if (intent.hasExtra("app")) {
                Log.i(microntekServer.LOG_TAG, "MTCAPPProc: " + action + " : " + intent.getStringExtra("app") + " : start");
                final String stringExtra2 = intent.getStringExtra("app");
                if (stringExtra2.equals("music")) {
                    microntekServer.startMusic(null, 0);
                } else if (stringExtra2.equals("movie")) {
                    microntekServer.startMovie(0);
                } else if (stringExtra2.equals("radio")) {
                    int intExtra = -1;
                    if (intent.hasExtra("extra")) {
                        intExtra = intent.getIntExtra("extra", -1);
                    }
                    if (intExtra != -1) {
                        Log.i(microntekServer.LOG_TAG, "MTCAPPProc: " + action + " : " + intExtra + " : startRadio with int");
                        microntekServer.startRadio(0, "" + intExtra);
                    } else {
                        microntekServer.startRadio(0);
                    }
                } else if (stringExtra2.equals("dvd")) {
                    microntekServer.startDTV(0);
                } else if (stringExtra2.equals("navi") && !MicrontekServer.gps_isfront) {
                    if (MicrontekServer.GPSPKNAME == null || MicrontekServer.GPSPKNAME.length() == 0 || !HctUtil.isPackageApplicationEnabled(microntekServer.mContext, MicrontekServer.GPSPKNAME)) {
                        microntekServer.RunApp("com.microntek.navgear");
                    } else {
                        microntekServer.RunApp(MicrontekServer.GPSPKNAME);
                    }
                }
            }
        } else if (action.equals("com.microntek.setVolume")) {
            String s;
            if (MicrontekServer.btLock) {

                s = "av_phone_volume=";
            } else {
                s = "av_volume=";
            }
            MicrontekServer.mCurVolume = Settings.System.getInt(microntekServer.getContentResolver(), s, MicrontekServer.KEY_VOLMAX / 2);
            Log.i(microntekServer.LOG_TAG, "MTCAPPProc: " + action + " : " + s + " : " + MicrontekServer.mCurVolume + " : ShowVolumeDialog");
            microntekServer.ShowVolumeDalog(MicrontekServer.mCurVolume);
        } else if (!action.equals("com.microntek.active")) {
            if (action.equals("com.microntek.clear")) {
                Log.i(microntekServer.LOG_TAG, "MTCAPPProc: " + action + " : om.microntek.speedstart");
                if (!ClearProcess.getInstance(context).getBusy()) {
                    context.sendBroadcastAsUser(new Intent("com.microntek.speedstart"), UserHandle.CURRENT_OR_SELF);
                    ClearProcess.getInstance(context).ClearManange(1, MicrontekServer.GPSPKNAME);
                    context.sendBroadcastAsUser(new Intent("com.microntek.speedend"), UserHandle.CURRENT_OR_SELF);
                }
            } else if (action.equals("com.microntek.closepackage")) {
                if (intent.hasExtra("package")) {
                    Log.i(microntekServer.LOG_TAG, "MTCAPPProc: " + action + " : " + intent.getStringExtra("package"));
                    final String stringExtra3 = intent.getStringExtra("package");
                    if (stringExtra3.equals("com.microntek.radio") || stringExtra3.equals("com.microntek.dvd") || stringExtra3.equals("com.microntek.music") || stringExtra3.equals("com.microntek.ipod") || stringExtra3.equals("com.microntek.tv") || stringExtra3.equals("com.microntek.photo") || stringExtra3.equals("com.microntek.media") || stringExtra3.equals("com.microntek.bluetooth") || stringExtra3.equals("com.microntek.travel") || stringExtra3.equals("com.microntek.weather")) {
                        if (HctUtil.CheckIsRun(context, stringExtra3)) {
                            if (stringExtra3.equals("com.microntek.bluetooth") && !HctUtil.getTopActivityClassName(context).equals("com.microntek.bluetooth.BtMusicActivity")) {
                                microntekServer.startHome();
                            }
                            final Intent intent2 = new Intent("com.microntek.bootcheck");
                            intent2.putExtra("class", "android.microntek.service");
                            context.sendBroadcastAsUser(intent2, UserHandle.CURRENT_OR_SELF);
                        }
                    } else {
                        ClearProcess.getInstance(context).closepackage(stringExtra3);
                    }
                }
            } else if (action.equals("com.microntek.radio.power")) {
                Log.i(microntekServer.LOG_TAG, "MTCAPPProc: " + action + " : Start radio if not running");
                if (!HctUtil.CheckIsRun(context, "com.microntek.radio")) {
                    microntekServer.startRadio(2);
                }
            } else if (action.equals("com.microntek.playmusic")) {
                Log.i(microntekServer.LOG_TAG, "MTCAPPProc: " + action + " : " + microntekServer.getmediaAppflag() + "Start music if not 1");
                if (microntekServer.getmediaAppflag() != 1) {
                    microntekServer.startMusic(null, 2);
                }
            } else if (action.equals("com.microntek.ipod.play")) {
                Log.i(microntekServer.LOG_TAG, "MTCAPPProc: " + action + " : Start ipod if not running");
                if (!HctUtil.CheckIsRun(context, "com.microntek.ipod")) {
                    microntekServer.startIpod(2);
                }
            } else if (action.equals("com.microntek.widget.btplay")) {
                Log.i(microntekServer.LOG_TAG, "MTCAPPProc: " + action + " : widget.btplay does nothing");
            }
        }
    }
}
