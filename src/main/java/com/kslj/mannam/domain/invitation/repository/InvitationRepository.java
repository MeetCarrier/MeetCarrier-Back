package com.kslj.mannam.domain.invitation.repository;

import com.kslj.mannam.domain.invitation.entity.Invitation;
import com.kslj.mannam.domain.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Invitation findByMatch(Match match);
}
