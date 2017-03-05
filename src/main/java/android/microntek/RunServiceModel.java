package android.microntek;

import android.graphics.drawable.*;
import android.content.*;

public class RunServiceModel
{
  private Drawable appIcon;
  private String appLabel;
  private Intent intent;
  private int pid;
  private String pkgName;
  private String processName;
  private String serviceName;
  private int uid;

  public Intent getIntent() {
    return this.intent;
  }

  public String getPkgName() {
    return this.pkgName;
  }

  public void setAppIcon(final Drawable appIcon) {
    this.appIcon = appIcon;
  }

  public void setAppLabel(final String appLabel) {
    this.appLabel = appLabel;
  }

  public void setIntent(final Intent intent) {
    this.intent = intent;
  }

  public void setPid(final int pid) {
    this.pid = pid;
  }

  public void setPkgName(final String pkgName) {
    this.pkgName = pkgName;
  }

  public void setProcessName(final String processName) {
    this.processName = processName;
  }

  public void setServiceName(final String serviceName) {
    this.serviceName = serviceName;
  }

  public void setUid(final int uid) {
    this.uid = uid;
  }

  public int getUid() {return this.uid;}
}
