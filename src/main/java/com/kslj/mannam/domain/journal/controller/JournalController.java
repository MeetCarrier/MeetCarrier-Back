package com.kslj.mannam.domain.journal.controller;

import com.kslj.mannam.domain.journal.dto.JournalRequestDto;
import com.kslj.mannam.domain.journal.dto.JournalResponseDto;
import com.kslj.mannam.domain.journal.service.JournalService;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/journals")
@Tag(name = "일기", description = "일기 관리 API")
public class JournalController {

    private final JournalService journalService;
    private final UserService userService;

    @Operation(
            summary     = "일기 목록 조회",
            description = "특정 연도(year)와 월(month)에 해당하는 일기 목록을 조회합니다.",
            parameters = {
                    @Parameter(
                            name        = "year",
                            description = "조회할 연도",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int32")
                    ),
                    @Parameter(
                            name        = "month",
                            description = "조회할 월 (1~12)",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int32")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "조회 성공",
                            content      = @Content(
                                    mediaType = "application/json",
                                    array     = @ArraySchema(
                                            schema = @Schema(implementation = JournalResponseDto.class)
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{year}/{month}")
    public ResponseEntity<List<JournalResponseDto>> JournalList(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                @PathVariable("year") int year,
                                                                @PathVariable("month") int month) {
        userService.inspectUserDetails(userDetails);
        List<JournalResponseDto> journalList = journalService.getJournalsByYearAndMonth(userDetails.getUser(), year, month);

        return ResponseEntity.ok(journalList);
    }

    @Operation(
            summary     = "일기 등록",
            description = "새로운 일기를 등록합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "일기 등록 요청 DTO",
                    required    = true,
                    content     = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = JournalRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "등록 성공",
                            content      = @Content(
                                    mediaType = "text/plain",
                                    schema    = @Schema(type = "string")
                            )
                    )
            }
    )
    @PostMapping("/register")
    public ResponseEntity<?> CreateJournal(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @RequestBody JournalRequestDto requestDto) {
        userService.inspectUserDetails(userDetails);
        long savedJournalId = journalService.saveJournal(requestDto, userDetails.getUser());

        return ResponseEntity.ok("일기가 등록되었습니다. JournalId = " + savedJournalId);
    }

    @Operation(
            summary     = "일기 수정",
            description = "지정된 ID의 일기를 수정합니다.\n요청 전송 시 필요한 부분의 데이터만 채워서 보내면 됩니다.",
            parameters = {
                    @Parameter(
                            name        = "journalId",
                            description = "수정할 일기의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "일기 수정 요청 DTO",
                    required    = true,
                    content     = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = JournalRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "수정 성공",
                            content      = @Content(
                                    mediaType = "text/plain",
                                    schema    = @Schema(type = "string")
                            )
                    )
            }
    )
    @PatchMapping("/{journalId}")
    public ResponseEntity<?> UpdateJournal(@PathVariable("journalId") long journalId,
                                           @RequestBody JournalRequestDto requestDto) {
        long updatedJournalId = journalService.updateJournal(journalId, requestDto);

        return ResponseEntity.ok("일기가 업데이트되었습니다. JournalId = " + updatedJournalId);
    }

    @Operation(
            summary     = "일기 삭제",
            description = "지정된 ID의 일기를 삭제합니다.",
            parameters = {
                    @Parameter(
                            name        = "journalId",
                            description = "삭제할 일기의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "삭제 성공",
                            content      = @Content(
                                    mediaType = "text/plain",
                                    schema    = @Schema(type = "string")
                            )
                    )
            }
    )
    @DeleteMapping("/{journalId}")
    public ResponseEntity<?> DeleteJournal(@PathVariable("journalId") long journalId) {
        long deletedJournalId = journalService.deleteJournal(journalId);

        return ResponseEntity.ok("일기가 삭제되었습니다. JournalId = " + deletedJournalId);
    }
}
