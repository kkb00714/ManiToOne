package com.finalproject.manitoone.dto.manito;

import com.finalproject.manitoone.domain.ManitoLetter;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.User;
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
public class ManitoLetterRequestDto {

  @NotNull
  @NotBlank
  @Size(min = 1, max = 600)
  private String letterContent;

  @Size(max = 200)
  private String musicUrl;

  @Size(max = 100)
  private String musicComment;


  // 특수문자, 스크립트 입력값 정제
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

  // YouTube URL 정제 + 형식 검증
  private String validateAndSanitizeYoutubeUrl(String url) {
    if (url == null || url.trim().isEmpty()) {
      return "";
    }

    String sanitizedUrl = url.replace("<", "")
        .replace(">", "")
        .replace("\"", "")
        .replace("'", "")
        .replace(";", "")
        .replace("javascript:", "")
        .trim();

    if (!sanitizedUrl.isEmpty()) {
      String youtubePattern = "^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)[a-zA-Z0-9_-]{11}$";
      if (!sanitizedUrl.matches(youtubePattern)) {
        throw new IllegalArgumentException("올바른 YouTube URL 형식이 아닙니다.");
      }
    }

    return sanitizedUrl;
  }

  // 정제된 값으로 엔티티 변환
  // parser를 통해서 letter를 조합 처리
  public ManitoLetter toEntity(Post post, User user) {
    String safeContent = sanitizeText(letterContent);
    String safeUrl = validateAndSanitizeYoutubeUrl(musicUrl);
    String safeMusicComment = sanitizeText(musicComment);

    return ManitoLetter.builder()
        .post(post)
        .user(user)
        .letterContent(safeContent)
        .musicUrl(safeUrl)
        .musicComment(safeMusicComment)
        .build();
  }
}
