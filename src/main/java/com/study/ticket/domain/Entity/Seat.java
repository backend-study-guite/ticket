package com.study.ticket.domain.Entity;

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
            throw new IllegalStateException("예약 불가능한 좌석 상태입니다: " + this.status);
        }
        this.status = SeatStatus.RESERVED;
    }


    public void pay(){
        if(this.status != SeatStatus.RESERVED) {
            throw new IllegalStateException("결제 오류");
        }
        this.status = SeatStatus.PAID;
    }

    public void release(){
        if(this.status != SeatStatus.RESERVED){
            throw new IllegalStateException("환불이 불가능 합니다.");
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
