package android.microntek.common;

import java.util.*;
import android.content.*;
import android.os.*;

public class VolumeController implements ToggleSlider.Listener
{

  public static interface VolStateChangeCallback
  {

    public abstract void onVolLevelChanged(int i);
  }
  private ArrayList mChangeCallbacks;
  private final Context mContext;
  private final ToggleSlider mControl;
  private Handler mHandler;
  private int mVolLevel;

  public VolumeController(final Context mContext, final ToggleSlider mControl, final int n, final int max) {
    this.mHandler = new Handler() {
      public void handleMessage(final Message message) {
        super.handleMessage(message);
        if (message.what == 0 && mControl != null) {
          mControl.setValue(message.arg1);
        }
      }
    };
    this.mChangeCallbacks = new ArrayList();
    this.mContext = mContext;
    this.mControl = mControl;
    this.mVolLevel = n;
    this.mControl.setMax(max);
    this.mControl.setValue(n);
    mControl.setOnChangedListener(this);
    this.mContext.registerReceiver(new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if ("com.microntek.VOLUME_CHANGED".equals(intent.getAction()) && intent.hasExtra("volume")) {
          final int intExtra = intent.getIntExtra("volume", 0);
          mControl.setValue(intExtra);
        }
      }
    }, new IntentFilter("com.microntek.VOLUME_CHANGED"));}


  public void addStateChangedCallback(final VolStateChangeCallback VolStateChangeCallback) {
    this.mChangeCallbacks.add(VolStateChangeCallback);
  }

  public void onChanged(final int n) {
    this.mVolLevel = n;
    if (this.mControl != null) {
      this.mControl.setValue(n);
    }
    final Iterator<VolStateChangeCallback> iterator = this.mChangeCallbacks.iterator();
    while (iterator.hasNext()) {
      iterator.next().onVolLevelChanged(n);
    }
  }

  public void onInit() {
  }

  public void onVolClick() {
    final Intent intent = new Intent("com.microntek.irkeyDown");
    intent.putExtra("keyCode", 258);
    this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
  }

  public void setVolume(final int n) {
    this.mVolLevel = n;
    this.mControl.setValue(n);
  }

  public void unregisterCallbacks() {
    this.mChangeCallbacks.clear();
  }
}
