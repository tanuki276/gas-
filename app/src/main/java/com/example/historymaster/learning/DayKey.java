package com.example.historymaster.learning;

import java.text.SimpleDateFormat;
import java.util.*;

/** Calendar dates, not 24-hour durations: safe across daylight-saving changes. */
public final class DayKey {
  public static String today() {
    return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(new Date());
  }

  public static long distance(String older, String newer) {
    try {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
      format.setLenient(false);
      format.setTimeZone(TimeZone.getTimeZone("UTC"));
      return (format.parse(newer).getTime() - format.parse(older).getTime()) / 86400000L;
    } catch (Exception e) {
      return Long.MAX_VALUE;
    }
  }
}
