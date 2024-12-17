package com.finalproject.manitoone.service;

import com.finalproject.manitoone.domain.ManitoMatches;
import com.finalproject.manitoone.domain.MatchProcessStatus;
import com.finalproject.manitoone.repository.MatchProcessStatusRepository;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncMatchService {

  private final ManitoMatchesService manitoMatchesService;
  private final MatchProcessStatusRepository statusRepository;

  @Async
  public CompletableFuture<ManitoMatches> processMatchAsync(String nickname) {
    try {
      Optional<MatchProcessStatus> existingProcess = statusRepository
          .findFirstByNicknameAndStatusOrderByCreatedAtDesc(
              nickname,
              MatchProcessStatus.ProcessStatus.IN_PROGRESS
          );

      if (existingProcess.isPresent()) {
        MatchProcessStatus status = existingProcess.get();
        if (status.isTimedOut()) {
          status.fail();
          statusRepository.save(status);
        } else {
          return CompletableFuture.completedFuture(null);
        }
      }

      // 새로운 프로세스 상태 생성
      MatchProcessStatus processStatus = MatchProcessStatus.create(nickname);
      statusRepository.save(processStatus);

      // 실제 매칭 프로세스 실행
      ManitoMatches match = manitoMatchesService.createMatch(nickname);

      // 프로세스 완료 상태 업데이트
      processStatus.complete();
      statusRepository.save(processStatus);

      return CompletableFuture.completedFuture(match);

    } catch (Exception e) {
      log.error("Failed to process match for user: {}", nickname, e);
      statusRepository.findFirstByNicknameAndStatusOrderByCreatedAtDesc(
          nickname,
          MatchProcessStatus.ProcessStatus.IN_PROGRESS
      ).ifPresent(status -> {
        status.fail();
        statusRepository.save(status);
      });
      return CompletableFuture.failedFuture(e);
    }
  }
}
