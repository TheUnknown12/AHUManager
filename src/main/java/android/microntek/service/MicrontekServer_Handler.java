package android.microntek.service;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by TheUnknown12 on 5.3.2017.
 */
class MicrontekServer_Handler extends Handler {
    private MicrontekServer microntekServer;
    String LOG_TAG = "MicrontekServer_Handler";
    public MicrontekServer_Handler(MicrontekServer microntekServer) {
        this.microntekServer = microntekServer;
    }

    public void handleMessage(final Message message) {
        super.handleMessage(message);
        switch (message.what) {
            case 0: {
                microntekServer.TickTask();
                Log.i(LOG_TAG, "TickTask");
                break;
            }
            case 1: {
                if (MicrontekServer.gps_open && MicrontekServer.gps_isfront) {
                    microntekServer.initGps();
                    Log.i(LOG_TAG, "initGPS + av_gps_ontop=true");
                    microntekServer.setParameters("av_gps_ontop=true");
                    break;
                }
                break;
            }
            case 2: {
                if (!MicrontekServer.gps_open || !MicrontekServer.gps_isfront) {
                    Log.i(LOG_TAG, "av_gps_ontop=false");
                    microntekServer.setParameters("av_gps_ontop=false");
                    break;
                }
                break;
            }
            case 7: {
                Log.i(LOG_TAG, "av_voiceprompt_on=false");
                microntekServer.setParameters("av_voiceprompt_on=false");
                break;
            }
            case 3: {
                Log.i(LOG_TAG, "deviceLock=false");
                MicrontekServer.deviceLock = false;
                break;
            }
            case 4: {
                Log.i(LOG_TAG, "startMusicClock");
                microntekServer.startMusicClock();
                break;
            }
            case 5: {
                Log.i(LOG_TAG, "SendVolStatus");
                microntekServer.SendVolStatus(message.arg1);

                break;
            }
            case 8: {
                Log.i(LOG_TAG, "SendHCTBoot");
                microntekServer.SendHCTBoot();
                break;
            }
            case 9: {
                Log.i(LOG_TAG, "SendTouchUpdate");
                microntekServer.SendTouchUpdate(message.obj.toString());
                break;
            }
        }
    }
}
