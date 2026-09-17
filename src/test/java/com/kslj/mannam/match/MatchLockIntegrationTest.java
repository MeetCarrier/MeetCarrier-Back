package com.kslj.mannam.match;

import com.kslj.mannam.domain.match.entity.Match;
import com.kslj.mannam.domain.match.enums.MatchStatus;
import com.kslj.mannam.domain.match.repository.MatchRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.enums.Gender;
import com.kslj.mannam.domain.user.enums.SocialType;
import com.kslj.mannam.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MatchLockIntegrationTest {
    @Autowired UserRepository userRepository;
    @Autowired MatchRepository matchRepository;
    @Autowired PlatformTransactionManager transactionManager;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void sharedUserCanOnlyBeCommittedToOneActiveMatch() throws Exception {
        User a = userRepository.saveAndFlush(user("a"));
        User b = userRepository.saveAndFlush(user("b"));
        User c = userRepository.saveAndFlush(user("c"));
        CountDownLatch firstLocked = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        TransactionTemplate transactions = new TransactionTemplate(transactionManager);
        List<MatchStatus> active = List.of(MatchStatus.Matched, MatchStatus.Surveying,
                MatchStatus.Chatting, MatchStatus.Meeting);

        var first = executor.submit(() -> transactions.executeWithoutResult(status -> {
            userRepository.findAllByIdForUpdate(List.of(a.getId(), b.getId()));
            firstLocked.countDown();
            try { releaseFirst.await(2, TimeUnit.SECONDS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
            matchRepository.save(Match.builder().user1(a).user2(b).score(0.8).build());
        }));
        assertThat(firstLocked.await(2, TimeUnit.SECONDS)).isTrue();

        var second = executor.submit(() -> transactions.executeWithoutResult(status -> {
            userRepository.findAllByIdForUpdate(List.of(a.getId(), c.getId()));
            if (!matchRepository.existsActiveMatchForUsers(List.of(a.getId(), c.getId()), active)) {
                matchRepository.save(Match.builder().user1(a).user2(c).score(0.7).build());
            }
        }));
        releaseFirst.countDown();
        first.get(3, TimeUnit.SECONDS);
        second.get(3, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertThat(matchRepository.findAll()).hasSize(1);
    }

    private User user(String suffix) {
        return User.builder().socialId("social-" + suffix).socialType(SocialType.Google)
                .nickname("user-" + suffix).gender(Gender.Male).age(25L).build();
    }
}
