package android.microntek.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;

/**
 * Created by TheUnknown12 on 5.3.2017.
 */
class MicrontekServer_PhoneReceiver extends BroadcastReceiver {
    private MicrontekServer microntekServer;

    public MicrontekServer_PhoneReceiver(MicrontekServer microntekServer) {
        this.microntekServer = microntekServer;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals("com.microntek.bt.report")) {
            Log.i(microntekServer.LOG_TAG, "MTCploy: com.microntek.bt.report: " + intent.getIntExtra("connect_state", 0));
            if (intent.hasExtra("connect_state")) {
                final int intExtra = intent.getIntExtra("connect_state", 0);
                if (intExtra == 2 || intExtra == 3 || intExtra == 5) {
                    if (intExtra == 2) {
                        microntekServer.setParameters("av_phone=out");

                    } else if (intExtra == 3) {
                        microntekServer.setParameters("av_phone=in");
                    } else {
                        microntekServer.setParameters("av_phone=answer");
                    }
                    if (!MicrontekServer.btLock) {
                        MicrontekServer.btLock = true;
                        final Intent intent2 = new Intent("com.microntek.bootcheck");
                        intent2.putExtra("class", "phonecallin");
                        context.sendBroadcastAsUser(intent2, UserHandle.CURRENT_OR_SELF);
                        microntekServer.MTCAdjVolume(2);
                        context.sendBroadcastAsUser(new Intent("com.microntek.active"), UserHandle.CURRENT_OR_SELF);
                    }
                } else if (MicrontekServer.btLock) {
                    MicrontekServer.btLock = false;
                    microntekServer.setParameters("av_phone=hangup");
                    final Intent intent3 = new Intent("com.microntek.bootcheck");
                    intent3.putExtra("class", "phonecallout");
                    context.sendBroadcastAsUser(intent3, UserHandle.CURRENT_OR_SELF);
                    microntekServer.MTCAdjVolume(2);
                }
            }
        } else if (action.equals("com.microntek.biglight.state")) {
            Log.i(microntekServer.LOG_TAG, "MTCploy: com.microntek.biglight.state: " + microntekServer.getParameters("sta_ill="));
            final Intent intent4 = new Intent("com.microntek.carlight");
            intent4.putExtra("state", microntekServer.getParameters("sta_ill="));
            context.sendBroadcastAsUser(intent4, UserHandle.CURRENT_OR_SELF);
        } else if (action.equals("com.microntek.hctreboot")) {
            Log.i(microntekServer.LOG_TAG, "MTCploy: com.microntek.hctreboot: powerReboot()");
            microntekServer.powerReboot();
        } else if (action.equals("ACTION_IVCAR_OPERATION")) {
            Log.i(microntekServer.LOG_TAG, "MTCploy: ACTION_IVCAR_OPERATION" + intent.getStringExtra("OPERATION") + intent.getStringExtra("freq"));
            if (intent.getStringExtra("OPERATION").equals("REDIO_PLAY")) {
                String stringExtra = "";
                if (intent.hasExtra("freq")) {
                    stringExtra = intent.getStringExtra("freq");
                }
                if (stringExtra.length() > 0) {
                    microntekServer.startRadio(1, stringExtra);
                } else {
                    microntekServer.startRadio(1);
                }
            }
        } else {
            Log.i(microntekServer.LOG_TAG, "MTCploy: " + action + " : " + intent.getStringExtra("pkname"));
            final String stringExtra2 = intent.getStringExtra("pkname");
            if (stringExtra2 != null) {
                if (action.equals("com.microntek.actstart")) {
                    if (MicrontekServer.GPSPKNAME.equals(stringExtra2)) {
                        MicrontekServer.gps_open = true;
                        MicrontekServer.gps_isfront = true;
                        microntekServer.mHandler.sendEmptyMessage(1);
                    } else if (stringExtra2.equals("com.google.android.youtube")) {
                        final Intent intent5 = new Intent("com.microntek.bootcheck");
                        intent5.putExtra("class", "toutube");
                        context.sendBroadcastAsUser(intent5, UserHandle.CURRENT_OR_SELF);
                        microntekServer.setParameters("av_channel_enter=sys");
                    } else {
                        MicrontekServer.gps_isfront = false;
                        microntekServer.mHandler.sendEmptyMessageDelayed(2, 1000L);
                    }
                } else if (action.equals("com.microntek.actdestroy")) {
                    if (MicrontekServer.GPSPKNAME.equals(stringExtra2)) {
                        MicrontekServer.gps_open = false;
                        microntekServer.mHandler.sendEmptyMessage(2);
                    } else if (stringExtra2.equals("com.microntek.dvd")) {
                        Log.i(microntekServer.LOG_TAG, "MTCploy: " + action + " : " + stringExtra2 + " : setParam: av_channel_exit=dvd");
                        microntekServer.setParameters("av_channel_exit=dvd");
                    }
                } else if (action.equals("com.microntek.gpschange")) {
                    MicrontekServer.GPSPKNAME = stringExtra2;
                    Log.i(microntekServer.LOG_TAG, "MTCploy: " + action + " : " + stringExtra2 + " : setParam: av_gps_package=");
                    microntekServer.setParameters("av_gps_package=" + MicrontekServer.GPSPKNAME);
                }
            }
        }
    }
}
