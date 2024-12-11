package com.finalproject.manitoone.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TimeFormatter {

  public static String formatTimeDiff(LocalDateTime createdAt) {
    Duration duration = Duration.between(createdAt, LocalDateTime.now());
    long minutes = duration.toMinutes();

    if (minutes < 1) { // 작성 1분 미만
      return "지금";
    } else if (minutes < 60) { // 작성 60분 미만
      return minutes + "분 전";
    } else if (duration.toHours() < 24) { // 작성 24시간 미만
      return duration.toHours() + "시간 전";
    } else {
      Period period = Period.between(createdAt.toLocalDate(), LocalDate.now());
      if (period.getYears() > 0) { // 1년 이상
        return period.getYears() + "년 전";
      } else if (period.getMonths() > 0) { // 1달 이상
        return period.getMonths() + "달 전";
      } else {
        return period.getDays() + "일 전"; // 1달 미만
      }
    }
  }

}
