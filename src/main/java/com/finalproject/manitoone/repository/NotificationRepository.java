package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.Notification;
import com.finalproject.manitoone.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(User user, LocalDateTime thirtyDaysAgo);
  boolean existsByUserEmailAndIsRead(String email, Boolean isRead);
  List<Notification> findByUserAndIsReadFalse(User user);
}
