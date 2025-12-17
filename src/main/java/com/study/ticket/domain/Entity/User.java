package com.study.ticket.domain.Entity;

import com.study.ticket.common.exception.CustomException;
import com.study.ticket.common.exception.ExceptionCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "points", columnDefinition = "int default 0")
    private Long points = 0L;

    public void chargePoint(Long amount) {
        if (amount <= 0L) throw new CustomException(ExceptionCode.ILLEGAL_POINTS);
        this.points += amount;
    }

    public void usePoint(Long amount) {
        if (amount > this.points) throw new CustomException(ExceptionCode.NOT_ENOUGH_POINTS);

        this.points -= amount;
    }
}

