package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.AiValidationLog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiValidationLogRepository extends JpaRepository<AiValidationLog, Long> {
  Optional<AiValidationLog> findByPostPostId(Long postId);
}
