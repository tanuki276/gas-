package com.example.historymaster.design;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** Jipi, an original compass-carrying fox. All artwork is resolution-independent. */
public final class MascotView extends View {
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private ValueAnimator animator;
  private float bounce;
  private boolean happy;

  public MascotView(Context context) {
    super(context);
    setContentDescription("旅の相棒、キツネのジピ");
    setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
  }

  public void greet(boolean celebrate) {
    happy = celebrate;
    if (!Motion.enabled(getContext())) {
      invalidate();
      return;
    }
    if (animator != null) animator.cancel();
    animator = ValueAnimator.ofFloat(0, 1);
    animator.setDuration(650);
    animator.setInterpolator(new DecelerateInterpolator());
    animator.addUpdateListener(
        a -> {
          bounce = (float) a.getAnimatedValue();
          invalidate();
        });
    animator.start();
  }

  private void fill(Canvas c, int color, float l, float t, float r, float b, float radius) {
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(color);
    c.drawRoundRect(l, t, r, b, radius, radius, paint);
  }

  private void triangle(
      Canvas c, int color, float x1, float y1, float x2, float y2, float x3, float y3) {
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(color);
    Path p = new Path();
    p.moveTo(x1, y1);
    p.lineTo(x2, y2);
    p.lineTo(x3, y3);
    p.close();
    c.drawPath(p, paint);
  }

  @Override
  protected void onDraw(Canvas c) {
    super.onDraw(c);
    float scale = Math.min(getWidth() / 160f, getHeight() / 160f);
    c.save();
    c.translate((getWidth() - 160 * scale) / 2, (getHeight() - 160 * scale) / 2);
    c.scale(scale, scale);
    paint.setColor(Palette.BORDER);
    c.drawOval(35, 139, 129, 151, paint);
    c.translate(0, -(float) Math.sin(bounce * Math.PI) * 10);
    fill(c, Palette.ORANGE, 103, 85, 147, 132, 24);
    fill(c, Palette.WHITE, 130, 87, 147, 110, 12);
    fill(c, Palette.ORANGE, 46, 83, 114, 141, 28);
    fill(c, Palette.PRIMARY, 50, 121, 71, 146, 10);
    fill(c, Palette.PRIMARY, 89, 121, 110, 146, 10);
    triangle(c, Palette.ORANGE, 28, 20, 34, 77, 70, 46);
    triangle(c, Palette.ORANGE, 132, 20, 125, 77, 91, 46);
    triangle(c, 0xFFF8C6AB, 38, 34, 43, 65, 61, 47);
    triangle(c, 0xFFF8C6AB, 122, 34, 116, 65, 99, 47);
    fill(c, Palette.ORANGE, 31, 42, 129, 114, 34);
    fill(c, 0xFFFFEADB, 39, 75, 121, 113, 27);
    paint.setColor(Palette.INK);
    if (happy) {
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(4);
      paint.setStrokeCap(Paint.Cap.ROUND);
      c.drawArc(50, 64, 66, 80, 195, 150, false, paint);
      c.drawArc(94, 64, 110, 80, 195, 150, false, paint);
      paint.setStyle(Paint.Style.FILL);
    } else {
      c.drawOval(53, 66, 61, 78, paint);
      c.drawOval(99, 66, 107, 78, paint);
    }
    triangle(c, Palette.INK, 74, 84, 86, 84, 80, 91);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(3);
    c.drawArc(70, 86, 90, 100, 15, 150, false, paint);
    paint.setStyle(Paint.Style.FILL);
    fill(c, Palette.PRIMARY, 47, 106, 114, 118, 5);
    paint.setColor(Palette.GOLD);
    c.drawCircle(80, 122, 14, paint);
    triangle(c, Palette.PRIMARY, 80, 111, 75, 125, 85, 125);
    triangle(c, Palette.WHITE, 80, 133, 75, 125, 85, 125);
    c.restore();
  }

  public void stop() {
    if (animator != null) animator.cancel();
    bounce = 0;
    invalidate();
  }

  @Override
  protected void onDetachedFromWindow() {
    stop();
    super.onDetachedFromWindow();
  }
}
