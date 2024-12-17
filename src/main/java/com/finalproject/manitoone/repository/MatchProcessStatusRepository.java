package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.MatchProcessStatus;
import com.finalproject.manitoone.domain.MatchProcessStatus.ProcessStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchProcessStatusRepository extends JpaRepository<MatchProcessStatus, Long> {
  Optional<MatchProcessStatus> findFirstByNicknameAndStatusOrderByCreatedAtDesc(
      String nickname,
      ProcessStatus status
  );

  void deleteByCreatedAtBefore(LocalDateTime cutoff);

  boolean existsByNicknameAndStatusAndTimeoutAtAfter(
      String nickname,
      ProcessStatus status,
      LocalDateTime now
  );
}
