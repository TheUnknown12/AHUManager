package android.microntek.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.microntek.HctUtil;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import java.io.File;

/**
 * Created by TheUnknown12 on 5.3.2017.
 */
class MicrontekServer_MediaDetectReceiver extends BroadcastReceiver {
    private MicrontekServer microntekServer;

    public MicrontekServer_MediaDetectReceiver(MicrontekServer microntekServer) {
        this.microntekServer = microntekServer;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final int what = 9;
        final int mAutoPlay = Settings.System.getInt(microntekServer.getContentResolver(), "MusicAutoPlayEN", 0);

        if (mAutoPlay != 0 && !MicrontekServer.btLock && !MicrontekServer.deviceLock && MicrontekServer.mPoweron && !microntekServer.getBackViewState()) {
            final String action = intent.getAction();
            if (action.equals("android.intent.action.MEDIA_MOUNTED")) {
                Log.i(microntekServer.LOG_TAG, "MediaDetectReceiver: MEDIA_MOUNTED");
                final String path = intent.getData().getPath();
                final String string = path + "/gt9xx_update.cfg";
                final File file = new File(string);
                if (file.isFile() && file.exists()) {
                    Log.i(microntekServer.LOG_TAG, "MediaDetectReceiver: file found " + string);
                    microntekServer.mTouchCount = 5;
                    microntekServer.mHandler.removeMessages(what);
                    final Message obtainMessage = microntekServer.mHandler.obtainMessage();
                    obtainMessage.what = what;
                    obtainMessage.obj = string;
                    microntekServer.mHandler.sendMessageDelayed(obtainMessage, 500L);
                }
                final String musicDevice = Util.getMusicDevice(path);
                if (musicDevice != null && !musicDevice.equals("FLASH") && !musicDevice.equals("SD1")) {
                    microntekServer.clearMusicClock();
                    if (!HctUtil.getTopActivityPackageName(context).equals("com.microntek.music")) {
                        microntekServer.startMusic(musicDevice, 0);
                    }
                }
            } else if (action.equals("android.intent.action.MEDIA_UNMOUNTED") || action.equals("android.intent.action.MEDIA_EJECT")) {
                microntekServer.mHandler.removeMessages(what);
            }
        }
    }
}
