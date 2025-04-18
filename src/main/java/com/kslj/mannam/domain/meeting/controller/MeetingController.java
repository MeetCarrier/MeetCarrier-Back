package com.kslj.mannam.domain.meeting.controller;

import com.kslj.mannam.domain.meeting.dto.MeetingRequestDto;
import com.kslj.mannam.domain.meeting.dto.MeetingResponseDto;
import com.kslj.mannam.domain.meeting.service.MeetingService;
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
@RequestMapping("/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    // 대면 약속 생성
    @PostMapping("/{matchId}")
    public ResponseEntity<Long> createMeeting(
            @PathVariable long matchId,
            @RequestBody MeetingRequestDto requestDto
    ) {
        long meetingId = meetingService.createMeeting(matchId, requestDto);
        return ResponseEntity.ok(meetingId);
    }

    // 대면 약속 조회
    @GetMapping
    public ResponseEntity<List<MeetingResponseDto>> getMeetings(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<MeetingResponseDto> response = meetingService.getMeetings(userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    // 대면 약속 수정
    @PatchMapping("/{meetingId}")
    public ResponseEntity<Void> updateMeeting(
            @PathVariable long meetingId,
            @RequestBody MeetingRequestDto requestDto
    ) {
        meetingService.updateMeeting(meetingId, requestDto);
        return ResponseEntity.ok().build();
    }

    // 대면 약속 삭제
    @DeleteMapping("/{meetingId}")
    public ResponseEntity<Void> deleteMeeting(@PathVariable long meetingId) {
        meetingService.deleteMeeting(meetingId);
        return ResponseEntity.ok().build();
    }
}
