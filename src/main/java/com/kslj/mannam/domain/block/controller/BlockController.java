package com.kslj.mannam.domain.block.controller;

import com.kslj.mannam.domain.block.dto.BlockRequestDto;
import com.kslj.mannam.domain.block.entity.Block;
import com.kslj.mannam.domain.block.service.BlockService;
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
public class BlockController {

    private final BlockService blockService;

    // 현재 유저의 블락한 연락처 목록 반환
    @GetMapping("/blocks")
    public ResponseEntity<?> getBlocks(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<Block>  blocks = blockService.getBlocks(userDetails.getUser());

        return ResponseEntity.ok(blocks);
    }

    // 새로운 연락처 추가
    @PostMapping("/blocks/register")
    public ResponseEntity<?> createBlock(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @RequestBody BlockRequestDto blockRequestDto) {

        long savedBlockId = blockService.createBlock(userDetails.getUser(), blockRequestDto);

        return ResponseEntity.ok("새로운 번호가 추가되었습니다. BlockId = " + savedBlockId);
    }

    // 연락처 수정
    @PatchMapping("/blocks/{blockId}")
    public ResponseEntity<?> updateBlock(@RequestBody BlockRequestDto blockRequestDto,
                                         @PathVariable("blockId") long blockId) {
        long updatedBlockId = blockService.updateBlock(blockId, blockRequestDto);

        return ResponseEntity.ok("번호 정보가 업데이트되었습니다. BlockId = " + updatedBlockId);
    }

    // 연락처 삭제
    @DeleteMapping("/blocks/{blockId}")
    public ResponseEntity<?> deleteBlock(@PathVariable(value = "blockId") long blockId) {
        long deletedBlockId = blockService.deleteBlock(blockId);

        return ResponseEntity.ok("번호가 삭제되었습니다. deletedBlockId = " + deletedBlockId);
    }
}
