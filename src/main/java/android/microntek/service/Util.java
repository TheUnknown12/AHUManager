package android.microntek.service;

import android.os.Environment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Util {
  public static final String[] Dev_listname;

  static {
        Dev_listname = new String[]{"FLASH","SD1","SD2","USB1","USB2", "USB3", "USB4" };
  }

  public Util() {
  }


  public static String GetDevPath(int paramInt)  {

    Environment environment = new Environment();
    String mediaDevice ="";
    Method method = null;

    switch (paramInt) {
      default: {
        return null;
      }
      case 0: {
        mediaDevice = "getExternalStoragePath";
        break;
      }
      case 1: {
        mediaDevice = "getExternalSDPath";
        break;
      }
      case 2: {
        mediaDevice = "getExternalSD2Path";
        break;
      }
      case 3: {
        mediaDevice = "getExternalUSBPath";
        break;
      }
      case 4: {
        mediaDevice = "getExternalUSB2Path";
        break;
      }
      case 5: {
        mediaDevice = "getExternalUSB3Path";
        break;
      }
      case 6: {
        mediaDevice = "getExternalUSB4Path";
      }
    }
    Class<? extends Environment> envClass = environment.getClass();
    Class[] tempClass = new java.lang.Class[0];
    try {
      method = envClass.getDeclaredMethod(mediaDevice,tempClass );
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    try {
      return method.invoke(environment,new Object[0]).toString();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }


  public static String getMusicDevice(final String path) {

    for (int i = Util.Dev_listname.length - 1; i >= 0; --i) {
      if (path.equals(GetDevPath(i))) {
        return Util.Dev_listname[i];

      }
    }
    return null;
  }
}