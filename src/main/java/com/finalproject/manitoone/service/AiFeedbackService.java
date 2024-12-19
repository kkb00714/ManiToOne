package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.AiPostLog;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.dto.post.AiPostLogResponseDto;
import com.finalproject.manitoone.repository.AiPostLogRepository;
import com.finalproject.manitoone.repository.PostRepository;
import com.finalproject.manitoone.repository.UserRepository;
import com.finalproject.manitoone.util.AlanUtil;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiFeedbackService {

  private final AiPostLogRepository aiPostLogRepository;
  private final UserRepository userRepository;
  private final PostRepository postRepository;

  public AiPostLogResponseDto getAiFeedback(HttpSession session) {
    if (session == null || session.getAttribute("email") == null) {
      throw new IllegalArgumentException(IllegalActionMessages.SESSION_REQUIRED.getMessage());
    }
    String email = (String) session.getAttribute("email");
    User user = userRepository.findByEmail(email).orElseThrow(
        () -> new IllegalArgumentException(IllegalActionMessages.USER_NOT_FOUND.getMessage()));

    // 가장 최근 AIPostLog 가져오기
    AiPostLog latestLog = aiPostLogRepository.findTopByPost_UserAndAiContentIsNotNullOrderByPost_CreatedAtDesc(
        user).orElse(null);

    if (latestLog != null) {
      return AiPostLogResponseDto.builder()
          .content(AlanUtil.getFeedbackContent(latestLog.getAiContent()))
          .musicTitle(AlanUtil.getMusicTitle(latestLog.getAiContent()))
          .musicLink(AlanUtil.getMusicLink(latestLog.getAiContent()))
          .build();
    }
    return AiPostLogResponseDto.builder().build();
  }

  @Async
  public void processFeedbackAsync(Long postId, String content) {
    Post post = postRepository.findByPostId(postId).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()));

    User user = post.getUser(); // Post에서 User 가져오기

    // 오늘 날짜 범위 계산
    LocalDateTime startOfToday = LocalDate.now().atStartOfDay(); // 오늘 00:00:00
    LocalDateTime now = LocalDateTime.now(); // 현재 시간

    // 오늘 작성된 유저의 AI 포스트 로그가 있는 Post 개수 확인
    long todayAiPostsCount = postRepository.countByUserAndCreatedAtBetweenAndAiPostLogsExists(user,
        startOfToday, now);

    if (todayAiPostsCount < 3) {
      try {
        // AI 피드백 요청
        String feedback = AlanUtil.getAlanAnswer(content);

        // 피드백 저장
        aiPostLogRepository.save(AiPostLog.builder().post(post).aiContent(feedback).build());

      } catch (Exception e) {
        log.info("AI 포스터 저장 에러");
        log.info("포스트 아이디 : " + post.getPostId());
        log.info("유저 아이디 : " + user.getUserId());
      }
    }
  }

  // 오늘 하루 피드백 3개 받았는지 검사 (3개 이상이라면 true, 아니라면 false)
  public Boolean isDailyAiFeedbackLimitExceeded(HttpSession session) {
    if (session == null || session.getAttribute("email") == null) {
      throw new IllegalArgumentException(IllegalActionMessages.SESSION_REQUIRED.getMessage());
    }
    String email = (String) session.getAttribute("email");

    User user = userRepository.findByEmail(email).orElseThrow(
        () -> new IllegalArgumentException(IllegalActionMessages.USER_NOT_FOUND.getMessage()));

    // 오늘 하루 피드백이 3개 이상인지 가져와서 3개 이상이라면 true, 아니라면 false
    // 오늘의 날짜 범위 계산
    LocalDateTime startOfToday = LocalDate.now().atStartOfDay(); // 00:00:00
    LocalDateTime endOfToday = LocalDate.now().atTime(23, 59, 59); // 23:59:59

    // 오늘의 AI 피드백 개수 확인
    long feedbackCount = aiPostLogRepository.countTodayAiFeedbacksByUser(user, startOfToday, endOfToday);

    return feedbackCount >= 3;
  }
}
