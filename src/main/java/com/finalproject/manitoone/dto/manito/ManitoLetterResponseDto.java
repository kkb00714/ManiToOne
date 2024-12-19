package com.finalproject.manitoone.dto.manito;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManitoLetterResponseDto {

  private Long manitoLetterId;
  private String letterContent;
  private String musicUrl;
  private String musicComment;
  private boolean isPublic;
  private boolean isReport;
  private boolean isAnswerReport;
  private String answerLetter;
  private String timeDiff;
  private boolean isOwner;
  private LocalDateTime createdAt;
  private String formattedCreatedAt;
  private Long postId;

  public static class ManitoLetterResponseDtoBuilder {
    private static String desanitizeText(String text) {
      if (text == null || text.trim().isEmpty()) {
        return "";
      }
      return text.replace("&lt;", "<")
          .replace("&gt;", ">")
          .replace("&amp;", "&")
          .replace("&quot;", "\"")
          .replace("&#x27;", "'");
    }

    public ManitoLetterResponseDtoBuilder letterContent(String letterContent) {
      this.letterContent = desanitizeText(letterContent);
      return this;
    }

    public ManitoLetterResponseDtoBuilder musicComment(String musicComment) {
      this.musicComment = desanitizeText(musicComment);
      return this;
    }

    public ManitoLetterResponseDtoBuilder answerLetter(String answerLetter) {
      this.answerLetter = desanitizeText(answerLetter);
      return this;
    }
  }
}
