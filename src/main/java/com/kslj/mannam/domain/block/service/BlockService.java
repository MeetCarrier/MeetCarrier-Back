package com.kslj.mannam.domain.block.service;

import com.kslj.mannam.domain.block.dto.BlockRequestDto;
import com.kslj.mannam.domain.block.dto.BlockResponseDto;
import com.kslj.mannam.domain.block.entity.Block;
import com.kslj.mannam.domain.block.repository.BlockRepository;
import com.kslj.mannam.domain.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BlockService {

    private final BlockRepository blockRepository;

    // 전화번호 등록
    @Transactional
    public long createBlock(User user, BlockRequestDto requestDto) {
        Block newBlock = Block.builder()
                .blockedPhone(requestDto.getBlockedPhone())
                .blockedInfo(requestDto.getBlockedInfo())
                .user(user)
                .build();

        Block savedBlock = blockRepository.save(newBlock);
        return savedBlock.getId();
    }

    // 전화번호 목록 반환
    @Transactional(readOnly = true)
    public List<BlockResponseDto> getBlocks(User user) {
        List<Block> blockList = blockRepository.getBlockByUser(user);

        List<BlockResponseDto> dtos = new ArrayList<>();

        for (Block block : blockList) {
            dtos.add(BlockResponseDto.fromEntity(block));
        }

        return dtos;
    }

    // 전화번호 정보 업데이트
    @Transactional
    public long updateBlock(long blockId, BlockRequestDto requestDto, User user) {
        Block block = blockRepository.findById(blockId)
                .orElseThrow(() -> new EntityNotFoundException("차단 정보를 찾을 수 없습니다. blockId = " + blockId));

        if (!block.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 정보를 수정할 권한이 없습니다.");
        }

        block.updateBlockedPhone(requestDto.getBlockedPhone());
        block.updateInfo(requestDto.getBlockedInfo());

        return block.getId();
    }

    // 전화번호 삭제
    @Transactional
    public void deleteBlock(long blockId, User user) {
        Block block = blockRepository.findById(blockId)
                .orElseThrow(() -> new EntityNotFoundException("차단 정보를 찾을 수 없습니다. blockId = " + blockId));

        if (!block.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 정보를 삭제할 권한이 없습니다.");
        }

        blockRepository.delete(block);
    }
}
