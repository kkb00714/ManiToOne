package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.AiPostLog;
import com.finalproject.manitoone.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiPostLogRepository extends JpaRepository<AiPostLog, Long> {

  Optional<AiPostLog> findByPostPostId(Long postId);
  Optional<AiPostLog> findTopByPost_UserAndAiContentIsNotNullOrderByPost_CreatedAtDesc(User user);
}
