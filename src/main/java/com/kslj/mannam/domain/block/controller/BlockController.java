package com.kslj.mannam.domain.block.controller;

import com.kslj.mannam.domain.block.dto.BlockDto;
import com.kslj.mannam.domain.block.service.BlockService;
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
@RequestMapping("/api/blocks")
@Tag(name = "블락", description = "사용자 블락 관리 API")
public class BlockController {

    private final BlockService blockService;
    private final UserService userService;

    // 현재 유저의 블락한 연락처 목록 반환
    @Operation(
            summary     = "블락 목록 조회",
            description = "현재 로그인한 사용자가 블락한 연락처 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "조회 성공",
                            content      = @Content(
                                    mediaType = "application/json",
                                    array       = @ArraySchema(
                                            schema = @Schema(implementation = BlockDto.class)
                                    )
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<?> getBlocks(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<BlockDto> blocks = blockService.getBlocks(userService.getUserById(1));

        return ResponseEntity.ok(blocks);
    }

    // 새로운 연락처 추가
    @Operation(
            summary     = "블락 추가",
            description = "새로운 연락처를 블락 목록에 추가합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "블락 등록 DTO",
                    required    = true,
                    content     = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = BlockDto.class)
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
    public ResponseEntity<?> createBlock(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @RequestBody BlockDto blockDto) {

        long savedBlockId = blockService.createBlock(userService.getUserById(1), blockDto);

        return ResponseEntity.ok("새로운 번호가 추가되었습니다. BlockId = " + savedBlockId);
    }

    // 연락처 수정
    @Operation(
            summary     = "블락 정보 수정",
            description = "지정된 블락(contact) 정보를 수정합니다.\n요청 전송 시 필요한 부분의 데이터만 채워서 보내면 됩니다.",
            parameters = {
                    @Parameter(
                            name        = "blockId",
                            description = "수정할 블락의 ID",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "integer", format = "int64")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "블락 수정 DTO",
                    required    = true,
                    content     = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = BlockDto.class)
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
    @PatchMapping("/{blockId}")
    public ResponseEntity<?> updateBlock(@RequestBody BlockDto blockDto,
                                         @PathVariable("blockId") long blockId) {
        long updatedBlockId = blockService.updateBlock(blockId, blockDto);

        return ResponseEntity.ok("번호 정보가 업데이트되었습니다. BlockId = " + updatedBlockId);
    }

    // 연락처 삭제
    @Operation(
            summary     = "블락 삭제",
            description = "지정된 블락 연락처를 삭제합니다.",
            parameters = {
                    @Parameter(
                            name        = "blockId",
                            description = "삭제할 블락의 ID",
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
    @DeleteMapping("/{blockId}")
    public ResponseEntity<?> deleteBlock(@PathVariable("blockId") long blockId) {
        long deletedBlockId = blockService.deleteBlock(blockId);

        return ResponseEntity.ok("번호가 삭제되었습니다. deletedBlockId = " + deletedBlockId);
    }
}
