package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.AiPostLog;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.dto.post.AiPostLogResponseDto;
import com.finalproject.manitoone.repository.AiPostLogRepository;
import com.finalproject.manitoone.repository.UserRepository;
import com.finalproject.manitoone.util.AlanUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiFeedbackService {

  private final AiPostLogRepository aiPostLogRepository;
  private final UserRepository userRepository;

  public AiPostLogResponseDto getAiFeedback(HttpSession session) {
    if (session == null || session.getAttribute("email") == null) {
      throw new IllegalArgumentException(IllegalActionMessages.SESSION_REQUIRED.getMessage());
    }
    String email = (String) session.getAttribute("email");
    User user = userRepository.findByEmail(email).orElseThrow(
        () -> new IllegalArgumentException(IllegalActionMessages.USER_NOT_FOUND.getMessage()));

    // 가장 최근 AIPostLog 가져오기
    AiPostLog latestLog = aiPostLogRepository.findTopByPost_UserAndAiContentIsNotNullOrderByPost_CreatedAtDesc(user).orElse(null);

    if (latestLog != null) {
      return AiPostLogResponseDto.builder()
          .content(AlanUtil.extractFeedback(latestLog.getAiContent()))
          .musicContent(AlanUtil.extractYoutubeRecommendations(latestLog.getAiContent()))
          .build();
    }
    return AiPostLogResponseDto.builder().build();
  }
}
