package com.kslj.mannam.firebase;

import com.kslj.mannam.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    List<FcmToken> findAllByUser(User user);
    Optional<FcmToken> findByToken(String token);

    void deleteByToken(String token);
}
