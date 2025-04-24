package com.kslj.mannam.domain.block.controller;

import com.kslj.mannam.domain.block.dto.BlockDto;
import com.kslj.mannam.domain.block.entity.Block;
import com.kslj.mannam.domain.block.service.BlockService;
import com.kslj.mannam.domain.user.service.UserService;
import com.kslj.mannam.oauth2.entity.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/blocks")
public class BlockController {

    private final BlockService blockService;
    private final UserService userService;

    // 현재 유저의 블락한 연락처 목록 반환
    @GetMapping
    public ResponseEntity<?> getBlocks(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<BlockDto> blocks = blockService.getBlocks(userService.getUserById(1));

        return ResponseEntity.ok(blocks);
    }

    // 새로운 연락처 추가
    @PostMapping("/register")
    public ResponseEntity<?> createBlock(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @RequestBody BlockDto blockDto) {

        long savedBlockId = blockService.createBlock(userService.getUserById(1), blockDto);

        return ResponseEntity.ok("새로운 번호가 추가되었습니다. BlockId = " + savedBlockId);
    }

    // 연락처 수정
    @PatchMapping("/{blockId}")
    public ResponseEntity<?> updateBlock(@RequestBody BlockDto blockDto,
                                         @PathVariable("blockId") long blockId) {
        long updatedBlockId = blockService.updateBlock(blockId, blockDto);

        return ResponseEntity.ok("번호 정보가 업데이트되었습니다. BlockId = " + updatedBlockId);
    }

    // 연락처 삭제
    @DeleteMapping("/{blockId}")
    public ResponseEntity<?> deleteBlock(@PathVariable(value = "blockId") long blockId) {
        long deletedBlockId = blockService.deleteBlock(blockId);

        return ResponseEntity.ok("번호가 삭제되었습니다. deletedBlockId = " + deletedBlockId);
    }
}
