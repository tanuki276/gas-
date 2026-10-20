package com.example.historymaster.ui;

import static org.junit.Assert.*;

import android.app.Activity;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import com.example.historymaster.MainActivity;
import com.example.historymaster.design.Motion;
import com.example.historymaster.learning.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33, qualifiers = "w393dp-h851dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
public class JipainsFlowTest {
  @Before
  public void clean() {
    Context app = RuntimeEnvironment.getApplication();
    app.getSharedPreferences("HistoryMasterPrefs", Context.MODE_PRIVATE).edit().clear().commit();
    ProgressStore.setSetting(app, "reduce_motion", true);
  }

  @Test
  public void homeTabsAndSettingsRenderAndUseRealNavigation() throws Exception {
    try (ActivityController<MainActivity> controller =
        Robolectric.buildActivity(MainActivity.class).setup()) {
      MainActivity activity = controller.get();
      capture(activity, "home");
      click(activity, "記録");
      capture(activity, "passport");
      click(activity, "図鑑");
      capture(activity, "explore");
      click(activity, "復習");
      capture(activity, "review_empty");
      assertNotNull(find(activity.getWindow().getDecorView(), "復習待ちはありません"));
    }
    try (ActivityController<SettingsActivity> controller =
        Robolectric.buildActivity(SettingsActivity.class).setup()) {
      assertFalse(Motion.enabled(controller.get()));
      capture(controller.get(), "settings");
    }
  }

  @Test
  public void completeLessonRestoreResultAndPreventDoubleRewards() throws Exception {
    Context app = RuntimeEnvironment.getApplication();
    Intent intent =
        new Intent(app, QuizActivity.class)
            .putExtra("region", "japan")
            .putExtra("lesson", "japan:nara");
    try (ActivityController<QuizActivity> controller =
        Robolectric.buildActivity(QuizActivity.class, intent).setup()) {
      QuizActivity activity = controller.get();
      capture(activity, "question");
      for (String answer : new String[] {"平城京", "710年", "元明天皇"}) {
        clickEnding(activity, answer);
        click(activity, "答え合わせ");
        assertNotNull(find(activity.getWindow().getDecorView(), "✓  その通り！"));
        if (answer.equals("平城京")) {
          capture(activity, "correct");
          controller.recreate();
          activity = controller.get();
        }
        click(activity, answer.equals("元明天皇") ? "レッスンを完了する" : "次の発見へ  →");
      }
      assertEquals(35, ProgressStore.load(app).xp);
      assertTrue(ProgressStore.load(app).mastered.contains("japan:nara"));
      capture(activity, "completed");
      assertEquals(0, activity.scroll.getScrollY());
      controller.recreate();
      activity = controller.get();
      assertEquals(35, ProgressStore.load(app).xp);
      click(activity, "コースへ戻る");
      controller.pause().stop();
      assertNull(ProgressStore.session(app, "japan:nara"));
    }
  }

  @Test
  public void mistakeIsSavedForReviewAndCorrectedAnswerRemovesIt() {
    Context app = RuntimeEnvironment.getApplication();
    Intent intent =
        new Intent(app, QuizActivity.class)
            .putExtra("region", "japan")
            .putExtra("lesson", "japan:nara");
    try (ActivityController<QuizActivity> controller =
        Robolectric.buildActivity(QuizActivity.class, intent).setup()) {
      clickEnding(controller.get(), "平安京");
      click(controller.get(), "答え合わせ");
      assertEquals(1, ProgressStore.review(app).size());
    }
    Intent review = new Intent(app, QuizActivity.class).putExtra("review", true);
    try (ActivityController<QuizActivity> controller =
        Robolectric.buildActivity(QuizActivity.class, review).setup()) {
      clickEnding(controller.get(), "平城京");
      click(controller.get(), "答え合わせ");
      assertTrue(ProgressStore.review(app).isEmpty());
    }
  }

  @Test
  public void interruptedEntranceLeavesContentVisibleAndReducedMotionSkipsAnimation() {
    Context app = RuntimeEnvironment.getApplication();
    LinearLayout root = new LinearLayout(app);
    View child = new View(app);
    root.addView(child);
    ProgressStore.setSetting(app, "reduce_motion", false);
    Motion.enter(child);
    Motion.cancelTree(root);
    assertEquals(1f, child.getAlpha(), 0f);
    assertEquals(0f, child.getTranslationY(), 0f);
    ProgressStore.setSetting(app, "reduce_motion", true);
    Motion.enter(child);
    assertEquals(1f, child.getAlpha(), 0f);
  }

  private static TextView find(View view, String text) {
    if (view instanceof TextView && ((TextView) view).getText().toString().equals(text))
      return (TextView) view;
    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) view;
      for (int i = 0; i < group.getChildCount(); i++) {
        TextView found = find(group.getChildAt(i), text);
        if (found != null) return found;
      }
    }
    return null;
  }

  private static void click(Activity activity, String title) {
    TextView button = find(activity.getWindow().getDecorView(), title);
    assertNotNull("Missing: " + title, button);
    assertTrue(button.isEnabled());
    button.performClick();
  }

  private static void clickEnding(Activity activity, String title) {
    Button button = ending(activity.getWindow().getDecorView(), title);
    assertNotNull(button);
    button.performClick();
  }

  private static Button ending(View view, String title) {
    if (view instanceof Button && ((Button) view).getText().toString().endsWith(title))
      return (Button) view;
    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) view;
      for (int i = 0; i < group.getChildCount(); i++) {
        Button result = ending(group.getChildAt(i), title);
        if (result != null) return result;
      }
    }
    return null;
  }

  private static void capture(Activity activity, String name) throws Exception {
    org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
    View view = activity.findViewById(android.R.id.content);
    float density = activity.getResources().getDisplayMetrics().density;
    int width = Math.round(393 * density), height = Math.round(851 * density);
    view.measure(
        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
    view.layout(0, 0, width, height);
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    view.draw(new Canvas(bitmap));
    File directory = new File("build/reports/ui");
    directory.mkdirs();
    try (FileOutputStream out = new FileOutputStream(new File(directory, name + ".png"))) {
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
    }
    bitmap.recycle();
  }
}
