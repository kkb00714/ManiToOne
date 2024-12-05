package com.finalproject.manitoone.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ManitoLetterParser {
  private static final String SEPARATOR = "\u0000";

  public static String combineLetter(String content, String musicUrl, String musicComment){
    content = content.replace(SEPARATOR, "");
    musicUrl = (musicUrl != null) ? musicUrl.replace(SEPARATOR, "") : "";
    musicComment = (musicComment != null) ? musicComment.replace(SEPARATOR, "") : "";

    return String.join(SEPARATOR, content, musicUrl, musicComment);
  }

  public static String extractContent(String comment) {
    String[] parts = comment.split(SEPARATOR);
    return parts[0];
  }

  public static String extractMusicUrl(String comment) {
    String[] parts = comment.split(SEPARATOR);
    return parts.length > 1 ? parts[1] : "";
  }

  public static String extractMusicComment(String comment) {
    String[] parts = comment.split(SEPARATOR);
    return parts.length > 2 ? parts[2] : "";
  }

}
