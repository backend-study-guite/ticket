package com.study.ticket.domain.repository;

import com.study.ticket.domain.Entity.Seat;
import com.study.ticket.domain.constant.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByConcertOptionIdAndStatus(Long concertOptionId, SeatStatus status);

    @Query("SELECT s " +
            "FROM Seat s " +
            "JOIN Reservation r " +
            "ON s.id = r.seatId " +
            "WHERE r.userId = :userId")
    List<Seat> findReservedSeatsByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id = :seatId")
    Optional<Seat> findByIdWithLock(@Param("seatId") Long seatId);
}
