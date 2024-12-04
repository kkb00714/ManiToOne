package com.finalproject.manitoone.util;

import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class DataUtil {

  public String getTimeDifference(LocalDateTime createdAt) {
    LocalDateTime now = LocalDateTime.now();
    Duration duration = Duration.between(createdAt, now);

    if (duration.toMinutes() < 1) {
      return "방금";
    } else if (duration.toHours() < 1) {
      return duration.toMinutes() + "분";
    } else if (duration.toDays() < 1) {
      return duration.toHours() + "시간";
    } else {
      return duration.toDays() + "일";
    }
  }
}
