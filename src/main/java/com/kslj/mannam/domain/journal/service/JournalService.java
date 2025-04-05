package com.kslj.mannam.domain.journal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kslj.mannam.domain.journal.dto.JournalRequestDto;
import com.kslj.mannam.domain.journal.dto.JournalResponseDto;
import com.kslj.mannam.domain.journal.entity.Journal;
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
    private final ObjectMapper objectMapper;

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
                List<String> images = objectMapper.readValue(journal.getImages(), new TypeReference<>() {});

                JournalResponseDto responseDto = JournalResponseDto.builder()
                        .id(journal.getId())
                        .content(journal.getContent())
                        .createdAt(journal.getCreatedAt())
                        .stamp(journal.getStamp())
                        .images(images)
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
        String imagesJson;

        try {
            imagesJson = objectMapper.writeValueAsString(journalRequestDto.getImages());
        } catch (Exception e) {
            throw new RuntimeException("이미지 리스트 -> 문자열 변환 실패", e);
        }
        Journal newJournal = Journal.builder()
                .content(journalRequestDto.getContent())
                .stamp(journalRequestDto.getStamp())
                .user(user)
                .images(imagesJson)
                .build();

        for (String image : journalRequestDto.getImages()) {
            // 업로드된 이미지들 경로 변경 코드 필요
        }

        Journal savedJournal = journalRepository.save(newJournal);
        return savedJournal.getId();
    }

    // 일기 수정
    @Transactional
    public long updateJournal(Long journalId, JournalRequestDto journalRequestDto) {
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("일기를 찾을 수 없습니다. journalId = " + journalId));

        // 일기의 내용과 도장 업데이트
        journal.updateContentAndStamp(journalRequestDto.getContent(), journalRequestDto.getStamp());

        try {
            // 현재 등록한 이미지 가져오기
            List<String> existingImages = objectMapper.readValue(journal.getImages(), new TypeReference<>() {});
            System.out.println("existingUrls = " + existingImages);

            // 전달된 이미지 목록 가져오기
            List<String> newImages = journalRequestDto.getImages();
            System.out.println("newUrls = " + newImages);

            // 제거할 이미지 추출
            List<String> imagesToRemove = existingImages.stream()
                    .filter(img -> !newImages.contains(img))
                    .toList();
            System.out.println("imagesToRemove = " + imagesToRemove);

            // 추가할 이미지 추출
            List<String> imagesToAdd = newImages.stream()
                    .filter(img -> !existingImages.contains(img))
                    .toList();
            System.out.println("추가할 이미지 = " + imagesToAdd);

            // AWS S3에서 이미지 삭제하는 함수 추가 필요

            // AWS S3에 업로드된 이미지들 경로 변경 코드 필요

            // 최종적으로 새로운 이미지 리스트 저장
            String imageJson = objectMapper.writeValueAsString(newImages);
            journal.updateImages(imageJson);

        } catch (Exception e) {
            throw new RuntimeException("이미지 리스트 -> 문자열 변환 실패", e);
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

            // AWS S3에 업로드된 이미지 삭제 코드 필요

            journalRepository.deleteById(journalId);
        }

        return journalId;
    }

}
