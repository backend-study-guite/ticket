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
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("충전 오류");
        }

        this.points += amount;
    }

    public void usePoint(Long amount) {
        if (amount == null || amount <= 0) {
            throw new CustomException(ExceptionCode.ILLEGAL_POINTS);
        }
        if (this.points < amount) {
            throw new CustomException(ExceptionCode.NOT_ENOUGH_POINTS);
        }
        this.points -= amount;
    }

    //테스트용 코드
    public static User create(String name) {
        User u = new User();
        u.name = name;
        u.points = 0L;
        return u;
    }

}

