package com.finalproject.manitoone.service;

import com.finalproject.manitoone.domain.ManitoMatches;
import com.finalproject.manitoone.domain.MatchProcessStatus;
import com.finalproject.manitoone.repository.MatchProcessStatusRepository;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncMatchService {

  private final ManitoMatchesService manitoMatchesService;
  private final MatchProcessStatusRepository statusRepository;
  private static final Object lock = new Object();

  @Async
  @Transactional
  public CompletableFuture<ManitoMatches> processMatchAsync(String nickname) {
    synchronized (lock) {
      try {
        if (hasActiveProcess(nickname)) {
          log.info("Active match process exists for user: {}", nickname);
          // 현재 진행중인 프로세스가 있다면 그대로 반환
          return CompletableFuture.completedFuture(null);
        }

        MatchProcessStatus processStatus = MatchProcessStatus.create(nickname);
        statusRepository.save(processStatus);

        try {
          ManitoMatches match = manitoMatchesService.createMatch(nickname);
          processStatus.complete();
          statusRepository.save(processStatus);
          return CompletableFuture.completedFuture(match);

        } catch (Exception e) {
          processStatus.fail();
          statusRepository.save(processStatus);
          throw e;
        }
      } catch (Exception e) {
        log.error("Error in processMatchAsync for user {}: {}", nickname, e.getMessage());
        return CompletableFuture.failedFuture(e);
      }
    }
  }

  private boolean hasActiveProcess(String nickname) {
    return statusRepository.existsByNicknameAndStatusAndTimeoutAtAfter(
        nickname,
        MatchProcessStatus.ProcessStatus.IN_PROGRESS,
        LocalDateTime.now()
    );
  }
}