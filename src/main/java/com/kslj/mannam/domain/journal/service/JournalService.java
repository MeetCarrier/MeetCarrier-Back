package com.kslj.mannam.domain.journal.service;

import com.kslj.mannam.domain.journal.dto.JournalRequestDto;
import com.kslj.mannam.domain.journal.dto.JournalResponseDto;
import com.kslj.mannam.domain.journal.entity.Journal;
import com.kslj.mannam.domain.journal.repository.JournalRepository;
import com.kslj.mannam.domain.user.entity.User;
import com.kslj.mannam.domain.user.enums.ActionType;
import com.kslj.mannam.domain.user.service.UserActionLogService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class JournalService {

    private final JournalRepository journalRepository;
    private final UserActionLogService userActionLogService;

    // 년, 월 기준으로 일기 검색 후 목록 제공
    @Transactional(readOnly = true)
    public List<JournalResponseDto> getJournalsByYearAndMonth(User user, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime from = yearMonth.atDay(1).atTime(0, 0, 0);
        LocalDateTime to = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Journal> journals = journalRepository.findByUserIdAndCreatedAtBetween(user.getId(), from, to);

        List<JournalResponseDto> journalResponseDtos = new ArrayList<>();

        for (Journal journal : journals) {
            JournalResponseDto responseDto = JournalResponseDto.builder()
                    .id(journal.getId())
                    .content(journal.getContent())
                    .createdAt(journal.getCreatedAt())
                    .stamp(journal.getStamp())
                    .build();

            journalResponseDtos.add(responseDto);
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
    public long updateJournal(Long journalId, JournalRequestDto journalRequestDto, User user) {
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new EntityNotFoundException("일기를 찾을 수 없습니다. journalId = " + journalId));

        if (!journal.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 일기를 수정할 권한이 없습니다.");
        }

        // 일기의 내용과 도장 업데이트
        journal.updateContentAndStamp(journalRequestDto.getContent(), journalRequestDto.getStamp());

        return journalId;
    }

    // 일기 삭제
    @Transactional
    public void deleteJournal(Long journalId, User user) {
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new EntityNotFoundException("일기를 찾을 수 없습니다. journalId = " + journalId));

        if (!journal.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 일기를 삭제할 권한이 없습니다.");
        }

        journalRepository.delete(journal);
    }
}
