package com.example.historymaster.design;

import android.animation.*;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.view.*;
import android.view.animation.*;
import com.example.historymaster.learning.ProgressStore;

public final class Motion {
  public static boolean enabled(Context context) {
    if (ProgressStore.setting(context, "reduce_motion", false)) return false;
    if (Build.VERSION.SDK_INT >= 26) return ValueAnimator.areAnimatorsEnabled();
    return Settings.Global.getFloat(
            context.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        != 0;
  }

  public static void enter(View view) {
    if (!enabled(view.getContext())) return;
    view.setAlpha(0);
    view.setTranslationY(12 * view.getResources().getDisplayMetrics().density);
    view.animate()
        .alpha(1)
        .translationY(0)
        .setDuration(260)
        .setInterpolator(new DecelerateInterpolator())
        .start();
  }

  public static void pop(View view) {
    if (!enabled(view.getContext())) return;
    view.animate().cancel();
    view.setScaleX(.94f);
    view.setScaleY(.94f);
    view.animate()
        .scaleX(1)
        .scaleY(1)
        .setDuration(300)
        .setInterpolator(new OvershootInterpolator(1.5f))
        .start();
  }

  public static void tap(View view) {
    if (ProgressStore.setting(view.getContext(), "haptics", true))
      view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
  }

  public static void cancelTree(View view) {
    view.animate().cancel();
    view.setAlpha(1f);
    view.setTranslationY(0f);
    view.setScaleX(1f);
    view.setScaleY(1f);
    if (view instanceof MascotView) ((MascotView) view).stop();
    if (view instanceof CelebrationView) ((CelebrationView) view).stop();
    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) view;
      for (int i = 0; i < group.getChildCount(); i++) cancelTree(group.getChildAt(i));
    }
  }
}
