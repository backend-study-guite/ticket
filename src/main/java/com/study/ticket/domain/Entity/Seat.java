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
    private SeatStatus status = SeatStatus.AVAILABLE;

    public boolean isAvailable(){
        return this.status == SeatStatus.AVAILABLE;
    }

    public void reserve(){
        if (this.status != SeatStatus.AVAILABLE){
            // ✅ 이미 RESERVED/PAID 이면 예약 불가
            throw new CustomException(ExceptionCode.SEAT_RESERVE_INVALID_STATE);
        }
        this.status = SeatStatus.RESERVED;
    }


    public void pay(){
        if(this.status != SeatStatus.RESERVED) {
            // ✅ RESERVED가 아니면 결제 처리 불가
            throw new CustomException(ExceptionCode.SEAT_PAY_INVALID_STATE);
        }
        this.status = SeatStatus.PAID;
    }

    public void release(){
        if(this.status != SeatStatus.RESERVED){
            // ✅ RESERVED 상태가 아니면 취소/환불 불가
            throw new CustomException(ExceptionCode.SEAT_RELEASE_INVALID_STATE);
        }
        this.status = SeatStatus.AVAILABLE;
    }

    //테스트용 코드
    public static Seat create(Long concertOptionId, String seatNumber) {
        Seat s = new Seat();
        s.concertOptionId = concertOptionId;
        s.seatNumber = seatNumber;
        s.status = SeatStatus.AVAILABLE;
        return s;
    }


}
