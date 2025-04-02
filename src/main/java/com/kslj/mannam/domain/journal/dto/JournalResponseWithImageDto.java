package com.kslj.mannam.domain.journal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JournalResponseWithImageDto {
    String content;
    String stamp;
    LocalDateTime createdAt;
    List<String> imageUrls;
}
