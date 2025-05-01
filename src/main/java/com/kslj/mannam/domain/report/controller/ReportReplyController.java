package com.kslj.mannam.domain.report.controller;

import com.kslj.mannam.domain.report.dto.ReplyRequestDto;
import com.kslj.mannam.domain.report.entity.ReportReply;
import com.kslj.mannam.domain.report.service.ReportReplyService;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "신고 답변", description = "신고에 대한 답변 관리 API")
public class ReportReplyController {

    private final ReportReplyService reportReplyService;
    private final UserService userService;

    // 답변 등록
    @Operation(
            summary     = "답변 등록",
            description = "지정된 신고(reportId)에 대한 답변을 등록합니다.",
            parameters = {
                    @Parameter(
                            name        = "reportId",
                            description = "답변을 등록할 신고의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "등록할 답변 정보",
                    required    = true,
                    content     = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = ReplyRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description  = "등록 성공",
                            content      = @Content(
                                    mediaType = "application/json",
                                    schema    = @Schema(type = "integer", format = "int64"),
                                    examples  = @ExampleObject(value = "123")
                            )
                    )
            }
    )
    @PostMapping("/{reportId}")
    public ResponseEntity<?> createReportReply(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                               @PathVariable(value = "reportId") long reportId,
                                               @RequestBody ReplyRequestDto replyRequestDto) {
        long replyId = reportReplyService.createReportReply(reportId, replyRequestDto, userService.getUserById(1));

        return ResponseEntity.ok("답변이 등록되었습니다. replyId = " + replyId);
    }

    // 답변 삭제
    @Operation(
            summary     = "답변 삭제",
            description = "지정된 ID의 답변을 삭제합니다.",
            parameters = {
                    @Parameter(
                            name        = "replyId",
                            description = "삭제할 답변의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공")
            }
    )
    @DeleteMapping("/{replyId}")
    public ResponseEntity<?> deleteReportReply(@PathVariable(value = "replyId") long replyId) {
        long deleteId = reportReplyService.deleteReportReply(replyId);

        return ResponseEntity.ok("답변이 삭제되었습니다. replyId = " + deleteId);
    }

    // 답변 수정
    @Operation(
            summary     = "답변 수정",
            description = "지정된 ID의 답변을 수정합니다.",
            parameters = {
                    @Parameter(
                            name        = "replyId",
                            description = "수정할 답변의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 답변 정보",
                    required    = true,
                    content     = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = ReplyRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공")
            }
    )
    @PatchMapping("/{replyId}")
    public ResponseEntity<?> updateReportReply(@PathVariable(value = "replyId") long replyId,
                                               @RequestBody ReplyRequestDto replyRequestDto) {
        long updateId = reportReplyService.updateReportReply(replyId, replyRequestDto);

        return ResponseEntity.ok("답변이 수정되었습니다. replyId = " + updateId);
    }
}
