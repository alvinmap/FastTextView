package com.lsjwzh.widget.text;

import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.EllipsisSpannedContainer;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.StaticLayoutBuilderCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;

/**
 * Simple and Fast TextView.
 */
public class FastTextView extends FastTextLayoutView {
  private static final String TAG = FastTextView.class.getSimpleName();
  private CharSequence mText;
  private TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
  private ReplacementSpan mCustomEllipsisSpan;
  TextViewAttrsHelper mAttrsHelper = new TextViewAttrsHelper();

  public FastTextView(Context context) {
    this(context, null);
  }

  public FastTextView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, -1);
  }

  public FastTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs, defStyleAttr, -1);
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public FastTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context, attrs, defStyleAttr, defStyleRes);
  }

  private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    mAttrsHelper.init(context, attrs, defStyleAttr, defStyleRes);
    TextPaint textPaint = getTextPaint();
    textPaint.setColor(mAttrsHelper.mTextColor);
    textPaint.setTextSize(mAttrsHelper.mTextSize);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    Layout textLayout = getTextLayout();
    if (textLayout != null) {
      CharSequence textSource = textLayout.getText();
      if (textSource instanceof Spannable) {
        if (ClickableSpanUtil.handleClickableSpan(this, textLayout, (Spannable) textSource, event)
            || (mCustomEllipsisSpan != null
            && mCustomEllipsisSpan instanceof ClickableSpanUtil.Clickable &&
            ClickableSpanUtil.handleClickableSpan(this, textLayout, (Spannable) textSource,
                ((ClickableSpanUtil.Clickable) mCustomEllipsisSpan).getClass(), event))) {
          return true;
        }
      }
    }
    return super.onTouchEvent(event);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    long start = System.currentTimeMillis();
    int width = MeasureSpec.getSize(widthMeasureSpec);
    if (mAttrsHelper.mMaxWidth != Integer.MAX_VALUE && width > mAttrsHelper.mMaxWidth) {
      widthMeasureSpec = MeasureSpec.makeMeasureSpec(mAttrsHelper.mMaxWidth, MeasureSpec.EXACTLY);
    }
    if (!TextUtils.isEmpty(mText) &&
        ((mLayout == null && width > 0) || (mLayout != null && width != mLayout.getWidth()))) {
      mLayout = makeLayout(mText, width);
    }
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    long end = System.currentTimeMillis();
    if (BuildConfig.DEBUG) {
      Log.d(TAG, "onMeasure cost:" + (end - start));
    }
  }

  public TextPaint getPaint() {
    return mTextPaint;
  }

  @Deprecated
  public TextPaint getTextPaint() {
    return mTextPaint;
  }

  public void setText(@android.annotation.NonNull CharSequence text) {
    if (mText != text) {
      setTextLayout(null);
    }
    mText = text;
  }

  public CharSequence getText() {
    return mText;
  }

  /**
   * Sets the horizontal alignment of the text and the
   * vertical gravity that will be used when there is extra space
   * in the TextView beyond what is required for the text itself.
   *
   * @attr ref android.R.styleable#TextView_gravity
   * @see android.view.Gravity
   */
  public void setGravity(int gravity) {
    if (mAttrsHelper.setGravity(gravity)) {
      setTextLayout(null);
    }
  }

  /**
   * Returns the horizontal and vertical alignment of this TextView.
   *
   * @attr ref android.R.styleable#TextView_gravity
   * @see android.view.Gravity
   */
  public int getGravity() {
    return mAttrsHelper.getGravity();
  }

  public void setMaxWidth(int width) {
    if (mAttrsHelper.mMaxWidth != width) {
      mAttrsHelper.mMaxWidth = width;
      setTextLayout(null);
    }
  }

  public int getMaxWidth() {
    return mAttrsHelper.mMaxWidth;
  }

  public void setMaxLines(int maxLines) {
    if (mAttrsHelper.mMaxLines != maxLines) {
      mAttrsHelper.mMaxLines = maxLines;
      setTextLayout(null);
    }
  }

  public int getMaxLines() {
    return mAttrsHelper.mMaxLines;
  }

  public void setTextSize(float textSize) {
    setTextSize(textSize, TypedValue.COMPLEX_UNIT_SP);
  }

  /**
   * Set the default text size to a given unit and value.  See {@link
   * TypedValue} for the possible dimension units.
   *
   * @param textSize The desired size in the given units.
   * @param unit     The desired dimension unit.
   */
  public void setTextSize(float textSize, int unit) {
    float rawTextSize = TypedValue.applyDimension(
        unit, textSize, getResources().getDisplayMetrics());
    mTextPaint.setTextSize(rawTextSize);
  }

  public float getTextSize() {
    return mTextPaint.getTextSize();
  }

  public int getEllipsize() {
    return mAttrsHelper.mEllipsize;
  }

  public void setEllipsize(int ellipsize) {
    if (mAttrsHelper.mEllipsize != ellipsize) {
      mAttrsHelper.mEllipsize = ellipsize;
      setTextLayout(null);
    }
  }

  public void setCustomEllipsisSpan(ReplacementSpan customEllipsisSpan) {
    mCustomEllipsisSpan = customEllipsisSpan;
  }

  public ReplacementSpan getCustomEllipsisSpan() {
    return mCustomEllipsisSpan;
  }

  @NonNull
  private StaticLayout makeLayout(CharSequence text, int maxWidth) {
    int width;
    if (text instanceof Spanned) {
      width = (int) Math.ceil(Layout.getDesiredWidth(text, mTextPaint));
    } else {
      width = (int) Math.ceil(mTextPaint.measureText(text, 0, text.length()));
    }

    StaticLayoutBuilderCompat layoutBuilder = StaticLayoutBuilderCompat.obtain(text, 0, text.length(), mTextPaint, maxWidth > 0 ? Math.min(maxWidth, width) : width);
    layoutBuilder.setLineSpacing(mAttrsHelper.mSpacingAdd, mAttrsHelper.mSpacingMultiplier)
        .setMaxLines(mAttrsHelper.mMaxLines)
        .setAlignment(TextViewAttrsHelper.getLayoutAlignment(this, getGravity()))
        .setIncludePad(true);
    TextUtils.TruncateAt truncateAt = getTruncateAt();
    layoutBuilder.setEllipsize(truncateAt);
    if (truncateAt != null && text instanceof Spanned) {
      EllipsisSpannedContainer ellipsisSpanned = new EllipsisSpannedContainer((Spanned) text);
      ellipsisSpanned.setCustomEllipsisSpan(mCustomEllipsisSpan);
      layoutBuilder.setText(ellipsisSpanned);
      StaticLayout staticLayout = layoutBuilder.build();
      int lineCount = staticLayout.getLineCount();
      if (lineCount > 0) {
        if (truncateAt == TextUtils.TruncateAt.END) {
          int ellipsisCount = staticLayout.getEllipsisCount(lineCount - 1);
          ellipsisSpanned.setEllipsisRange(ellipsisCount, ellipsisCount + 1);
        } else {
          int ellipsisStart = staticLayout.getEllipsisStart(lineCount - 1);
          ellipsisSpanned.setEllipsisRange(ellipsisStart, ellipsisStart + 1);
        }
      }
      return staticLayout;
    }
    return layoutBuilder.build();
  }

  private TextUtils.TruncateAt getTruncateAt() {
    switch (mAttrsHelper.mEllipsize) {
      // do not support marque
      case 1:
        return TextUtils.TruncateAt.START;
      case 2:
        return TextUtils.TruncateAt.MIDDLE;
      case 3:
        return TextUtils.TruncateAt.END;
      default:
        return null;
    }
  }
}
