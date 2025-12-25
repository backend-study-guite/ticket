package com.study.ticket.domain.repository;

import com.study.ticket.domain.Entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertRepository extends JpaRepository<Concert, Long> {

}
