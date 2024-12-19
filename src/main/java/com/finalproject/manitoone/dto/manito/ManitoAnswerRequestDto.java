package com.finalproject.manitoone.dto.manito;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ManitoAnswerRequestDto {

  @NotNull
  @NotBlank
  @Size(min = 1, max = 500)
  private String answerComment;

  private String sanitizeText(String text) {
    if (text == null || text.trim().isEmpty()) {
      return "";
    }
    return text.replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("'", "&#x27;");
  }

  public String getSanitizedAnswerComment() {
    return sanitizeText(answerComment);
  }
}
