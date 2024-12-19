package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.AiPostLog;
import com.finalproject.manitoone.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AiPostLogRepository extends JpaRepository<AiPostLog, Long> {

  Optional<AiPostLog> findByPostPostId(Long postId);

  Optional<AiPostLog> findTopByPost_UserAndAiContentIsNotNullOrderByPost_CreatedAtDesc(User user);

  Optional<List<AiPostLog>> findAllByPostPostId(Long postId);

  // 특정 User가 오늘 작성한 AI 피드백 로그 개수 조회
  @Query("SELECT COUNT(a) " +
      "FROM AiPostLog a " +
      "WHERE a.post.user = :user " +
      "AND a.post.createdAt BETWEEN :startOfToday AND :endOfToday")
  long countTodayAiFeedbacksByUser(
      @Param("user") User user,
      @Param("startOfToday") LocalDateTime startOfToday,
      @Param("endOfToday") LocalDateTime endOfToday
  );
}
