package com.study.ticket.domain.Entity;

import com.study.ticket.common.exception.CustomException;
import com.study.ticket.common.exception.ExceptionCode;
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
        return new Reservation(userId, seatId, ReservationStatus.NOT_PAID);
    }

    private Reservation(Long userId, Long seatId, ReservationStatus status) {
        this.userId = userId;
        this.seatId = seatId;
        this.status = status;
    }

    public void validateOwner(Long userId) {
        if(!this.userId.equals(userId)) {
            throw new CustomException(ExceptionCode.RESERVATION_NOT_OWNED_BY_USER);
        }
    }

    public void payment() {
        if(this.status == ReservationStatus.PAID) {
            throw new CustomException(ExceptionCode.RESERVATION_ALREADY_PAID);
        }
        this.status = ReservationStatus.PAID;
    }
}
