package com.kslj.mannam.domain.journal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JournalRequestDto {
    private String content;
    private String stamp;
    private List<String> images;
}
