package com.kslj.mannam.domain.journal.controller;

import com.kslj.mannam.domain.journal.dto.JournalRequestDto;
import com.kslj.mannam.domain.journal.dto.JournalResponseDto;
import com.kslj.mannam.domain.journal.service.JournalService;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/journals")
public class JournalController {

    private final JournalService journalService;
    private final UserService userService;

    @GetMapping("/{year}/{month}")
    public ResponseEntity<List<JournalResponseDto>> JournalList(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                @PathVariable(value = "year") int year,
                                                                @PathVariable(value = "month") int month) {
        List<JournalResponseDto> journalList = journalService.getJournalsByYearAndMonth(userService.getUserById(1), year, month);

        return ResponseEntity.ok(journalList);
    }

    @PostMapping("/register")
    public ResponseEntity<?> CreateJournal(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @RequestBody JournalRequestDto requestDto) {
        long savedJournalId = journalService.saveJournal(requestDto, userService.getUserById(1));

        return ResponseEntity.ok("일기가 등록되었습니다. JournalId = " + savedJournalId);
    }

    @PatchMapping("/{journalId}")
    public ResponseEntity<?> UpdateJournal(@PathVariable(value = "journalId") long journalId,
                                           @RequestBody JournalRequestDto requestDto) {
        long updatedJournalId = journalService.updateJournal(journalId, requestDto);

        return ResponseEntity.ok("일기가 업데이트되었습니다. JournalId = " + updatedJournalId);
    }

    @DeleteMapping("/{journalId}")
    public ResponseEntity<?> DeleteJournal(@PathVariable(value = "journalId") long journalId) {
        long deletedJournalId = journalService.deleteJournal(journalId);

        return ResponseEntity.ok("일기가 삭제되었습니다. JournalId = " + deletedJournalId);
    }
}
