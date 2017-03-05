package android.microntek.service;

import android.app.*;

public class ServerApplication extends Application
{
  private int state;

  public ServerApplication() {
    this.state = 0;
  }
}
