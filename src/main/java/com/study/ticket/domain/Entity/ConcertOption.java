package com.study.ticket.domain.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "concert_option")
@NoArgsConstructor
public class ConcertOption {

    @Id
    @Column(name = "concert_option_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "concert_id")
    private Long concertId;

}
