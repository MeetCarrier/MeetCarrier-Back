package com.kslj.mannam.domain.journal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalResponseDto {
    private long id;
    private String content;
    private String stamp;
    private LocalDateTime createdAt;
    private List<String> images;
}
