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

    public static Reservation of(Long userId, Long seatId) {
        return new Reservation(userId, seatId);
    }

    private Reservation (Long userId, Long seatId) {
        this.userId = userId;
        this.seatId = seatId;
        this.status = ReservationStatus.NOT_PAID;
    }

    public void payment() {
        this.status = ReservationStatus.PAID;
    }
}
