package android.microntek.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by TheUnknown12 on 5.3.2017.
 */
class MicrontekServer_CarKeyReceiver extends BroadcastReceiver {
    private MicrontekServer microntekServer;

    public MicrontekServer_CarKeyReceiver(MicrontekServer microntekServer) {
        this.microntekServer = microntekServer;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.microntek.irkeyDown") && intent.hasExtra("keyCode")) {
            Log.i(microntekServer.LOG_TAG, "CarkeyProc: com.microntek.irkeyDown: " + intent.getIntExtra("keyCode", -1));
            microntekServer.keyFunctions.DoPressKeyTask(intent.getIntExtra("keyCode", -1));
        }
    }
}
