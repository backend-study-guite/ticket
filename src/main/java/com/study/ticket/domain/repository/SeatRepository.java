package com.study.ticket.domain.repository;

import com.study.ticket.domain.Entity.Seat;
import com.study.ticket.domain.constant.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id = :id")
    Optional<Seat> findByIdForUpdate(@Param("id") Long id);

    List<Seat> findAllByConcertOptionIdAndStatus(
            Long concertOptionId,
            SeatStatus status,
            Sort sort
    );
    // 호출


}
