package com.kslj.mannam.domain.report.controller;

import com.kslj.mannam.domain.report.dto.ReplyRequestDto;
import com.kslj.mannam.domain.report.entity.ReportReply;
import com.kslj.mannam.domain.report.service.ReportReplyService;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/reply")
public class ReportReplyController {

    private final ReportReplyService reportReplyService;
    private final UserService userService;

    // 답변 등록
    @PostMapping("/{reportId}")
    public ResponseEntity<?> createReportReply(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                               @PathVariable(value = "reportId") long reportId,
                                               @RequestBody ReplyRequestDto replyRequestDto) {
        long replyId = reportReplyService.createReportReply(reportId, replyRequestDto, userService.getUserById(1));

        return ResponseEntity.ok("답변이 등록되었습니다. replyId = " + replyId);
    }

    // 답변 삭제
    @DeleteMapping("/{replyId}")
    public ResponseEntity<?> deleteReportReply(@PathVariable(value = "replyId") long replyId) {
        long deleteId = reportReplyService.deleteReportReply(replyId);

        return ResponseEntity.ok("답변이 삭제되었습니다. replyId = " + deleteId);
    }

    // 답변 수정
    @PatchMapping("/{replyId}")
    public ResponseEntity<?> updateReportReply(@PathVariable(value = "replyId") long replyId,
                                               @RequestBody ReplyRequestDto replyRequestDto) {
        long updateId = reportReplyService.updateReportReply(replyId, replyRequestDto);

        return ResponseEntity.ok("답변이 수정되었습니다. replyId = " + updateId);
    }
}
