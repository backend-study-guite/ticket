package com.study.ticket.domain.repository;

import com.study.ticket.domain.Entity.ConcertOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConcertOptionRepository extends JpaRepository<ConcertOption, Long> {
    List<ConcertOption> findByConcertId(Long concertId);

    List<ConcertOption> findByConcertIdAndStartTimeIsAfter(Long concertId, LocalDateTime startTimeAfter);
}
