package com.study.ticket.domain.service;

import com.study.ticket.common.exception.CustomException;
import com.study.ticket.common.exception.ExceptionCode;
import com.study.ticket.domain.Entity.*;
import com.study.ticket.domain.constant.SeatStatus;
import com.study.ticket.domain.dto.request.ChargePointRequest;
import com.study.ticket.domain.dto.request.PaymentRequest;
import com.study.ticket.domain.dto.request.ReserveSeatRequest;
import com.study.ticket.domain.dto.response.ConcertListResponse;
import com.study.ticket.domain.dto.response.ConcertOptionListResponse;
import com.study.ticket.domain.dto.response.SeatListResponse;
import com.study.ticket.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketingService {

    private final ConcertRepository concertRepository;
    private final ConcertOptionRepository concertOptionRepository;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 콘서트 목록을 조회하는 메서드
     * @return
     */
    public ConcertListResponse getConcerts() {

        /*
        VERSION 1

        List<Concert> concerts = concertRepository.findAll();

        return ConcertListResponse.from(concerts);
         */

        String cacheKey = "concerts";
        String lockKey =  cacheKey + ":lock";

        ConcertListResponse cached = (ConcertListResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return cached;

        Boolean acquireLock = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(3));

        if (Boolean.TRUE.equals(acquireLock)) {
            try {
                List<Concert> concerts = concertRepository.findAll();
                ConcertListResponse response = ConcertListResponse.from(concerts);
                redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(5));
                return response;
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new CustomException(ExceptionCode.SERVER_ERROR);
            }
            return getConcerts();
        }
    }

    /**
     * 콘서트 옵션을 조회하는 메서드
     * @param concertId
     * @return
     */
    public ConcertOptionListResponse getConcertOptions(Long concertId) {

        /*
        처음 짰던 코드. 이렇게하면 성능관련 이슈.
        왜?
        -> DB에서 모든 자료를 가지고 온 후 필터링 하는거라, 부하가 걸림.
        해결방법
        -> 쿼리나 메서드를 통해서 필터링 된 자료만 가지고와(메서드 이름이 너무 길어지는 것 같아서 제외했는데,
            나중에 QueryDSL이나 이런걸 통해서 구현하는 것도 생각해봐야겠네
        List<ConcertOption> concertOptions = concertOptionRepository.findByConcertId(concertId)
                .stream()
                .filter(
                        o -> o.getStartTime()
                                .isAfter(LocalDateTime.now()))
                .toList();
         */
        /*
        VERSION 1
        List<ConcertOption> concertOptions = concertOptionRepository.findByConcertIdAndStartTimeIsAfter(concertId, LocalDateTime.now());

        return ConcertOptionListResponse.from(concertOptions);
         */

        String cacheKey = "concerts:" + concertId + ":options";
        String lockKey =  cacheKey + ":lock";

        ConcertOptionListResponse cached = (ConcertOptionListResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return cached;

        Boolean acquireLock = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(3));

        if (Boolean.TRUE.equals(acquireLock)) {
            try {
                List<ConcertOption> concertOptions = concertOptionRepository.findByConcertIdAndStartTimeIsAfter(concertId, LocalDateTime.now());
                ConcertOptionListResponse response = ConcertOptionListResponse.from(concertOptions);
                redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(5));
                return response;
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new CustomException(ExceptionCode.SERVER_ERROR);
            }
            return getConcertOptions(concertId);
        }
    }

    /**
     * 예매가능한 좌석을 조회하는 메서드
     * @param concertOptionId
     * @return
     */
    public SeatListResponse getAvailableSeats(Long concertOptionId) {
        /*
        VERSION 1
        List<Seat> seats = seatRepository.findByConcertOptionIdAndStatus(concertOptionId, SeatStatus.AVAILABLE);

        return SeatListResponse.from(seats);
         */

        String cacheKey = "concerts:options:" + concertOptionId + ":seats";
        String lockKey = cacheKey + ":lock";

        // 캐시 조회
        SeatListResponse cached = (SeatListResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return cached;

        Boolean acquireLock = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(3));

        if (Boolean.TRUE.equals(acquireLock)) {
            try {
                List<Seat> seats = seatRepository.findByConcertOptionIdAndStatus(concertOptionId, SeatStatus.AVAILABLE);
                SeatListResponse response = SeatListResponse.from(seats);
                redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(5));
                return response;
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new CustomException(ExceptionCode.SERVER_ERROR);
            }
            return getAvailableSeats(concertOptionId);
        }
    }

    /**
     * 유저가 예약 또는 구매한 좌석을 조회하는 메서드
     * @param userId
     * @return
     */
    public SeatListResponse getReservedSeats(Long userId) {
        /*
         처음 내가 짠 코드
         이 방식의 문제점 : N + 1문제 발생.
         왜?
         -> 모든 seatId에 관해서 쿼리를 날리므로.
         해결방법
         1. 연관관계 매핑을 했을 경우, fetch join 사용
         2. 연관관계 매핑을 하지 않았을 경우, 개발자가 직접 쿼리를 작성.

         List<Reservation> reservations = reservationRepository.findByUserId(userId);

         List<Seat> reservedSeats = reservations
                .stream()
                .map(o1 -> seatRepository.findById(o1.getSeatId()).orElse(null))
                .toList();
         */
        List<Seat> reservedSeats = seatRepository.findReservedSeatsByUserId(userId);

        return SeatListResponse.from(reservedSeats);
    }

    /**
     * 좌석을 예약하는 메서드
     * 1. 동시성 제어하는 로직 구현
     * @param request
     * @return
     */
    @Transactional
    public String reserveSeat(ReserveSeatRequest request) {

        Seat seat = seatRepository.findByIdWithLock(request.seatId()).orElseThrow(() -> new CustomException(ExceptionCode.SEAT_NOT_FOUND));

        seat.reserve();

        Reservation reservation = Reservation.of(request.userId(), request.seatId());
        reservationRepository.save(reservation);

        // 예약을 하면 기존에 조회했던 값이랑 달라지므로, 캐싱해줬던 값을 삭제해줘야해
        String cacheKey = "concerts:options:" + seat.getConcertOptionId() + ":seats";
        redisTemplate.delete(cacheKey);

        return seat.getSeatNumber();
    }

    /**
     * 예약한 좌석을 결제하는 메서드
     * @param request
     * @return
     */
    @Transactional
    public String payment(PaymentRequest request) {

        User user = userRepository.findByIdWithLock(request.userId())
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new CustomException(ExceptionCode.RESERVATION_NOT_FOUND));

        return payForReserve(user, reservation);
    }

    /**
     * 포인트를 충전하는 메서드
     * @param request
     * @return
     */
    @Transactional
    public Long chargePoint(ChargePointRequest request) {

        User user = userRepository.findByIdWithLock(request.userId())
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        user.chargePoint(request.amount());

        return user.getPoints();
    }

    // ----- 내부 메서드 -----

    private String payForReserve(User user, Reservation reservation) {
        Long point = user.getPoints();

        Seat seat = seatRepository.findById(reservation.getSeatId()).orElseThrow(() -> new CustomException(ExceptionCode.SEAT_NOT_FOUND));

        Long price = seat.getPrice();

        user.usePoint(price);

        seat.payment();
        reservation.payment();

        return seat.getSeatNumber();
    }
}
