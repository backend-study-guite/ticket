package com.study.ticket.domain.service;

import com.study.ticket.domain.dto.request.ChargePointRequest;
import com.study.ticket.domain.dto.request.PaymentRequest;
import com.study.ticket.domain.dto.request.ReserveSeatRequest;
import com.study.ticket.domain.dto.response.ConcertListResponse;
import com.study.ticket.domain.dto.response.ConcertOptionListResponse;
import com.study.ticket.domain.dto.response.SeatListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketingService {

    /**
     * 콘서트 목록을 조회하는 메서드
     * @return
     */
    public ConcertListResponse getConcerts() {
        return null;
    }

    /**
     * 콘서트 옵션을 조회하는 메서드
     * @param concertId
     * @return
     */
    public ConcertOptionListResponse getConcertOptions(Long concertId) {
        return null;
    }

    /**
     * 예매가능한 좌석을 조회하는 메서드
     * @param concertOptionId
     * @return
     */
    public SeatListResponse getAvailableSeats(Long concertOptionId) {
        return null;
    }

    /**
     * 유저가 예약 또는 구매한 좌석을 조회하는 메서드
     * @param userId
     * @return
     */
    public SeatListResponse getReservedSeats(Long userId) {
        return null;
    }

    /**
     * 좌석을 예약하는 메서드
     * [카프카 연동 로직]
     * 1. 이전 단계에서 논의한 대기열 및 분산 락 로직 수행.
     * 2. DB 저장 성공 시, ReservationEvent 객체 생성.
     * 3. 'reservation-topic'으로 이벤트 전송.
     * 4. 주의: 전송 실패 시 로그를 남기되, 사용자 예약 자체를 롤백할지(Strong Consistency)
     * 아니면 별도 재시도를 할지(Eventual Consistency) 정책 결정 필요.
     */
    public String reserveSeat(ReserveSeatRequest request) {
        return null;
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
