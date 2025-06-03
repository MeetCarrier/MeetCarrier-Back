package com.kslj.mannam.domain.meeting.entity;

import com.kslj.mannam.domain.match.entity.Match;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private String location;

    private String note;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    public void updateDate(LocalDateTime date) {
        this.date = date;
    }

    public void updateLocation(String location) {
        this.location = location;
    }

    public void updateNote(String note) {
        this.note = note;
    }
}
