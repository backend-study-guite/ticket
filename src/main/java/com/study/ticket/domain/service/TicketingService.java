package com.study.ticket.domain.service;

import com.study.ticket.common.exception.CustomException;
import com.study.ticket.common.exception.ExceptionCode;
import com.study.ticket.domain.Entity.Reservation;
import com.study.ticket.domain.Entity.Seat;
import com.study.ticket.domain.constant.ReservationStatus;
import com.study.ticket.domain.constant.SeatStatus;
import com.study.ticket.domain.dto.request.ChargePointRequest;
import com.study.ticket.domain.dto.request.PaymentRequest;
import com.study.ticket.domain.dto.request.ReserveSeatRequest;
import com.study.ticket.domain.dto.response.ConcertListResponse;
import com.study.ticket.domain.dto.response.ConcertOptionListResponse;
import com.study.ticket.domain.dto.response.SeatListResponse;
import com.study.ticket.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketingService {

    private final ConcertRepository concertRepository;
    private final ConcertOptionRepository concertOptionRepository;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    /**
     * 콘서트 목록을 조회하는 메서드
     * @return
     */
    public ConcertListResponse getConcerts() {
        return ConcertListResponse.from(concertRepository.findAll());
    }

    /**
     * 콘서트 옵션을 조회하는 메서드
     * @param concertId
     * @return
     */
    public ConcertOptionListResponse getConcertOptions(Long concertId) {
        return ConcertOptionListResponse.from(concertOptionRepository.findAllByConcertId(concertId));
    }

    /**
     * 예매가능한 좌석을 조회하는 메서드
     * @param concertOptionId
     * @return
     */
    public SeatListResponse getAvailableSeats(Long concertOptionId) {
        return SeatListResponse.from(seatRepository.findAllByConcertOptionIdAndStatus(concertOptionId, SeatStatus.AVAILABLE));
    }

    /**
     * 유저가 예약 또는 구매(결제완료)한 좌석을 조회하는 메서드
     * @param userId
     * @return
     */
    public SeatListResponse getReservedSeats(Long userId) {
        List<Long> seatIds = reservationRepository.findAllSeatIdByUserIdAndStatus(userId, ReservationStatus.PAID);
        return SeatListResponse.from(seatRepository.findAllById(seatIds));
    }

    /**
     * 좌석을 예약하는 메서드
     * 1. 동시성 제어하는 로직 구현
     * @param request
     * @return
     */
    @Transactional
    public String reserveSeat(ReserveSeatRequest request) {
        Long userId = request.userId();
        Long seatId = request.seatId();

        // 사용자, 좌석 존재 체크
        userRepository.findById(userId).orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));
        Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new CustomException(ExceptionCode.SEAT_NOT_FOUND));

        // 좌석 예약 상태 체크
        if(seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new CustomException(ExceptionCode.SEAT_ALREADY_RESERVED);
        }

        // 좌석 예약
        seat.changeStatus(SeatStatus.RESERVED);
        Reservation reservation = new Reservation(userId, seatId, ReservationStatus.NOT_PAID);
        reservationRepository.save(reservation);

        return "좌석 예약이 완료되었습니다.";
    }

    /**
     * 예약한 좌석을 결제하는 메서드
     * @param request
     * @return
     */
    public String payment(PaymentRequest request) {
        return null;
    }

    /**
     * 포인트를 충전하는 메서드
     * @param request
     * @return
     */
    public Long chargePoint(ChargePointRequest request) {
        return null;
    }
}
