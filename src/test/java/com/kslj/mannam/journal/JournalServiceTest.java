package com.kslj.mannam.journal;

import com.kslj.mannam.TestUtils;
import com.kslj.mannam.domain.journal.dto.JournalResponseDto;
import com.kslj.mannam.domain.journal.entity.Journal;
import com.kslj.mannam.domain.journal.repository.JournalRepository;
import com.kslj.mannam.domain.journal.service.JournalService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Transactional
public class JournalServiceTest {

    @Autowired
    private JournalService journalService;

    @Autowired
    private JournalRepository journalRepository;

    @Autowired
    private TestUtils testUtils;

    // 일기 년/월 기준으로 조회 테스트
    @Test
    public void testGetJournalByYearAndMonth() {
        // given
        User foundUser = testUtils.createAndGetTestUser();

        Journal journal1 = Journal.builder()
                .content("테스트1")
                .stamp("기쁨")
                .user(foundUser)
                .createdAt(LocalDateTime.of(2025, 3, 31, 10, 20, 30))
                .build();

        Journal journal2 = Journal.builder()
                .content("테스트2")
                .stamp("기쁨")
                .user(foundUser)
                .createdAt(LocalDateTime.of(2025, 4, 1, 10, 20, 30))
                .build();

        Journal journal3 = Journal.builder()
                .content("테스트3")
                .stamp("기쁨")
                .user(foundUser)
                .createdAt(LocalDateTime.of(2025, 4, 2, 10, 20, 30))
                .build();

        journalRepository.saveAll(Arrays.asList(journal1, journal2, journal3));

        // when
        List<JournalResponseDto> journalsByYearAndMonth = journalService.getJournalsByYearAndMonth(foundUser, 2025, 4);

        // then
        for(JournalResponseDto journalDto : journalsByYearAndMonth) {
            System.out.println("journalDto.getContent() = " + journalDto.getContent());
        }
        Assertions.assertThat(journalsByYearAndMonth.size()).isEqualTo(2);

    }

}
