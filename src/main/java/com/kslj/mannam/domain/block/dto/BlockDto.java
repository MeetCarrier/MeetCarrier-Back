package com.kslj.mannam.domain.block.dto;

import com.kslj.mannam.domain.block.entity.Block;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlockDto {
    String blockedPhone;
    String blockedInfo;

    public static BlockDto fromEntity(Block block) {
        return BlockDto.builder()
                .blockedPhone(block.getBlockedPhone())
                .blockedInfo(block.getBlockedInfo())
                .build();
    }
}
