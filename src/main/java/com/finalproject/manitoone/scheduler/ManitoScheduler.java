package com.finalproject.manitoone.scheduler;

import com.finalproject.manitoone.domain.ManitoMatches;
import com.finalproject.manitoone.repository.ManitoMatchesRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ManitoScheduler {

  private final ManitoMatchesRepository manitoMatchesRepository;

  @Scheduled(fixedRate = 2 * 60 * 60 * 1000) // 2시간마다 실행
  public void updateExpiredMatches() {
    // 현재 시간 기준 24시간 전 계산
    LocalDateTime deadline = LocalDateTime.now().minusHours(24);

    // 24시간 이상 경과된 MATCHED 상태에서 ManitoLetter가 없는 엔티티 찾기
    List<ManitoMatches> expiredMatches = manitoMatchesRepository.findUnansweredMatches(deadline);

    if (!expiredMatches.isEmpty()) {
      log.info("만기된 매칭 수: {}", expiredMatches.size());

      expiredMatches.forEach(ManitoMatches::markAsExpired);
      manitoMatchesRepository.saveAll(expiredMatches);

    } else {
      log.info("만기된 매칭이 없습니다.");
    }
  }
}
