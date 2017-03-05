package android.microntek.service;

import android.app.ActivityManager;
import android.content.Context;
import android.microntek.Constant;
import android.microntek.HctUtil;

import java.util.Iterator;
import java.util.List;

public class MicrontekServer_KeyFunctions_ModeSwitch {
    public MicrontekServer_KeyFunctions_ModeSwitch() {
    }

    void SwitchMode(MicrontekServer microntekServer) {

        final List runningTasks = ((ActivityManager) microntekServer.getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(40);
        int mAppMode = -1;
        final Iterator<ActivityManager.RunningTaskInfo> iterator = runningTasks.iterator();
        while (iterator.hasNext()) {
            mAppMode = microntekServer.checkModeAPPName(iterator.next().topActivity.getPackageName());
            if (mAppMode >= 0) {
                break;
            }
        }
        if (mAppMode == -1) {
            mAppMode = microntekServer.mAppMode;
        }
        do {
            mAppMode = (mAppMode + 1) % Constant.ModeAppList.length;
        }
        while (!HctUtil.isPackageApplicationEnabled(microntekServer.mContext, Constant.ModeAppList[mAppMode]) || mAppMode == microntekServer.mAppMode);
        switch (microntekServer.mAppMode = mAppMode) {
            default:
            case 0: {
                if (microntekServer.startRadio(1) == 1) {
                    microntekServer.mToast.setText(R.string.radio);
                    microntekServer.mToast.setGravity(17, 0, -100);
                    microntekServer.mToast.show();
                    break;
                }
                break;
            }
            case 2: {
                if (microntekServer.startDVD(1) == 1) {
                    microntekServer.mToast.setText(R.string.dvd);
                    microntekServer.mToast.setGravity(17, 0, -100);
                    microntekServer.mToast.show();
                    break;
                }
                break;
            }
            case 1: {
                if (microntekServer.startMusic(null, 1) == 1) {
                    microntekServer.mToast.setText(R.string.music);
                    microntekServer.mToast.setGravity(17, 0, -100);
                    microntekServer.mToast.show();
                    break;
                }
                break;
            }
            case 3: {
                if (microntekServer.startMovie(1) == 1) {
                    microntekServer.mToast.setText(R.string.media);
                    microntekServer.mToast.setGravity(17, 0, -100);
                    microntekServer.mToast.show();
                    break;
                }
                break;
            }
            case 4: {
                if (microntekServer.startIpod(1) == 1) {
                    microntekServer.mToast.setText(R.string.ipod);
                    microntekServer.mToast.setGravity(17, 0, -100);
                    microntekServer.mToast.show();
                    break;
                }
                break;
            }
            case 5: {
                if (microntekServer.startAux(1) == 1) {
                    microntekServer.mToast.setText(R.string.avin);
                    microntekServer.mToast.setGravity(17, 0, -100);
                    microntekServer.mToast.show();
                    break;
                }
                break;
            }
        }
    }
}