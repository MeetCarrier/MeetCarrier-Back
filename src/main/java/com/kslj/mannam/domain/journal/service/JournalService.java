package com.kslj.mannam.domain.journal.service;

import com.kslj.mannam.domain.journal.dto.JournalRequestDto;
import com.kslj.mannam.domain.journal.dto.JournalResponseDto;
import com.kslj.mannam.domain.journal.dto.JournalResponseWithImageDto;
import com.kslj.mannam.domain.journal.entity.Journal;
import com.kslj.mannam.domain.journal.entity.JournalImage;
import com.kslj.mannam.domain.journal.repository.JournalRepository;
import com.kslj.mannam.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class JournalService {

    private final JournalRepository journalRepository;

    // 년, 월 기준으로 일기 검색 후 목록 제공
    @Transactional(readOnly = true)
    public List<JournalResponseDto> getJournalsByYearAndMonth(User user, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime from = yearMonth.atDay(1).atTime(0, 0, 0);
        LocalDateTime to = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Journal> journals = journalRepository.findByUserIdAndCreatedAtBetween(user.getId(), from, to);

        List<JournalResponseDto> journalResponseDtos = new ArrayList<>();

        for(Journal journal : journals) {
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

        for (String imageUrl : journalRequestDto.getImageUrls()) {
            newJournal.getImages().add(JournalImage.builder().imageUrl(imageUrl).journal(newJournal).build());
        }

        Journal savedJournal = journalRepository.save(newJournal);
        return savedJournal.getId();
    }

    // 특정 일기 조회
    @Transactional
    public JournalResponseWithImageDto getJournalById(Long journalId) {
        Optional<Journal> targetJournal = journalRepository.findById(journalId);
        JournalResponseWithImageDto responseDto;

        if (targetJournal.isEmpty()) {
            throw new RuntimeException("일기를 찾을 수 없습니다. journalId = " + journalId);
        } else {
            Journal journal = targetJournal.get();
            responseDto = JournalResponseWithImageDto.builder()
                    .content(journal.getContent())
                    .stamp(journal.getStamp())
                    .createdAt(journal.getCreatedAt())
                    .imageUrls(journal.getImages().stream().map(JournalImage::getImageUrl).toList())
                    .build();
        }

        return responseDto;
    }

    // 일기 수정
    @Transactional
    public long updateJournal(Long journalId, JournalRequestDto journalRequestDto) {
        Optional<Journal> targetJournal = journalRepository.findById(journalId);

        if (targetJournal.isEmpty()) {
            throw new RuntimeException("일기를 찾을 수 없습니다. journalId = " + journalId);
        } else {
            // 일기의 내용과 도장 업데이트
            Journal journal = targetJournal.get();
            journal.updateContentAndStamp(journalRequestDto.getContent(), journalRequestDto.getStamp());

            // 현재 등록한 이미지 가져오기
            List<JournalImage> existingImages = journal.getImages();
            List<String> existingUrls = existingImages.stream().map(JournalImage::getImageUrl).toList();
            System.out.println("existingUrls = " + existingUrls);

            // 전달된 이미지 목록 가져오기
            List<String> newUrls = journalRequestDto.getImageUrls();
            System.out.println("newUrls = " + newUrls);

            // 제거할 이미지 추출 및 삭제
            List<JournalImage> imagesToRemove = existingImages.stream().filter(img -> !newUrls.contains(img.getImageUrl())).toList();
            imagesToRemove.forEach(journal.getImages()::remove);
            System.out.println("imagesToRemove = " + imagesToRemove);

            // AWS S3에서 이미지 삭제하는 함수 추가 필요

            // 추가할 이미지 추출 및 등록
            List<String> urlsToAdd = newUrls.stream().filter(url -> !existingUrls.contains(url)).toList();
            for (String url : urlsToAdd) {
                journal.getImages().add(JournalImage.builder().imageUrl(url).build());
            }
            System.out.println("urlsToAdd = " + urlsToAdd);
        }

        return journalId;
    }

    // 일기 삭제
    @Transactional
    public long deleteJournal(Long journalId) {
        Optional<Journal> targetJournal = journalRepository.findById(journalId);

        if (targetJournal.isEmpty()) {
            throw new RuntimeException("일기를 찾을 수 없습니다. journalId = " + journalId);
        } else {
            journalRepository.deleteById(journalId);
        }

        return journalId;
    }

}
