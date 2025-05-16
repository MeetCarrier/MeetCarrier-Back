package com.kslj.mannam.domain.user.repository;

import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.entity.UserActionLog;
import com.kslj.mannam.domain.user.enums.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserActionLogRepository extends JpaRepository<UserActionLog, Long> {
    boolean existsByUserAndActionTypeAndActionDate(User user, ActionType actionType, LocalDate ActionDate);

    List<UserActionLog> findByActionDate(LocalDate actionDate);
}
