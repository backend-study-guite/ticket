package com.study.ticket.domain.service;

import com.study.ticket.common.exception.CustomException;
import com.study.ticket.common.exception.ExceptionCode;
import com.study.ticket.domain.Entity.Reservation;
import com.study.ticket.domain.Entity.Seat;
import com.study.ticket.domain.Entity.User;
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
     * 좌석을 예약하는 메서드 (단일 요청)
     * @param request
     * @return
     */
    @Transactional
    public String reserveSeatWithoutLock(ReserveSeatRequest request) {
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
     * 좌석을 예약하는 메서드
     * 1. 동시성 제어하는 로직 구현 (비관락 적용)
     * @param request
     * @return
     */
    @Transactional
    public String reserveSeatWithLock(ReserveSeatRequest request) {
        Long userId = request.userId();
        Long seatId = request.seatId();

        // 1. 사용자 존재 체크
        userRepository.findById(userId).orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        // 2. 좌석 존재 체크 + 비관락
        Seat seat = seatRepository.findByIdWithLock(seatId).orElseThrow(() -> new CustomException(ExceptionCode.SEAT_NOT_FOUND));

        // 3. 좌석 예약 상태 체크
        if(seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new CustomException(ExceptionCode.SEAT_ALREADY_RESERVED);
        }

        // 4. 좌석 상태 변경
        seat.changeStatus(SeatStatus.RESERVED);

        // 5. 예약 생성
        Reservation reservation = new Reservation(userId, seatId, ReservationStatus.NOT_PAID);
        reservationRepository.save(reservation);

        return "좌석 예약이 완료되었습니다.";
    }

    /**
     * 예약한 좌석을 결제하는 메서드
     * @param request
     * @return
     */
    @Transactional
    public String payment(PaymentRequest request) {
        Long userId = request.userId();
        Long reservationId = request.reservationId();
        Long usePoint = request.usePoint();

        // 1. 사용자 존재 체크
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        // 2. 예약 - 존재 체크, 중복 결제 방지, 본인 체크
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new CustomException(ExceptionCode.RESERVATION_NOT_FOUND));

        if(reservation.getStatus() == ReservationStatus.PAID) {
            throw new CustomException(ExceptionCode.RESERVATION_ALREADY_PAID);
        }

        if(!reservation.getUserId().equals(userId)) {
            throw new CustomException(ExceptionCode.RESERVATION_NOT_OWNED_BY_USER);
        }

        // 3. 좌석 - 존재 체크, 중복 결제 방지
        Seat seat = seatRepository.findById(reservation.getSeatId()).orElseThrow(() -> new CustomException(ExceptionCode.SEAT_NOT_FOUND));

        if(seat.getStatus() == SeatStatus.PAID) {
            throw new CustomException(ExceptionCode.SEAT_ALREADY_PAID);
        }

        // 4. 포인트 - 사용 가능 체크
        if(user.getPoints() < usePoint) {
            throw new CustomException(ExceptionCode.NOT_ENOUGH_POINTS);
        }

        if (usePoint > seat.getPrice()) {
            throw new CustomException(ExceptionCode.POINT_EXCEEDS_SEAT_PRICE);
        }

        // 5. 상태 변경
        reservation.changeStatus(ReservationStatus.PAID);
        seat.changeStatus(SeatStatus.PAID);
        user.usePoint(usePoint);

        return "결제가 완료되었습니다.";
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
