package com.microntek.app;

import android.app.*;
import android.content.*;
import android.microntek.service.R;
import android.os.*;
import android.media.*;
import android.microntek.common.*;
import android.view.*;

public class VolumeDialog extends Dialog implements VolumeController.VolStateChangeCallback
{
  private Context mContext;
  private final Runnable mDismissDialogRunnable;
  private VolumeInterface mFun;
  protected Handler mHandler;
  private VolumeController mVolumeController;
  private final int mVolumeDialogLongTimeout;
  private final int mVolumeDialogShortTimeout;

  public VolumeDialog(final Context mContext) {
    super(mContext);
    this.mHandler = new Handler();
    this.mFun = null;
    this.mDismissDialogRunnable = new Runnable() {
      @Override
      public void run() {
        if (isShowing()) {
          dismiss();
        }
      }
    };
    this.mContext = mContext;
    this.mVolumeDialogLongTimeout = 5000;
    this.mVolumeDialogShortTimeout = 2000;
    try {
      this.mFun = (VolumeInterface)mContext;
    }
    catch (Exception ex) {}
  }

  private void dismissVolumeDialog(final int n) {
    this.removeAllVolumeDialogCallbacks();
    this.mHandler.postDelayed(this.mDismissDialogRunnable, (long)n);
  }

  private void removeAllVolumeDialogCallbacks() {
    this.mHandler.removeCallbacks(this.mDismissDialogRunnable);
  }

  public void SetVolumeDialog(final int volume) {
    if (this.mVolumeController != null) {
      this.mVolumeController.setVolume(volume);
      this.dismissVolumeDialog(this.mVolumeDialogShortTimeout);
    }
  }

  public void onCreate(final Bundle bundle) {
    final int canceledOnTouchOutside = 1;
    super.onCreate(bundle);
    final Window window = this.getWindow();
    window.setType(WindowManager.LayoutParams.TYPE_VOLUME_OVERLAY);
    final WindowManager.LayoutParams attributes = window.getAttributes();
    attributes.privateFlags |= 0x10;
    window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
     window.requestFeature(Window.FEATURE_NO_TITLE);
    this.setContentView(R.layout.volume_dialog);
    this.setCanceledOnTouchOutside(false);
    int getVolume = 0;
    if (this.mFun != null) {
      getVolume = this.mFun.GetVolume();
    }
    final String parameters = ((AudioManager)this.mContext.getSystemService(Context.AUDIO_SERVICE)).getParameters("cfg_maxvolume=");

      int maxVolume = 30;
      try {
          maxVolume = Integer.parseInt(parameters);
      } catch (NumberFormatException e) {

      }
      this.mVolumeController = new VolumeController(this.getContext(), (ToggleSlider)this.findViewById(R.id.volume_slider), getVolume, maxVolume);

  }

  protected void onStart() {
    super.onStart();
    this.dismissVolumeDialog(this.mVolumeDialogLongTimeout);
    this.mVolumeController.addStateChangedCallback((VolumeController.VolStateChangeCallback)this);
  }

  protected void onStop() {
    super.onStop();
    this.mVolumeController.unregisterCallbacks();
    this.removeAllVolumeDialogCallbacks();
  }

  public void onVolLevelChanged(final int n) {
    this.dismissVolumeDialog(this.mVolumeDialogShortTimeout);
    if (this.mFun != null) {
      this.mFun.OnChangeVolume(n);
    }
  }
}
