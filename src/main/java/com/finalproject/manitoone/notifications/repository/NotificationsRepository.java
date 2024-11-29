package com.finalproject.manitoone.notifications.repository;

import com.finalproject.manitoone.notifications.domain.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationsRepository extends JpaRepository<Notifications, Long> {

}
