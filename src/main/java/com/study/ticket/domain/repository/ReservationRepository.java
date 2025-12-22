package com.study.ticket.domain.repository;

import com.study.ticket.domain.Entity.Reservation;
import com.study.ticket.domain.constant.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("SELECT r.seatId FROM Reservation r WHERE r.userId = :userId AND r.status = :status")
    List<Long> findAllSeatIdByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ReservationStatus status);
}
