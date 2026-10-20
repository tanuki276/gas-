package com.example.historymaster.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import com.example.historymaster.data.*;
import com.example.historymaster.design.*;
import com.example.historymaster.models.*;
import com.google.android.material.button.MaterialButton;
import java.util.*;

public abstract class Screen extends AppCompatActivity {
  protected LinearLayout body, shell;
  protected FrameLayout stage;
  protected ScrollView scroll;
  protected String region;
  protected HistoryData data;
  protected final int ink = Palette.INK, teal = Palette.PRIMARY;

  protected int dp(int value) {
    return Math.round(value * getResources().getDisplayMetrics().density);
  }

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    region = Regions.safe(getIntent().getStringExtra("region"));
    data = JsonLoader.loadHistoryData(this, region + ".json");
    getWindow().setStatusBarColor(Palette.BACKGROUND);
    getWindow().setNavigationBarColor(Palette.WHITE);
    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    if (android.os.Build.VERSION.SDK_INT >= 26)
      getWindow()
          .getDecorView()
          .setSystemUiVisibility(
              View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
  }

  protected void page(String title, String subtitle) {
    if (stage != null) Motion.cancelTree(stage);
    stage = new FrameLayout(this);
    shell = new LinearLayout(this);
    shell.setOrientation(LinearLayout.VERTICAL);
    shell.setBackgroundColor(Palette.BACKGROUND);
    stage.addView(shell, new FrameLayout.LayoutParams(-1, -1));
    scroll = new ScrollView(this);
    scroll.setFillViewport(true);
    scroll.setClipToPadding(false);
    body = new LinearLayout(this);
    body.setOrientation(LinearLayout.VERTICAL);
    body.setPadding(dp(20), dp(12), dp(20), dp(28));
    body.setSaveEnabled(false);
    scroll.addView(body, new ScrollView.LayoutParams(-1, -2));
    shell.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
    setContentView(stage);
    if (!(this instanceof com.example.historymaster.MainActivity)) {
      Button back = button("‹  戻る", this::finish, false);
      back.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
    }
    if (title != null && !title.isEmpty()) text(title, 28, true);
    if (subtitle != null && !subtitle.isEmpty()) text(subtitle, 14, false);
  }

  protected GradientDrawable surface(int color, int stroke, int radius) {
    GradientDrawable shape = new GradientDrawable();
    shape.setColor(color);
    shape.setCornerRadius(dp(radius));
    if (stroke != 0) shape.setStroke(dp(1), stroke);
    return shape;
  }

  protected LinearLayout card(int color) {
    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(dp(18), dp(14), dp(18), dp(16));
    card.setBackground(surface(color, color == Palette.WHITE ? Palette.BORDER : 0, 24));
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
    lp.setMargins(0, dp(9), 0, dp(9));
    body.addView(card, lp);
    return card;
  }

  protected TextView label(LinearLayout parent, String value, int size, int color, boolean bold) {
    TextView text = new TextView(this);
    text.setText(value);
    text.setTextSize(size);
    text.setTextColor(color);
    text.setFontFeatureSettings("kern");
    text.setLineSpacing(dp(4), 1);
    text.setPadding(0, dp(6), 0, dp(6));
    if (bold) text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    parent.addView(text, new LinearLayout.LayoutParams(-1, -2));
    return text;
  }

  protected TextView text(String value, int size, boolean bold) {
    TextView text = label(body, value, size, bold ? Palette.INK : Palette.MUTED, bold);
    if (bold) ViewCompat.setAccessibilityHeading(text, true);
    return text;
  }

  protected MaterialButton action(
      LinearLayout parent, String title, Runnable click, boolean primary) {
    MaterialButton button = new MaterialButton(this);
    button.setText(title);
    button.setAllCaps(false);
    button.setTextSize(16);
    button.setMinHeight(dp(58));
    button.setPadding(dp(18), dp(12), dp(18), dp(12));
    button.setCornerRadius(dp(18));
    button.setInsetTop(0);
    button.setInsetBottom(dp(3));
    button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    button.setBackgroundTintList(
        new ColorStateList(
            new int[][] {{-android.R.attr.state_enabled}, {}},
            new int[] {Palette.BORDER, primary ? Palette.PRIMARY : Palette.WHITE}));
    button.setTextColor(
        new ColorStateList(
            new int[][] {{-android.R.attr.state_enabled}, {}},
            new int[] {Palette.MUTED, primary ? Palette.WHITE : Palette.INK}));
    button.setStrokeWidth(primary ? 0 : dp(1));
    button.setStrokeColor(ColorStateList.valueOf(Palette.BORDER));
    button.setElevation(primary ? dp(3) : 0);
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
    lp.setMargins(0, dp(5), 0, dp(5));
    parent.addView(button, lp);
    button.setOnClickListener(
        v -> {
          Motion.tap(v);
          Motion.pop(v);
          click.run();
        });
    return button;
  }

  protected Button button(String title, Runnable click, boolean primary) {
    return action(body, title, click, primary);
  }

  protected void meter(LinearLayout parent, int value, int max, String description) {
    ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
    bar.setMax(Math.max(1, max));
    bar.setProgress(Math.max(0, Math.min(value, max)));
    bar.setProgressTintList(ColorStateList.valueOf(Palette.PRIMARY));
    bar.setProgressBackgroundTintList(ColorStateList.valueOf(Palette.BORDER));
    bar.setContentDescription(description);
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(12));
    lp.setMargins(0, dp(8), 0, dp(8));
    parent.addView(bar, lp);
  }

  protected MascotView mascot(LinearLayout parent, int size, boolean happy) {
    MascotView mascot = new MascotView(this);
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(size), dp(size));
    lp.gravity = Gravity.CENTER_HORIZONTAL;
    parent.addView(mascot, lp);
    mascot.post(() -> mascot.greet(happy));
    return mascot;
  }

  protected void celebrate() {
    CelebrationView celebration = new CelebrationView(this);
    stage.addView(celebration, new FrameLayout.LayoutParams(-1, -1));
    celebration.setClickable(false);
    celebration.burst();
  }

  protected void open(Class<?> cls) {
    Intent intent = new Intent(this, cls);
    intent.putExtra("region", region);
    startActivity(intent);
  }

  protected void article(String title) {
    article(region, title);
  }

  protected void article(String regionId, String title) {
    Intent intent = new Intent(this, ArticleActivity.class);
    intent.putExtra("region", regionId);
    intent.putExtra("title", title);
    startActivity(intent);
  }

  protected void browse(String url) {
    try {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    } catch (ActivityNotFoundException e) {
      Toast.makeText(this, "リンクを開くブラウザがありません", Toast.LENGTH_LONG).show();
    }
  }

  protected List<Entity> entities() {
    List<Entity> result = new ArrayList<>();
    if (data != null && data.getPeriods() != null)
      for (Period period : data.getPeriods()) collect(period.getMiddleLevelEntities(), result);
    return result;
  }

  private void collect(List<Entity> list, List<Entity> result) {
    if (list == null) return;
    for (Entity e : list) {
      result.add(e);
      collect(e.getSubEntities(), result);
    }
  }

  protected boolean ready() {
    if (data != null && data.getPeriods() != null) return true;
    text("教材を読み込めませんでした。ホームからもう一度お試しください。", 16, false);
    return false;
  }

  @Override
  protected void onStop() {
    if (stage != null) Motion.cancelTree(stage);
    super.onStop();
  }
}
