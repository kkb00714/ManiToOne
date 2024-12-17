package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.ManitoErrorMessages;
import com.finalproject.manitoone.constants.MatchStatus;
import com.finalproject.manitoone.domain.ManitoMatches;
import com.finalproject.manitoone.domain.MatchProcessStatus;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.repository.ManitoMatchesRepository;
import com.finalproject.manitoone.repository.MatchProcessStatusRepository;
import com.finalproject.manitoone.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ManitoMatchesService {

  private final ManitoMatchesRepository manitoMatchesRepository;
  private final UserRepository userRepository;
  private final AiValidationService aiValidationService;
  private final MatchProcessStatusRepository matchProcessStatusRepository;

  @Transactional(isolation = Isolation.SERIALIZABLE)
  public ManitoMatches createMatch(String nickname) {
    User user = userRepository.findUserByNickname(nickname)
        .orElseThrow(
            () -> new EntityNotFoundException(ManitoErrorMessages.USER_NOT_FOUND.getMessage()));

    // 24시간 이내 매칭 여부 확인
    LocalDateTime timeLimit = LocalDateTime.now().minusHours(24);
    if (manitoMatchesRepository.hasRecentMatch(user, timeLimit)) {
      throw new IllegalStateException(ManitoErrorMessages.ALREADY_MATCHED_24HOURS.getMessage());
    }

    // 매칭 프로세스 상태 생성
    MatchProcessStatus processStatus = MatchProcessStatus.create(nickname);
    matchProcessStatusRepository.save(processStatus);

    try {
      // 배정 가능한 게시글 찾기 (72시간 이내)
      LocalDateTime postTimeLimit = LocalDateTime.now().minusHours(72);
      List<Post> assignablePosts = manitoMatchesRepository.findAssignablePosts(postTimeLimit,
          user.getUserId());

      if (assignablePosts.isEmpty()) {
        processStatus.fail();
        matchProcessStatusRepository.save(processStatus);
        throw new IllegalStateException(ManitoErrorMessages.NO_AVAILABLE_POSTS.getMessage());
      }

      // 적절한 게시물을 찾을 때까지 반복
      for (Post post : assignablePosts) {
        try {
          boolean isAlreadyMatched = manitoMatchesRepository
              .existsByMatchedPostIdAndStatus(post, MatchStatus.MATCHED);

          if (!isAlreadyMatched) {
            // Post 객체를 직접 전달하여 AI 검증 수행
            boolean isAppropriate = aiValidationService.validatePostContent(post);

            if (isAppropriate) {
              ManitoMatches match = ManitoMatches.builder()
                  .matchedPostId(post)
                  .matchedUserId(user)
                  .status(MatchStatus.MATCHED)
                  .build();

              // 매칭 성공 시 프로세스 상태 업데이트
              processStatus.complete();
              matchProcessStatusRepository.save(processStatus);

              return manitoMatchesRepository.save(match);
            }

            // AI가 부적절하다고 판단한 경우
            post.updateManitoStatus(false);
            log.warn("Post ID {} has been marked as inappropriate by AI", post.getPostId());
          }
        } catch (Exception e) {
          log.error("Failed to process post: {}", post.getPostId(), e);
        }
      }

      // 모든 게시물이 부적절하거나 매칭 실패한 경우
      processStatus.fail();
      matchProcessStatusRepository.save(processStatus);
      throw new IllegalStateException(ManitoErrorMessages.NO_AVAILABLE_POSTS.getMessage());

    } catch (Exception e) {
      processStatus.fail();
      matchProcessStatusRepository.save(processStatus);
      throw e;
    }
  }

  @Transactional(readOnly = true)
  public Optional<ManitoMatches> getCurrentValidMatch(String nickname) {
    User user = userRepository.findUserByNickname(nickname)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.USER_NOT_FOUND.getMessage()));

    LocalDateTime timeLimit = LocalDateTime.now().minusHours(24);
    return manitoMatchesRepository.findLatestMatchByUser(user.getUserId(), timeLimit);
  }

  // Pass 처리
  @Transactional
  public void passMatch(Long matchId, String nickname) {
    ManitoMatches match = manitoMatchesRepository.findById(matchId)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.MANITO_MATCH_NOT_FOUND.getMessage()));

    if (!match.getMatchedUserId().getNickname().equals(nickname)) {
      throw new IllegalStateException(ManitoErrorMessages.NO_PERMISSION_TO_PASS.getMessage());
    }

    match.markAsPassed();
  }

  @Transactional(readOnly = true)
  public boolean hasRecentMatch(String nickname, LocalDateTime timeLimit) {
    User user = userRepository.findUserByNickname(nickname)
        .orElseThrow(
            () -> new EntityNotFoundException(ManitoErrorMessages.USER_NOT_FOUND.getMessage()));
    return manitoMatchesRepository.hasRecentMatch(user, timeLimit);
  }
}
