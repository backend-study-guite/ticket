package com.study.ticket.domain.Entity;

import com.study.ticket.common.exception.CustomException;
import com.study.ticket.common.exception.ExceptionCode;
import com.study.ticket.domain.constant.SeatStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "seat")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {

    @Id
    @Column(name = "seat_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_number")
    private String seatNumber;

    @Column(name = "concert_option_id")
    private Long concertOptionId;

    @Column(name = "price")
    private Long price;

    @Column(name = "seat_status")
    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    public void reserve() {
        if (!this.status.equals(SeatStatus.AVAILABLE)) throw new CustomException(ExceptionCode.SEAT_ALREADY_RESERVED);

        this.status = SeatStatus.RESERVED;
    }

    public void payment() {
        if (this.status.equals(SeatStatus.RESERVED)) this.status = SeatStatus.PAID;
    }
}
