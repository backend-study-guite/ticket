package com.study.ticket.domain.Entity;

import com.study.ticket.domain.constant.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "reservation")
@NoArgsConstructor
public class Reservation {

    @Id
    @Column(name = "reservation_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "seat_id")
    private Long seatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status")
    private ReservationStatus status;

    public static Reservation create(Long userId, Long seatId) {
        Reservation r = new Reservation();
        r.userId = userId;
        r.seatId = seatId;
        r.status = ReservationStatus.NOT_PAID;
        return r;
    }

    public boolean isNotPaid() {
        return this.status == ReservationStatus.NOT_PAID;
    }

    public void pay() {
        if (this.status != ReservationStatus.NOT_PAID) {
            throw new IllegalStateException("이미 결제된 예약 입니다.");
        }
        this.status = ReservationStatus.PAID;
    }
}

