package android.microntek.service;

import android.content.Intent;
import android.os.SystemClock;
import android.os.UserHandle;

public class MicrontekServer_KeyFunctions {
    private final MicrontekServer microntekServer;
    private final MicrontekServer_KeyFunctions_ModeSwitch modeSwitch = new MicrontekServer_KeyFunctions_ModeSwitch();

    public MicrontekServer_KeyFunctions(MicrontekServer microntekServer) {
        this.microntekServer = microntekServer;
    }

    void DoPressKeyTask(final int n) {
        final int n2 = 1;
        switch (n) {
            case 365: {
                if (microntekServer.keyFunctions.checkLastSwitchTime() && !MicrontekServer.btLock) {
                    microntekServer.sendBroadcastAsUser(new Intent("com.microntek.recent"), UserHandle.CURRENT_OR_SELF);
                    break;
                }
                break;
            }
            case 281: {
                microntekServer.MTCAdjVolume(0);
                break;
            }
            case 273: {
                microntekServer.MTCAdjVolume(n2);
                break;
            }
            case 256: {
                if (microntekServer.keyFunctions.checkLastSwitchTime() && !MicrontekServer.btLock) {
                    microntekServer.keyFunctions.ModeSwitch(microntekServer);
                    break;
                }
                break;
            }
            case 258: {
                microntekServer.MuteSwitch();
                break;
            }
            case 277: {
                if (microntekServer.keyFunctions.checkLastSwitchTime() && !MicrontekServer.btLock) {
                    microntekServer.startGFsel();
                    break;
                }
                break;
            }
            case 296: {
                if (microntekServer.keyFunctions.checkLastSwitchTime() && !MicrontekServer.btLock) {
                    microntekServer.startDVD(0);
                    break;
                }
                break;
            }
            case 297: {
                if (microntekServer.keyFunctions.checkLastSwitchTime() && !MicrontekServer.btLock) {
                    microntekServer.startRadio(0);
                    break;
                }
                break;
            }
            case 298: {
                microntekServer.LoudSwitch();
                break;
            }
            case 316: {
                if (!MicrontekServer.btLock && microntekServer.IsSwitchToBT()) {
                    microntekServer.startBT(n2);
                    break;
                }
                break;
            }
            case 327: {
                if (!MicrontekServer.btLock && microntekServer.IsSwitchToBT()) {
                    microntekServer.startBT(n2);
                    break;
                }
                break;
            }
            case 332: {
                microntekServer.IRsetBrightness();
                break;
            }
            case 303: {
                microntekServer.EQSwitch();
                break;
            }
            case 304: {
                if (microntekServer.keyFunctions.checkLastSwitchTime() && !MicrontekServer.btLock) {
                    microntekServer.startBT(n2);
                    break;
                }
                break;
            }
            case 305: {
                if (microntekServer.keyFunctions.checkLastSwitchTime()) {
                    microntekServer.startGPS();
                    break;
                }
                break;
            }
            case 319: {
                if (microntekServer.keyFunctions.checkLastSwitchTime() && !MicrontekServer.btLock) {
                    microntekServer.startAux(0);
                    break;
                }
                break;
            }
            case 310: {
                microntekServer.startHome();
                break;
            }
            case 320: {
                if (microntekServer.keyFunctions.checkLastSwitchTime() && !MicrontekServer.btLock) {
                    microntekServer.startDTV(0);
                    break;
                }
                break;
            }
            case 321: {
                microntekServer.startSettings();
                break;
            }
            case 331: {
                microntekServer.startMusic(null, 0);
                break;
            }
            case 336: {
                if (microntekServer.keyFunctions.checkLastSwitchTime() && !MicrontekServer.btLock) {
                    microntekServer.startMovie(0);
                    break;
                }
                break;
            }
            case 337: {
                if (microntekServer.keyFunctions.checkLastSwitchTime() && !MicrontekServer.btLock) {
                    microntekServer.startIpod(0);
                    break;
                }
                break;
            }
            case 339: {
                if (microntekServer.keyFunctions.checkLastSwitchTime()) {
                    microntekServer.startScreenShot();
                    break;
                }
                break;
            }
            case 344: {
                microntekServer.startSpeech();
                break;
            }
            case 362: {
                if (microntekServer.keyFunctions.checkLastSwitchTime() && !MicrontekServer.btLock) {
                    microntekServer.startSYNC();
                    break;
                }
                break;
            }
        }
    }

    void ModeSwitch(MicrontekServer microntekServer) {

        modeSwitch.SwitchMode(microntekServer);
    }

    boolean checkLastSwitchTime() {
      final long uptimeMillis = SystemClock.uptimeMillis();
      boolean b;
      if (uptimeMillis - MicrontekServer.runtime > 1000L) {
        MicrontekServer.runtime = uptimeMillis;
        b = true;
      }
      else {
        b = false;
      }
      return b;
    }
}