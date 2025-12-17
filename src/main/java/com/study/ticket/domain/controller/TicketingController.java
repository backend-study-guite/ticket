package com.study.ticket.domain.controller;

import com.study.ticket.domain.dto.request.ChargePointRequest;
import com.study.ticket.domain.dto.request.PaymentRequest;
import com.study.ticket.domain.dto.request.ReserveSeatRequest;
import com.study.ticket.domain.dto.response.ConcertListResponse;
import com.study.ticket.domain.dto.response.ConcertOptionListResponse;
import com.study.ticket.domain.dto.response.SeatListResponse;
import com.study.ticket.domain.service.TicketingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

        return ResponseEntity
                .ok(ticketingService.getConcerts());
    }

    /**
     * 공연 옵션을 조회하는 API
     * @return
     */
    @GetMapping("/concerts/{concertId}/options")
    public ResponseEntity<ConcertOptionListResponse> getConcertOptions(@PathVariable Long concertId) {
        return ResponseEntity
                .ok(ticketingService.getConcertOptions(concertId));
    }

    /**
     * 공연 옵션에서 예매가능한 좌석을 조회하는 API
     * @param concertOptionId
     * @return
     */
    @GetMapping("/concerts/options/{concertOptionId}/seats")
    public ResponseEntity<SeatListResponse> getAvailableSeats(@PathVariable Long concertOptionId) {
        return ResponseEntity
                .ok(ticketingService.getAvailableSeats(concertOptionId));
    }

    /**
     * 예약된(결제완료된) 좌석 목록을 조회하는 API
     * @param userId
     * @return
     */
    @GetMapping("/user/{userId}/reservations")
    public ResponseEntity<SeatListResponse> getReservedSeats(@PathVariable Long userId) {
        return ResponseEntity
                .ok(ticketingService.getReservedSeats(userId));
    }

    /**
     * 좌석을 예약하는 API
     * @param request
     * @return
     */
    @PostMapping("/reservations")
    public ResponseEntity<String> reserveSeat(@RequestBody ReserveSeatRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ticketingService.reserveSeat(request));
    }

    /**
     * 예약된 좌석을 결제하는 API
     * @param request
     * @return
     */
    @PostMapping("/payments")
    public ResponseEntity<String> payment(@RequestBody PaymentRequest request) {
        return ResponseEntity
                .ok(ticketingService.payment(request));
    }

    /**
     * 포인트를 충전하는 API
     * @param request
     * @return
     */
    @PostMapping("/point/charge")
    public ResponseEntity<Long> chargePoint(@RequestBody ChargePointRequest request) {
        return ResponseEntity
                .ok(ticketingService.chargePoint(request));
    }
}
