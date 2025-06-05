package com.kslj.mannam.domain.notification.repository;

import com.kslj.mannam.domain.notification.entity.Notification;
import com.kslj.mannam.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findNotificationByUser(User user);
    void removeNotificationByUser(User user);
    boolean existsByUserAndReferenceId(User user, Long referenceId);
    boolean existsByUserAndIsReadFalse(User user);
}
