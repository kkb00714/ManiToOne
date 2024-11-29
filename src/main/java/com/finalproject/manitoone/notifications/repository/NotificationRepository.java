package com.finalproject.manitoone.notifications.repository;

import com.finalproject.manitoone.notifications.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
