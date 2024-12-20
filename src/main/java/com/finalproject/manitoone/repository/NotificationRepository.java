package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.constants.NotiType;
import com.finalproject.manitoone.domain.Notification;
import com.finalproject.manitoone.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(User user, LocalDateTime thirtyDaysAgo);
  boolean existsByUserEmailAndIsRead(String email, Boolean isRead);
  List<Notification> findByUserAndIsReadFalse(User user);

  Notification findByUserAndSenderUserAndType(User receiveUser, User senderUser, NotiType type);
  Notification findByUserAndSenderUserAndTypeAndRelatedObjectId(User receiveUser, User senderUser, NotiType type, Long relatedObjectId);
  Optional<List<Notification>> findByTypeInAndRelatedObjectId(List<NotiType> types, Long relatedObjectId);
}
