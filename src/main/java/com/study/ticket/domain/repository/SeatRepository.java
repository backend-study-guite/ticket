package com.study.ticket.domain.repository;

import com.study.ticket.domain.Entity.Seat;
import com.study.ticket.domain.constant.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findAllByConcertOptionIdAndStatus(Long concertOptionId, SeatStatus status);
}
