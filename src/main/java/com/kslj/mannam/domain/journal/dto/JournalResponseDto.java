package com.kslj.mannam.domain.journal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalResponseDto {
    long id;
    String content;
    String stamp;
    LocalDateTime createdAt;
}
