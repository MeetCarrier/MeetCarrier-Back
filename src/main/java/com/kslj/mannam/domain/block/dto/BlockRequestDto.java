package com.kslj.mannam.domain.block.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlockRequestDto {
    String blockedPhone;
    String blockedInfo;
}
