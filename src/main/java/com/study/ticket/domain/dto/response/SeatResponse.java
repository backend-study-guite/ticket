package com.study.ticket.domain.dto.response;

import com.study.ticket.domain.Entity.Seat;
import com.study.ticket.domain.constant.SeatStatus;

public record SeatResponse(Long seatId, String seatNumber, String status) {
    public static SeatResponse from(Seat seat) {
        return new SeatResponse(seat.getId(), seat.getSeatNumber(), seat.getStatus().toString());
    }
}

