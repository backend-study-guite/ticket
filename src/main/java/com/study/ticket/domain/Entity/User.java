package com.study.ticket.domain.Entity;

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
        if(amount == null || amount <=0) {
            throw new IllegalArgumentException("사용 금액이 0보다 커야합니다");
        }
        if(this.points < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다. 현재포인트: " + this.points);
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

