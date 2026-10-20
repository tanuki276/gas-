package com.example.historymaster.design;

import android.animation.*;
import android.content.Context;
import android.graphics.*;
import android.view.View;

/** A single short burst; no timers or infinite animation survives the screen. */
public final class CelebrationView extends View {
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private ValueAnimator animator;
  private float progress = 1;
  private final int[] colors = {Palette.PRIMARY, Palette.GOLD, Palette.ORANGE, Palette.GREEN};

  public CelebrationView(Context c) {
    super(c);
    setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
  }

  public void burst() {
    if (!Motion.enabled(getContext())) return;
    if (animator != null) animator.cancel();
    animator = ValueAnimator.ofFloat(0, 1);
    animator.setDuration(950);
    animator.addUpdateListener(
        a -> {
          progress = (float) a.getAnimatedValue();
          invalidate();
        });
    animator.start();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (progress >= 1) return;
    float density = getResources().getDisplayMetrics().density;
    for (int i = 0; i < 32; i++) {
      float angle = (float) (Math.PI * 2 * i / 32);
      float radius = (50 + (i % 5) * 25) * density * progress;
      float x = getWidth() / 2f + (float) Math.cos(angle) * radius;
      float y =
          getHeight() * .26f
              + (float) Math.sin(angle) * radius
              + 90 * density * progress * progress;
      paint.setColor(colors[i % 4]);
      paint.setAlpha((int) (255 * (1 - progress)));
      canvas.save();
      canvas.rotate(i * 31 + progress * 180, x, y);
      canvas.drawRoundRect(x, y, x + 7 * density, y + 4 * density, 2 * density, 2 * density, paint);
      canvas.restore();
    }
  }

  public void stop() {
    if (animator != null) animator.cancel();
    progress = 1;
    invalidate();
  }

  @Override
  protected void onDetachedFromWindow() {
    stop();
    super.onDetachedFromWindow();
  }
}
