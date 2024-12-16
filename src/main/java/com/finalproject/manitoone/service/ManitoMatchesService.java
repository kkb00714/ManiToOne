package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.ManitoErrorMessages;
import com.finalproject.manitoone.constants.MatchStatus;
import com.finalproject.manitoone.domain.ManitoMatches;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.repository.ManitoMatchesRepository;
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

  // 유저에게 게시글 배정
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

    // 배정 가능한 게시글 찾기 (72시간 이내)
    LocalDateTime postTimeLimit = LocalDateTime.now().minusHours(72);
    List<Post> assignablePosts = manitoMatchesRepository.findAssignablePosts(postTimeLimit, user.getUserId());

    if (assignablePosts.isEmpty()) {
      throw new IllegalStateException(ManitoErrorMessages.NO_AVAILABLE_POSTS.getMessage());
    }

    for (Post post : assignablePosts) {
      try {
        boolean isAlreadyMatched = manitoMatchesRepository
            .existsByMatchedPostIdAndStatus(post, MatchStatus.MATCHED);

        if (!isAlreadyMatched) {
          ManitoMatches match = ManitoMatches.builder()
              .matchedPostId(post)
              .matchedUserId(user)
              .status(MatchStatus.MATCHED)
              .build();

          return manitoMatchesRepository.save(match);
        }
      } catch (Exception e) {
        log.error("Failed to create match for post: {}", post.getPostId(), e);
      }
    }

    throw new IllegalStateException(ManitoErrorMessages.NO_AVAILABLE_POSTS.getMessage());
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
}
