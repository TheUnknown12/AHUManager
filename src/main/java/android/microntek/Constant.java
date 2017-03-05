package android.microntek;

import android.microntek.service.R;

public class Constant
{
  public static final String[] ModeAppList;
  public static final int[] music_Style;
  public static final String[] pknameList;
  public static final String[] pkAudioList;

  static {

    music_Style = new int[]{R.string.music_style0,R.string.music_style1,R.string.music_style2,R.string.music_style3,R.string.music_style4,R.string.music_style5,R.string.music_style6};
    //added "com.microntek.tv","com.microntek.btMusic" in Android 6.0
    pknameList = new String[]{"com.microntek.avin","com.microntek.dvr","com.microntek.dvd","com.microntek.ntv","com.microntek.atv","com.microntek.tv","com.microntek.media","com.microntek.music","com.microntek.radio","com.microntek.ipod","com.microntek.bluetooth","com.microntek.btMusic","com.microntek.civxusb"};
    //added in Android 6.0
    pkAudioList = new String[]{"com.microntek.avin","com.microntek.dvr","com.microntek.dvd","com.microntek.tv","com.microntek.media","com.microntek.music","com.microntek.radio","com.microntek.ipod","com.microntek.btMusic"};
    //ModeAppList = new String[]{ "com.microntek.radio","com.microntek.dvd","com.microntek.music","com.microntek.media", "com.microntek.ipod","com.microntek.avin"};
    ModeAppList = new String[]{ "com.microntek.radio","com.microntek.music"};

  }
}