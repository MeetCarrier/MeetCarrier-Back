package com.kslj.mannam.domain.block.dto;

import com.kslj.mannam.domain.block.entity.Block;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockResponseDto {
    long id;
    String blockedPhone;
    String blockedInfo;

    public static BlockResponseDto fromEntity(Block block) {
        return BlockResponseDto.builder()
                .id(block.getId())
                .blockedPhone(block.getBlockedPhone())
                .blockedInfo(block.getBlockedInfo())
                .build();
    }
}
