package com.kslj.mannam.domain.journal.service;

import com.kslj.mannam.domain.journal.dto.JournalRequestDto;
import com.kslj.mannam.domain.journal.dto.JournalResponseDto;
import com.kslj.mannam.domain.journal.entity.Journal;
import com.kslj.mannam.domain.journal.repository.JournalRepository;
import com.kslj.mannam.domain.notification.enums.NotificationType;
import com.kslj.mannam.domain.notification.service.NotificationService;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.enums.ActionType;
import com.kslj.mannam.domain.user.repository.UserRepository;
import com.kslj.mannam.domain.user.service.UserActionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class JournalService {

    private final JournalRepository journalRepository;
    private final UserActionLogService userActionLogService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // 년, 월 기준으로 일기 검색 후 목록 제공
    @Transactional(readOnly = true)
    public List<JournalResponseDto> getJournalsByYearAndMonth(User user, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime from = yearMonth.atDay(1).atTime(0, 0, 0);
        LocalDateTime to = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Journal> journals = journalRepository.findByUserIdAndCreatedAtBetween(user.getId(), from, to);

        List<JournalResponseDto> journalResponseDtos = new ArrayList<>();

        for(Journal journal : journals) {
            try {
                JournalResponseDto responseDto = JournalResponseDto.builder()
                        .id(journal.getId())
                        .content(journal.getContent())
                        .createdAt(journal.getCreatedAt())
                        .stamp(journal.getStamp())
                        .build();

                journalResponseDtos.add(responseDto);
            } catch (Exception e) {
                throw new RuntimeException("이미지 문자열 -> 리스트 변환 실패", e);
            }
        }

        return journalResponseDtos;
    }

    // 일기 작성
    @Transactional
    public long saveJournal(JournalRequestDto journalRequestDto, User user) {
        Journal newJournal = Journal.builder()
                .content(journalRequestDto.getContent())
                .stamp(journalRequestDto.getStamp())
                .user(user)
                .build();

        Journal savedJournal = journalRepository.save(newJournal);
        userActionLogService.logUserAction(user, ActionType.WRITE_JOURNAL);
        return savedJournal.getId();
    }

    // 일기 수정
    @Transactional
    public long updateJournal(Long journalId, JournalRequestDto journalRequestDto) {
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("일기를 찾을 수 없습니다. journalId = " + journalId));

        // 일기의 내용과 도장 업데이트
        journal.updateContentAndStamp(journalRequestDto.getContent(), journalRequestDto.getStamp());

        return journalId;
    }

    // 일기 삭제
    @Transactional
    public long deleteJournal(Long journalId) {
        Optional<Journal> targetJournal = journalRepository.findById(journalId);

        journalRepository.deleteById(journalId);

        return journalId;
    }

    // 매 20시마다 일기를 작성하지 않은 사람들에게 알림 전송
    @Scheduled(cron = "0 0 20 * * *")
    @Transactional
    public void notifyDiary() {
        // 오늘 날짜 세팅
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        // 모든 유저 조회
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            boolean hasJournalToday = journalRepository.existsByUserAndCreatedAtBetween(user, startOfDay, endOfDay);

            if (!hasJournalToday) {
                notificationService.createNotification(NotificationType.Journal, user, null);
            }
        }
    }
}
