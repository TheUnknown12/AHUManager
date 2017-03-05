package android.microntek.common;

import android.graphics.drawable.Drawable;
import android.microntek.service.R;
import android.widget.*;
import android.content.*;
import android.util.*;
import android.view.*;


public class ToggleSlider extends LinearLayout implements SeekBar.OnSeekBarChangeListener
{
  public interface Listener
  {
    void onChanged(int paramInt);

    void onInit();

    void onVolClick();
  }

  private TextView mLabel;
  private Listener mListener;
  private SeekBar mSlider;
  private ImageView mToggle;

  public ToggleSlider(final Context context) {
    this(context, null);
  }

  public ToggleSlider(final Context context, final AttributeSet set) {
    this(context, set, 0);
  }

  public ToggleSlider(final Context context, final AttributeSet set, final int n) {
    super(context, set, n);
    View.inflate(context, R.layout.volume_slider, (ViewGroup)this);
    (this.mToggle = (ImageView)this.findViewById(R.id.toggle)).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mListener.onVolClick();
      }
    });
    (this.mSlider = (SeekBar)this.findViewById(R.id.slider)).setOnSeekBarChangeListener(this);
    this.mLabel = (TextView)this.findViewById(R.id.label);
  }

  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (this.mListener != null) {
      this.mListener.onInit();
    }
  }

  public void onProgressChanged(final SeekBar seekBar, final int n, final boolean b) {
    if (this.mListener != null && b) {
      this.mListener.onChanged(n);
    }
  }

  public void onStartTrackingTouch(final SeekBar seekBar) {
    if (this.mListener != null) {
      this.mListener.onChanged(this.mSlider.getProgress());
    }
  }

  public void onStopTrackingTouch(final SeekBar seekBar) {
    if (this.mListener != null) {
      this.mListener.onChanged(this.mSlider.getProgress());
    }
  }

  public void setMax(final int max) {
    this.mSlider.setMax(max);
  }

  public void setOnChangedListener(final Listener mListener) {
    this.mListener = mListener;
  }

  public void setValue(final int progress) {
    this.mSlider.setProgress(progress);
    this.mLabel.setText(("" + progress));
    final ImageView mToggle = this.mToggle;

    if (progress <= 0) {
      mToggle.setImageResource( R.drawable.notification_template_icon_low_bg);
    }
    else {
      mToggle.setImageResource( R.drawable.notification_template_icon_bg);
    }

  }
}



