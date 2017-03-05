package android.microntek.service;

import android.content.ComponentName;
import android.content.Intent;
import android.microntek.HctUtil;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

public class MicrontekServer_SerialDataProcess {
    private final MicrontekServer microntekServer;

    public MicrontekServer_SerialDataProcess(MicrontekServer microntekServer) {
        this.microntekServer = microntekServer;
    }

    void DataProc(final byte[] array, int n) {
        final char c = (char) (-1);
        final int n2 = 1024;
        if (MicrontekServer.bufRevCount + n >= n2) {
            MicrontekServer.bufRevCount = 0;
            if (n >= n2) {
                n = 1024;
            }
        }
        for (int i = 0; i < n; ++i) {
            MicrontekServer.serialRevBuf[MicrontekServer.bufRevCount + i] = array[i];
        }
        n += MicrontekServer.bufRevCount;
        int j = 0;
        while (j < n) {
            final int framePos = HctUtil.getFramePos(MicrontekServer.serialRevBuf, j, n);
            if (framePos == -1) {
                break;
            }
            final char c2 = (char) (framePos & c);
            final char c3 = (char) (framePos >> 16 & c);
            j = c3 + '\u0001';
            cmdProc(MicrontekServer.serialRevBuf, c2, c3);
        }
        MicrontekServer.bufRevCount = n - j;
        for (int k = 0; k < MicrontekServer.bufRevCount; ++k) {
            MicrontekServer.serialRevBuf[k] = MicrontekServer.serialRevBuf[k + j];
        }
    }

    void cmdProc(final byte[] array, final int n, final int n2) {
        final int mainCmd = array[n + 1] & 0xFF;
        final int subCmd = array[n + 2] & 0xFF;
        Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : " + array);
        MainSwitch:
        {
            switch (mainCmd) {
                //RADIO
                case 17: {
                    switch (subCmd) {
                        default: {
                            break;
                        }
                        //Radio seek end
                        case 32: {
                            microntekServer.RadioReport("seek", "end");
                            break MainSwitch;
                        }
                        //Radio seek start
                        case 51: {
                            microntekServer.RadioReport("seek", "start");
                            break MainSwitch;
                        }
                        //Radio seek found
                        case 33: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : strength " + HctUtil.getInt(array, 5, 1));
                            microntekServer.RadioReport("strength", HctUtil.getInt(array, 5, 1));
                            microntekServer.RadioReport("seek", "found");
                            break MainSwitch;
                        }
                        //Radio seek autoend
                        case 34: {
                            microntekServer.RadioReport("seek", "autoend");
                            break MainSwitch;
                        }
                        //Radio seek autostart
                        case 52: {
                            microntekServer.RadioReport("seek", "autostart");
                            break MainSwitch;
                        }
                        //Radio seek autofound
                        case 53: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : strength " + HctUtil.getInt(array, 5, 1));
                            microntekServer.RadioReport("strength", HctUtil.getInt(array, 5, 1));
                            microntekServer.RadioReport("seek", "autofound");
                            break MainSwitch;
                        }
                        //Radio report title
                        case 35: {
                            HctUtil.getInt(array, n + 5, n2 - n);
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : title " + HctUtil.getAsciiByteArray(array, n + 5, 8));
                            microntekServer.RadioReport("title", HctUtil.getAsciiByteArray(array, n + 5, 8));
                            break MainSwitch;
                        }
                        //Radio report freq
                        case 37: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : freq " + HctUtil.getInt2(array, n + 5, 3) * 1000);
                            microntekServer.RadioReport("freq", HctUtil.getInt2(array, n + 5, 3) * 1000);
                            break MainSwitch;
                        }
                        //Radio report tp
                        case 36: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : tp " + HctUtil.getInt(array, n + 5, 1));
                            microntekServer.RadioReport("tp", HctUtil.getInt(array, n + 5, 1));
                            break MainSwitch;
                        }
                        //Radio report ta on
                        case 49: {
                            microntekServer.RadioReport("ta", 1);
                            break MainSwitch;
                        }
                        //Radio report ta of
                        case 50: {
                            microntekServer.RadioReport("ta", 0);
                            break MainSwitch;
                        }
                        //Radio report psn
                        case 38: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : psn " + HctUtil.getAsciiByteArray(array, n + 5, 8));
                            microntekServer.RadioReport("psn", HctUtil.getAsciiByteArray(array, n + 5, 8));
                            break MainSwitch;
                        }
                        //Radio report pi
                        case 40: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : psn " + HctUtil.getInt(array, n + 5, 1));
                            microntekServer.RadioReport("pi", HctUtil.getInt(array, n + 5, 1));
                            break MainSwitch;
                        }
                        //Radio report pty
                        case 39: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : psn " + HctUtil.getInt(array, n + 5, 1));
                            microntekServer.RadioReport("pty", HctUtil.getInt(array, n + 5, 1));
                            break MainSwitch;
                        }
                        //Radio report stereo
                        case 41: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : stereo " + HctUtil.getInt(array, n + 5, 1));
                            microntekServer.RadioReport("stereo", HctUtil.getInt(array, n + 5, 1));
                            break MainSwitch;
                        }
                    }

                }
                //TV
                case 18: {
                    switch (subCmd) {
                        case 12: {
                            final int int2 = HctUtil.getInt2(array, n + 3, 2);
                            final int int3 = HctUtil.getInt2(array, n + 5, 2);
                            final int int4 = HctUtil.getInt2(array, n + 7, 2);
                            final String asciiString = HctUtil.getAsciiString(array, n + 9, int2 - 4);
                            final Intent intent = new Intent("com.hct.tvlist.report");
                            intent.putExtra("tv.allcnt", int3);
                            intent.putExtra("tv.index", int4);
                            intent.putExtra("tv.item", asciiString);
                            microntekServer.sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
                            break MainSwitch;
                        }
                        case 18: {
                            microntekServer.TVReport("freq", HctUtil.getInt2(array, n + 5, 2));
                            break MainSwitch;
                        }
                        case 15: {
                            microntekServer.TVReport("seek", "end");
                            break MainSwitch;
                        }
                        case 17: {
                            microntekServer.TVReport("seek", "autoend");
                            break MainSwitch;
                        }
                        case 16: {
                            microntekServer.TVReport("seek", "found");
                            break MainSwitch;
                        }
                        case 21: {
                            microntekServer.TVReport("seek", "autofound");
                            break MainSwitch;
                        }
                        case 19: {
                            microntekServer.TVReport("seek", "start");
                            break MainSwitch;
                        }
                        case 20: {
                            microntekServer.TVReport("seek", "autostart");
                            break MainSwitch;
                        }
                    }
                    break;
                }
                //Keyevent
                case 22: {
                    switch (subCmd) {
                        case 100: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : startTouchKeyStudy");
                            microntekServer.startTouchKeyStudy();
                            break;
                        }
                        case 96:
                        case 97: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : com.hct.key.study" + " : " + HctUtil.getInt2(array, n + 5, 2));
                            final int int5 = HctUtil.getInt2(array, n + 5, 2);
                            final Intent intent2 = new Intent("com.hct.key.study");
                            intent2.putExtra("keyCode", int5);
                            microntekServer.sendBroadcastAsUser(intent2, UserHandle.CURRENT_OR_SELF);
                            break;
                        }
                        case 98:
                        case 99: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : com.hct.swkey.study" + " : " + HctUtil.getInt2(array, n + 5, 2));
                            final int int6 = HctUtil.getInt2(array, n + 5, 2);
                            final Intent intent3 = new Intent("com.hct.swkey.study");
                            intent3.putExtra("keyCode", int6);
                            microntekServer.sendBroadcastAsUser(intent3, UserHandle.CURRENT_OR_SELF);
                            break;
                        }
                        case 64: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : poweron if powerstate != 2 : " + microntekServer.getPowerState());
                            if (microntekServer.getPowerState() != 2) {
                                microntekServer.powerOn();
                                microntekServer.setPowerState(2);
                            }
                            microntekServer.StartBtAcc(true);
                            break;
                        }
                        case 65: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : poweroff 1 if powerstate = 2 : " + microntekServer.getPowerState());
                            if (microntekServer.getPowerState() == 2) {
                                microntekServer.powerOff();
                            }
                            microntekServer.StartBtAcc(false);
                            microntekServer.setPowerState(1);
                            break;
                        }
                        case 73: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : poweroff 0 if powerstate = 2 : " + microntekServer.getPowerState());
                            if (microntekServer.getPowerState() == 2) {
                                microntekServer.powerOff();
                            }
                            microntekServer.StartBtAcc(false);
                            microntekServer.setPowerState(0);
                            break;
                        }
                        case 72: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : powershutdown : " + microntekServer.getPowerState());
                            microntekServer.powershutdown();
                            break;
                        }
                        case 81: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : MtcStartApp() if no btLock : " + MicrontekServer.btLock);
                            if (!MicrontekServer.btLock) {
                                microntekServer.MtcStartApp();
                                break;
                            }
                            break;
                        }
                        case 78: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : com.microntek.irkeyDown : " + HctUtil.getInt2(array, n + 5, 2));
                            final int int7 = HctUtil.getInt2(array, n + 5, 2);
                            final Intent intent4 = new Intent("com.microntek.irkeyDown");
                            intent4.putExtra("keyCode", int7);
                            microntekServer.sendBroadcastAsUser(intent4, UserHandle.CURRENT_OR_SELF);
                            microntekServer.clearMusicClock();
                            break;
                        }
                        case 67: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : start backview com.microntek.smallbtoff");
                            microntekServer.setBackViewState(true);
                            microntekServer.sendBroadcastAsUser(new Intent("com.microntek.smallbtoff"), UserHandle.CURRENT_OR_SELF);
                            microntekServer.startBackView("backview");
                            microntekServer.setBackViewState(true);
                            break;
                        }
                        case 68: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : end backview com.microntek.smallbton");
                            microntekServer.setBackViewState(false);
                            microntekServer.sendBroadcastAsUser(new Intent("com.microntek.backviewend"), UserHandle.CURRENT_OR_SELF);
                            microntekServer.sendBroadcastAsUser(new Intent("com.microntek.smallbton"), UserHandle.CURRENT_OR_SELF);
                            microntekServer.setBackViewState(false);
                            break;
                        }
                        case 69: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : starthome  set ajxserver com.microntek.smallbtoff");
                            microntekServer.sendBroadcastAsUser(new Intent("com.microntek.smallbtoff"), UserHandle.CURRENT_OR_SELF);
                            final Intent intent5 = new Intent();
                            intent5.setComponent(new ComponentName("android.microntek.canbus", "android.microntek.canbus.Ajxserver"));
                            microntekServer.startServiceAsUser(intent5, UserHandle.OWNER);
                            microntekServer.setmAjXTopPackage(HctUtil.getTopActivityPackageName(microntekServer.getmContext()));
                            microntekServer.startHome();
                            break;
                        }
                        case 70: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : run ajxserver com.microntek.smallbton");
                            microntekServer.sendBroadcastAsUser(new Intent("com.microntek.ajx"), UserHandle.CURRENT_OR_SELF);
                            microntekServer.sendBroadcastAsUser(new Intent("com.microntek.smallbton"), UserHandle.CURRENT_OR_SELF);
                            microntekServer.RunApp(microntekServer.getmAjXTopPackage());
                            microntekServer.setmAjXTopPackage(null);
                            break;
                        }
                        case 79: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : startDVD");
                            microntekServer.startDVD(0);
                            break;
                        }
                        case 110: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : resetSystemData, system putint: microntek.firstboot=128");
                            Settings.System.putInt(microntekServer.getContentResolver(), "microntek.firstboot", 128);
                            microntekServer.ResetSystemData();
                            break;
                        }
                        case 74: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " :com.microntek.carlight state : " + HctUtil.getInt2(array, n + 5, 1));
                            final Intent intent6 = new Intent("com.microntek.carlight");
                            final int int8 = HctUtil.getInt2(array, n + 5, 1);
                            final String s = "state";
                            String s2;
                            if (int8 == 0) {
                                s2 = "true";
                            } else {
                                s2 = "false";
                            }
                            intent6.putExtra(s, s2);
                            microntekServer.sendBroadcastAsUser(intent6, UserHandle.CURRENT_OR_SELF);
                            break;
                        }
                        case 75: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " :com.microntek.carstatechange : " + MicrontekServer.safe);
                            MicrontekServer.safe = microntekServer.getParameters("sta_driving=");
                            final Intent intent7 = new Intent("com.microntek.carstatechange");
                            intent7.putExtra("type", "SAFE");
                            microntekServer.sendBroadcastAsUser(intent7, UserHandle.CURRENT_OR_SELF);
                            break;
                        }
                        case 76: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " :start ipod if true : " + microntekServer.getParameters("sta_ipod="));
                            MicrontekServer.ipod = microntekServer.getParameters("sta_ipod=");
                            if (MicrontekServer.mPoweron && MicrontekServer.ipod.equals("true")) {
                                microntekServer.startIpod(0);
                                break;
                            }
                            break;
                        }
                        case 80: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " :start ipod if true : " + microntekServer.getParameters("sta_ipod="));
                            MicrontekServer.ipod = microntekServer.getParameters("sta_ipod=");
                            if (MicrontekServer.mPoweron && MicrontekServer.ipod.equals("true")) {
                                microntekServer.startIpod(0);
                                break;
                            }
                            break;
                        }
                    }
                    Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : collapseStatusBar() ");
                    microntekServer.collapseStatusBar(microntekServer.getmContext());
                    break;
                }
                //DVD msg
                case 19: {
                    if (subCmd == 18) {
                        Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : com.microntek.dvdMsg keyCode : " + HctUtil.getInt2(array, n + 5, 2));
                        final Intent intent8 = new Intent("com.microntek.dvdMsg");
                        intent8.putExtra("keyCode", HctUtil.getInt2(array, n + 5, 2));
                        microntekServer.sendBroadcastAsUser(intent8, UserHandle.CURRENT_OR_SELF);
                    } else if (subCmd == 2) {
                        Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : com.microntek.dvdOpenRequest");
                        microntekServer.sendBroadcastAsUser(new Intent("com.microntek.dvdOpenRequest"), UserHandle.CURRENT_OR_SELF);
                        break;
                    }
                    Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : collapseStatusBar() ");
                    microntekServer.collapseStatusBar(microntekServer.getmContext());
                    break;
                }
                //Touch key / Video signal change
                case 31: {
                    switch (subCmd) {
                        default: {
                            break MainSwitch;
                        }
                        case 0: {

                            final int int9 = HctUtil.getInt2(array, n + 3, 2);
                            final Intent intent9 = new Intent("com.hct.touch.report");
                            if (int9 == 1) {
                                intent9.putExtra("touch.key", HctUtil.getInt2(array, n + 5, 1));
                                Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : ccom.hct.touch.report touch.key: " + HctUtil.getInt2(array, n + 3, 2));
                            } else {
                                intent9.putExtra("touch.point", new int[]{HctUtil.getInt2(array, n + 5, 2), HctUtil.getInt2(array, n + 7, 2)});
                                Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : ccom.hct.touch.report touch.point: " + HctUtil.getInt2(array, n + 5, 2) + " : " + HctUtil.getInt2(array, n + 7, 2));
                            }
                            microntekServer.sendBroadcastAsUser(intent9, UserHandle.CURRENT_OR_SELF);
                            break MainSwitch;
                        }
                        case 2: {
                            final Intent intent10 = new Intent("com.microntek.videosignalchange");
                            final int int10 = HctUtil.getInt(array, n + 5, 2);
                            final int n5 = int10 >> 8 & 0xFF;
                            final int n6 = int10 & 0xFF;
                            intent10.putExtra("sta", n5 == 1);
                            if (n6 == 1) {
                                intent10.putExtra("type", "backview");
                            } else if (n6 == 2) {
                                intent10.putExtra("type", "avin");
                            } else if (n6 == 3) {
                                intent10.putExtra("type", "tv");
                            } else if (n6 == 4) {
                                intent10.putExtra("type", "dvr");
                            } else {
                                intent10.putExtra("type", "null");
                            }
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : com.microntek.videosignalchange : " + intent10.getExtra("sta") + " : " + intent10.getExtra("type"));
                            microntekServer.sendBroadcastAsUser(intent10, UserHandle.CURRENT_OR_SELF);
                            break MainSwitch;
                        }
                        case 3:
                        case 4: {
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : clearMusicClock()");
                            microntekServer.clearMusicClock();
                            break MainSwitch;
                        }
                    }

                }
                //Vol change
                case 16: {
                    switch (subCmd) {
                        default: {
                            break MainSwitch;
                        }
                        case 20: {
                            int int11 = 0;
                            if (HctUtil.getInt(array, n + 5, 1) == 0) {
                                String s3;
                                if (MicrontekServer.btLock) {
                                    s3 = "av_phone_volume=";
                                } else {
                                    s3 = "av_volume=";
                                }
                                Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : sendVolstatus() :" + s3 + int11);
                                int11 = Settings.System.getInt(microntekServer.getContentResolver(), s3, MicrontekServer.KEY_VOLMAX / 2);
                            }
                            microntekServer.SendVolStatus1(MicrontekServer.mCurVolume = int11);
                            break MainSwitch;
                        }
                    }

                }
                case 21: {
                    //Canbusdata
                    switch (subCmd) {
                        default: {
                            break MainSwitch;
                        }
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9: {
                            final int int12 = HctUtil.getInt2(array, n + 3, 2);
                            final int int13 = HctUtil.getInt2(array, n + 5, 2);
                            final int int14 = HctUtil.getInt2(array, n + 7, 2);
                            final String asciiString2 = HctUtil.getAsciiString(array, n + 9, int12 - 4);
                            final Intent intent11 = new Intent("com.hct.canbuslist.report");
                            intent11.putExtra("canbus.listtype", subCmd - 1);
                            intent11.putExtra("canbus.allcnt", int13);
                            intent11.putExtra("canbus.index", int14);
                            intent11.putExtra("canbus.item", asciiString2);
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : com.hct.canbuslist.report : canbus.item=" + asciiString2 + " : canbus.index=" + int14 + " : canbus.allcnt=" + int13 + " : canbus.listtype=" + (subCmd - 1));
                            microntekServer.sendBroadcastAsUser(intent11, UserHandle.CURRENT_OR_SELF);
                            break MainSwitch;
                        }
                        case 24: {
                            final Intent intent12 = new Intent("com.hct.canbuslist.report");
                            intent12.putExtra("canbus.listend", "end");
                            Log.i(microntekServer.getLOG_TAG(), "cmdProc: " + mainCmd + " : " + subCmd + " : com.hct.canbuslist.report : canbus.listend=end");
                            microntekServer.sendBroadcastAsUser(intent12, UserHandle.CURRENT_OR_SELF);
                            break MainSwitch;
                        }
                    }

                }
            }
        }
    }
}