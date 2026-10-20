package com.example.historymaster.ui;

import android.os.Bundle;
import androidx.appcompat.widget.SwitchCompat;
import com.example.historymaster.design.Palette;
import com.example.historymaster.learning.ProgressStore;

public class SettingsActivity extends Screen {
  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    render();
  }

  private void render() {
    page("自分に合うペースで", "Jipains の設定");
    text("1日の目標", 22, true);
    for (int goal : new int[] {20, 30, 50})
      button(
          (ProgressStore.goal(this) == goal ? "✓  " : "") + goal + " XP / 日",
          () -> {
            ProgressStore.goal(this, goal);
            render();
          },
          ProgressStore.goal(this) == goal);
    text("演出とアクセシビリティ", 22, true);
    toggle("動きを減らす", "reduce_motion", false);
    text("端末側のアニメーション無効設定も尊重します。", 13, false);
    toggle("操作の振動", "haptics", true);
    toggle("クイズの効果音", "sound", false);
    text("このアプリについて", 22, true);
    text("Jipains 3.0\n7地域の基礎コースと歴史図鑑。ジピはJipains独自の旅の相棒です。", 16, false);
    button(
        "Wikipedia本文のライセンス",
        () -> browse("https://creativecommons.org/licenses/by-sa/4.0/deed.ja"),
        false);
    text("出典記事に個別の条件がある場合は、その条件もご確認ください。学習記録・お気に入りはこの端末に保存されます。", 13, false);
  }

  private void toggle(String label, String key, boolean initial) {
    SwitchCompat toggle = new SwitchCompat(this);
    toggle.setText(label);
    toggle.setTextSize(17);
    toggle.setTextColor(Palette.INK);
    toggle.setMinHeight(dp(60));
    toggle.setChecked(ProgressStore.setting(this, key, initial));
    toggle.setOnCheckedChangeListener(
        (button, checked) -> ProgressStore.setSetting(this, key, checked));
    body.addView(toggle);
  }
}
