package com.kslj.mannam.journal;

import com.kslj.mannam.TestUtils;
import com.kslj.mannam.domain.journal.dto.JournalRequestDto;
import com.kslj.mannam.domain.journal.dto.JournalResponseDto;
import com.kslj.mannam.domain.journal.dto.JournalResponseWithImageDto;
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
import java.util.ArrayList;
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
    private UserService userService;

    @Autowired
    private TestUtils testUtils;

    // JournalRequestDto 생성 메서드
    private JournalRequestDto createJournalRequest(String content, String stamp, List<String> imageUrls) {
        return JournalRequestDto.builder()
                .content(content)
                .stamp(stamp)
                .imageUrls(imageUrls)
                .build();
    }

    // 새로운 일기 추가 및 조회 테스트 (이미지 없이)
    @Test
    public void testCreateJournal() {
        // given
        User foundUser = testUtils.createAndGetTestUser();
        JournalRequestDto journalRequestDto = createJournalRequest("오늘은 좋은 날이었다.", "좋아요", new ArrayList<>());

        // when
        long savedJournalId = journalService.saveJournal(journalRequestDto, foundUser);
        JournalResponseWithImageDto foundJournal = journalService.getJournalById(savedJournalId);

        // then
        System.out.println("journalRequestDto.getContent() = " + journalRequestDto.getContent());
        System.out.println("foundJournal.getContent() = " + foundJournal.getContent());
        Assertions.assertThat(foundJournal.getContent()).isEqualTo(journalRequestDto.getContent());
    }

    // 새로운 일기 추가 및 조회 테스트 (이미지 없이)
    @Test
    public void testCreateJournalWithImage() {
        // given
        User foundUser = testUtils.createAndGetTestUser();
        JournalRequestDto journalRequestDto = createJournalRequest("오늘은 좋은 날이었다.", "좋아요", new ArrayList<String>(Arrays.asList("1.jpg", "2.jpg", "3.jpg")));

        // when
        long savedJournalId = journalService.saveJournal(journalRequestDto, foundUser);
        JournalResponseWithImageDto foundJournal = journalService.getJournalById(savedJournalId);

        // then
        System.out.println("journalRequestDto.getImageUrls() = " + journalRequestDto.getImageUrls());
        System.out.println("foundJournal.getImageUrls() = " + foundJournal.getImageUrls());
        Assertions.assertThat(foundJournal.getImageUrls()).isEqualTo(journalRequestDto.getImageUrls());
    }

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

    // 기존 일기 수정 테스트
    @Test
    public void testUpdateJournal() {
        // given
        User foundUser = testUtils.createAndGetTestUser();
        JournalRequestDto journalRequestDto = createJournalRequest("오늘은 좋은 날이었다.", "좋아요", new ArrayList<String>(Arrays.asList("1.jpg", "2.jpg", "3.jpg")));

        long journalId = journalService.saveJournal(journalRequestDto, foundUser);

        // when
        JournalRequestDto updatedRequestDto = createJournalRequest("테스트", "슬퍼요", new ArrayList<>(Arrays.asList("3.jpg", "4.jpg", "5.jpg")));
        journalService.updateJournal(journalId, updatedRequestDto);
        JournalResponseWithImageDto updatedJournal = journalService.getJournalById(journalId);

        // then
        System.out.println("updatedJournal.getContent() = " + updatedJournal.getContent());
        System.out.println("updatedJournal.getImageUrls() = " + updatedJournal.getImageUrls());
        Assertions.assertThat(updatedJournal.getImageUrls()).isEqualTo(updatedRequestDto.getImageUrls());

    }

    // 일기 삭제 테스트
    @Test
    public void testDeleteJournal() {
        // given
        User foundUser = testUtils.createAndGetTestUser();
        JournalRequestDto journalRequestDto1 = createJournalRequest("오늘은 좋은 날이었다.", "좋아요", new ArrayList<>());
        JournalRequestDto journalRequestDto2 = createJournalRequest("오늘은 좋은 날이었다.", "좋아요", new ArrayList<>());
        JournalRequestDto journalRequestDto3 = createJournalRequest("오늘은 좋은 날이었다.", "좋아요", new ArrayList<>());

        long journalId1 = journalService.saveJournal(journalRequestDto1, foundUser);
        long journalId2 = journalService.saveJournal(journalRequestDto2, foundUser);
        long journalId3 = journalService.saveJournal(journalRequestDto3, foundUser);

        // when
        journalService.deleteJournal(journalId1);
        List<JournalResponseDto> journalsByYearAndMonth = journalService.getJournalsByYearAndMonth(foundUser, 2025, 4);

        // then
        Assertions.assertThat(journalsByYearAndMonth.size()).isEqualTo(2);
    }
}
