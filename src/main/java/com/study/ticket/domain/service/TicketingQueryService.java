package com.study.ticket.domain.service;

import com.study.ticket.domain.Entity.Concert;
import com.study.ticket.domain.Entity.ConcertOption;
import com.study.ticket.domain.Entity.Reservation;
import com.study.ticket.domain.Entity.Seat;
import com.study.ticket.domain.constant.ReservationStatus;
import com.study.ticket.domain.constant.SeatStatus;
import com.study.ticket.domain.dto.response.ConcertListResponse;
import com.study.ticket.domain.dto.response.ConcertOptionListResponse;
import com.study.ticket.domain.dto.response.SeatListResponse;
import com.study.ticket.domain.repository.ConcertOptionRepository;
import com.study.ticket.domain.repository.ConcertRepository;
import com.study.ticket.domain.repository.ReservationRepository;
import com.study.ticket.domain.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketingQueryService {

    // Repository는 "DB 조회 담당" (SQL을 대신 만들어 실행)
    private final ConcertRepository concertRepository;
    private final ConcertOptionRepository concertOptionRepository;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 콘서트 목록을 조회하는 메서드
     * @return
     */
    public ConcertListResponse getConcerts() {
        // [1] Repository 호출 → DB에서 Concert 엔티티 리스트를 가져온다.
        List<Concert> concerts = concertRepository.findAll();
        // [2] 엔티티(List<Concert>)를 응답 DTO(ConcertListResponse)로 변환
        return ConcertListResponse.from(concerts);
    }

    /**
     * 콘서트 옵션을 조회하는 메서드
     * @param concertId
     * @return
     */
    public ConcertOptionListResponse getConcertOptions(Long concertId) {
        // [1] DB 조회: concert_id가 같은 옵션들만 가져온다.
        List<ConcertOption> options = concertOptionRepository.findAllByConcertId(concertId);
        // [2] 엔티티 → DTO 변환
        return ConcertOptionListResponse.from(options);
    }

    /**
     * 예매가능한 좌석을 조회하는 메서드
     * @param concertOptionId
     * @return
     */
    public SeatListResponse getAvailableSeats(Long concertOptionId) {
        // [1] DB 조회:
        //     - concertOptionId가 같은 좌석 중
        //     - status가 AVAILABLE인 것만
        //     - rowChar, colNumber로 정렬해서 가져온다.
        List<Seat> seats = seatRepository.findAllByConcertOptionIdAndStatus(
                concertOptionId,
                SeatStatus.AVAILABLE,
                Sort.by(Sort.Order.asc("rowChar"),Sort.Order.asc("colNumber"))
        );

        // [2] 엔티티 → DTO 변환
        return SeatListResponse.from(seats);
    }

    /**
     * 유저가 예약 또는 구매한 좌석을 조회하는 메서드
     * @param userId
     * @return
     */
    public SeatListResponse getReservedSeats(Long userId) {

        // [1] 예약 테이블에서 "userId + PAID" 조건으로 예약 목록을 가져온다.
        //     이유: Seat 테이블에는 userId가 없고,
        //          "누가 어떤 좌석을 샀는지" 관계는 Reservation이 가지고 있기 때문.
        List<Reservation> reservations =
                reservationRepository.findAllByUserIdAndStatus(userId, ReservationStatus.PAID);

        // [2] Reservation 엔티티들에서 seatId만 뽑는다.
        //     reservation 1개가 seat 1개를 가리키는 구조라서 가능.
        List<Long> seatIds = reservations.stream()
                .map(Reservation::getSeatId)
                .distinct() // 혹시 모를 중복 방지
                .toList();

        // [3] seatId 목록으로 Seat 엔티티들을 조회한다.
        //     seatIds가 비어있으면 DB를 굳이 조회하지 않고 빈 리스트 반환
        List<Seat> seats = seatIds.isEmpty()
                ? List.of()
                : seatRepository.findAllById(seatIds);

        // [4] 엔티티 → DTO 변환
        return SeatListResponse.from(seats);

    }

}
