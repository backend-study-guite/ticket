package com.study.ticket.domain.controller;

import com.study.ticket.domain.dto.request.ChargePointRequest;
import com.study.ticket.domain.dto.request.PaymentRequest;
import com.study.ticket.domain.dto.request.ReserveSeatRequest;
import com.study.ticket.domain.dto.response.ConcertListResponse;
import com.study.ticket.domain.dto.response.ConcertOptionListResponse;
import com.study.ticket.domain.dto.response.SeatListResponse;
import com.study.ticket.domain.service.TicketingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ticketing")
@RequiredArgsConstructor
public class TicketingController {

    private final TicketingService ticketingService;

    /**
     * 공연목록을 조회하는 API
     * @return
     */
    @GetMapping("/concerts")
    public ResponseEntity<ConcertListResponse> getConcerts() {
        return null;
    }

    /**
     * 공연 옵션을 조회하는 API
     * @return
     */
    @GetMapping("/concerts/{concertId}/options")
    public ResponseEntity<ConcertOptionListResponse> getConcertOptions(@PathVariable Long concertId) {
        return null;
    }

    /**
     * 공연 옵션에서 예매가능한 좌석을 조회하는 API
     * @param concertOptionId
     * @return
     */
    @GetMapping("/concerts/options/{concertOptionId}/seats")
    public ResponseEntity<SeatListResponse> getAvailableSeats(@PathVariable Long concertOptionId) {
        return null;
    }

    /**
     * 예약된(결제완료된) 좌석 목록을 조회하는 API
     * @param userId
     * @return
     */
    @GetMapping("/user/{userId}/reservations")
    public ResponseEntity<SeatListResponse> getReservedSeats(@PathVariable Long userId) {
        return null;
    }

    /**
     * 좌석을 예약하는 API
     * @param request
     * @return
     */
    @PostMapping("/reservations")
    public ResponseEntity<String> reserveSeat(@RequestBody ReserveSeatRequest request) {
        /*
         * [HTTP 요청 진입 지점]
         *
         * - 클라이언트(Postman, 프론트엔드 등)가
         *   POST /api/ticketing/reservations 로 요청을 보냄
         *
         * - 요청 Body(JSON 예시):
         *   {
         *     "seatId": 1,
         *     "userId": 10
         *   }
         */

        // @RequestBody의 역할:
        // 1) HTTP Body(JSON)를 읽어서
        // 2) ReserveSeatRequest DTO 객체로 변환한다
        //    → seatId, userId 값이 DTO 안에 들어 있음
        String result = ticketingService.reserveSeat(request);
        /*
         * [Service 호출]
         *
         * - 컨트롤러는 "비즈니스 판단"을 하지 않는다
         * - 단순히:
         *   "좌석 예약 요청이 왔어요" 라고 서비스에게 전달하는 역할
         *
         * - request 안에는:
         *   request.seatId()
         *   request.userId()
         *   가 들어 있음
         */

        // HTTP 200 OK 응답으로 문자열 반환
        return ResponseEntity.ok(result);
    }

    /**
     * 예약된 좌석을 결제하는 API
     * @param request
     * @return
     */
    @PostMapping("/payments")
    public ResponseEntity<String> payment(@RequestBody PaymentRequest request) {
        /*
         * 요청 Body(JSON 예시):
         * {
         *   "userId": 10,
         *   "reservationId": 5
         * }
         */


        String result = ticketingService.payment(request);

        /*
         * - 결제 처리 자체는 서비스에서 수행
         * - 컨트롤러는 단순 전달자
         */
        return ResponseEntity.ok(result);

    }

    /**
     * 포인트를 충전하는 API
     * @param request
     * @return
     */
    @PostMapping("/point/charge")
    public ResponseEntity<Long> chargePoint(@RequestBody ChargePointRequest request) {
        return null;
    }
}
