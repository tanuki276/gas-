package com.example.historymaster.ui;

import android.os.Bundle;
import android.text.*;
import android.widget.*;
import com.example.historymaster.data.*;
import com.example.historymaster.design.Palette;
import com.example.historymaster.models.*;
import com.example.historymaster.utils.SharedPrefsHelper;
import java.text.Normalizer;
import java.util.*;

public class SearchActivity extends Screen {
  private final List<Entry> index = new ArrayList<>();
  private LinearLayout results;
  private EditText input;
  private Spinner filter;
  private CheckBox favorites;
  private int limit = 40;

  private static final class Entry {
    String region, period, search;
    Entity entity;
  }

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    page("発見を、探そう。", "7地域を横断して国・王朝・関連語を検索できます。");
    for (String id : Regions.IDS) {
      HistoryData history = JsonLoader.loadHistoryData(this, id + ".json");
      if (history != null && history.getPeriods() != null)
        for (Period period : history.getPeriods())
          collect(id, period.getUpperLevel(), period.getMiddleLevelEntities());
    }
    input = new EditText(this);
    input.setHint("国・王朝・キーワードを入力");
    input.setTextColor(Palette.INK);
    input.setHintTextColor(Palette.MUTED);
    input.setSingleLine(true);
    input.setMinHeight(dp(56));
    input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
    body.addView(input);
    filter = new Spinner(this);
    String[] labels = new String[8];
    labels[0] = "すべての地域";
    System.arraycopy(Regions.NAMES, 0, labels, 1, 7);
    filter.setAdapter(
        new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels));
    filter.setMinimumHeight(dp(52));
    filter.setContentDescription("検索する地域");
    body.addView(filter);
    favorites = new CheckBox(this);
    favorites.setText("お気に入りだけ");
    favorites.setTextColor(Palette.INK);
    favorites.setMinHeight(dp(48));
    body.addView(favorites);
    results = new LinearLayout(this);
    results.setOrientation(LinearLayout.VERTICAL);
    body.addView(results);
    if (state != null) {
      input.setText(state.getString("query", ""));
      filter.setSelection(state.getInt("filter", 0));
      favorites.setChecked(state.getBoolean("favorites"));
    }
    input.addTextChangedListener(
        new TextWatcher() {
          public void beforeTextChanged(CharSequence s, int a, int c, int f) {}

          public void afterTextChanged(Editable e) {}

          public void onTextChanged(CharSequence s, int start, int before, int count) {
            limit = 40;
            show();
          }
        });
    filter.setOnItemSelectedListener(
        new android.widget.AdapterView.OnItemSelectedListener() {
          public void onNothingSelected(android.widget.AdapterView<?> parent) {}

          public void onItemSelected(
              android.widget.AdapterView<?> p, android.view.View v, int i, long id) {
            limit = 40;
            show();
          }
        });
    favorites.setOnCheckedChangeListener(
        (button, checked) -> {
          limit = 40;
          show();
        });
    show();
  }

  private static String normalize(String text) {
    return Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT).trim();
  }

  private void collect(String region, String period, List<Entity> entities) {
    if (entities == null) return;
    for (Entity entity : entities) {
      Entry entry = new Entry();
      entry.region = region;
      entry.period = period;
      entry.entity = entity;
      entry.search =
          normalize(
              entity.getName()
                  + " "
                  + period
                  + " "
                  + Regions.name(region)
                  + " "
                  + (entity.getTags() == null ? "" : TextUtils.join(" ", entity.getTags())));
      index.add(entry);
      collect(region, period, entity.getSubEntities());
    }
  }

  private void show() {
    results.removeAllViews();
    LinearLayout root = body;
    body = results;
    String query = normalize(input.getText().toString());
    int selected = filter.getSelectedItemPosition();
    List<Entry> matches = new ArrayList<>();
    for (Entry entry : index) {
      if (selected > 0 && !entry.region.equals(Regions.IDS[selected - 1])) continue;
      if (favorites.isChecked()
          && !SharedPrefsHelper.favorite(this, entry.region, entry.entity.getName())) continue;
      boolean match = true;
      for (String word : query.split("\\s+")) if (!entry.search.contains(word)) match = false;
      if (match) matches.add(entry);
    }
    text(matches.size() + "件の発見", 15, true);
    if (matches.isEmpty()) text("見つかりませんでした。短い言葉や、別の地域で試してください。", 16, false);
    for (int i = 0; i < Math.min(limit, matches.size()); i++) {
      Entry entry = matches.get(i);
      LinearLayout item = card(Palette.WHITE);
      label(item, Regions.name(entry.region) + " / " + entry.period, 12, Palette.MUTED, false);
      label(item, entry.entity.getName(), 20, Palette.INK, true);
      if (entry.entity.getTags() != null)
        label(item, TextUtils.join(" · ", entry.entity.getTags()), 14, Palette.MUTED, false);
      action(item, "解説を読む  ↗", () -> article(entry.region, entry.entity.getName()), false);
    }
    if (limit < matches.size())
      button(
          "さらに40件を表示",
          () -> {
            limit += 40;
            show();
          },
          false);
    body = root;
  }

  @Override
  protected void onSaveInstanceState(Bundle state) {
    super.onSaveInstanceState(state);
    state.putString("query", input.getText().toString());
    state.putInt("filter", filter.getSelectedItemPosition());
    state.putBoolean("favorites", favorites.isChecked());
  }
}
