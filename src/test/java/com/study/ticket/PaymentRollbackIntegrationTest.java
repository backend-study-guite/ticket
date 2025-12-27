package com.study.ticket;

import com.study.ticket.common.exception.CustomException;
import com.study.ticket.common.exception.ExceptionCode;
import com.study.ticket.domain.Entity.Reservation;
import com.study.ticket.domain.Entity.Seat;
import com.study.ticket.domain.Entity.User;
import com.study.ticket.domain.constant.ReservationStatus;
import com.study.ticket.domain.constant.SeatStatus;
import com.study.ticket.domain.dto.request.PaymentRequest;
import com.study.ticket.domain.repository.ReservationRepository;
import com.study.ticket.domain.repository.SeatRepository;
import com.study.ticket.domain.repository.UserRepository;
import com.study.ticket.domain.service.TicketingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class PaymentRollbackIntegrationTest {

    @Autowired TicketingService ticketingService;

    @Autowired UserRepository userRepository;
    @Autowired SeatRepository seatRepository;
    @Autowired ReservationRepository reservationRepository;

    /**
     * Seat.price 같은 값은 엔티티에 setter가 없어서,
     * 테스트에서만 DB에 직접 update 해서 "결제 가능한 상황"을 만들 때 사용한다.
     */
    @Autowired JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clean() {
        // 테스트끼리 데이터가 섞이지 않게 깨끗하게 비움 (create-drop여도 습관적으로 두면 안정적)
        // FK가 있으면 보통 reservation -> seat/users 순으로 지우는 게 안전하다.
        reservationRepository.deleteAll();
        seatRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * [케이스 1] 포인트 부족 -> 결제 실패 -> 롤백 검증
     *
     * payment() 내부에서 user.usePoint(price)에서 NOT_ENOUGH_POINTS가 터져야 한다.
     * 중요한 건 "예외가 터졌을 때, 이미 바뀐 것처럼 보이던 상태 변경이 DB에 남지 않아야 한다"는 것.
     * (= 트랜잭션 롤백)
     */
    @Test
    void payment_notEnoughPoints_rollsBackAll() {
        // ---------- given: 결제가 '시도'될 수 있는 상태를 미리 만들어 둔다 ----------

        // 결제 요청자
        User user = userRepository.saveAndFlush(User.create("payer"));

        // 결제 대상 좌석
        Seat seat = seatRepository.saveAndFlush(Seat.create(1L, "A1"));

        // 이 유저가 이 좌석을 예약해 둔 상태(기본 NOT_PAID)
        Reservation reservation =
                reservationRepository.saveAndFlush(Reservation.create(user.getId(), seat.getId()));

        // 좌석을 결제 가능한 상태(RESERVED)로 맞춰둔다.
        // - payment()는 seat.pay()에서 RESERVED여야 PAID로 바뀔 수 있음
        jdbcTemplate.update(
                "update seat set seat_status=? where seat_id=?",
                SeatStatus.RESERVED.name(), seat.getId()
        );

        // 좌석 가격을 설정한다 (price가 null이면 payment()에서 0으로 처리되어 포인트 부족이 안 터질 수 있음)
        long price = 1000L;
        jdbcTemplate.update(
                "update seat set price=? where seat_id=?",
                price, seat.getId()
        );

        // 유저 포인트를 일부러 부족하게 세팅 (0 < 1000 이므로 NOT_ENOUGH_POINTS 예외 유도)
        long points = 0L;
        jdbcTemplate.update(
                "update users set points=? where user_id=?",
                points, user.getId()
        );

        PaymentRequest req = new PaymentRequest(user.getId(), reservation.getId());

        // ---------- when & then: 예외 + ExceptionCode까지 정확히 검증 ----------
        assertThatThrownBy(() -> ticketingService.payment(req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    // satisfies: 예외 객체를 꺼내서 내부 필드까지 검증할 때 사용
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getCode()).isEqualTo(ExceptionCode.NOT_ENOUGH_POINTS);
                    // CustomException은 super(code.getMessage())라서 메시지도 동일해야 함
                    assertThat(ce.getMessage()).isEqualTo(ExceptionCode.NOT_ENOUGH_POINTS.getMessage());
                });

        // ---------- 롤백 검증: DB에서 다시 조회해서 상태가 "그대로"인지 확인 ----------
        User reUser = userRepository.findById(user.getId()).orElseThrow();
        Seat reSeat = seatRepository.findById(seat.getId()).orElseThrow();
        Reservation reReservation = reservationRepository.findById(reservation.getId()).orElseThrow();

        // 포인트는 차감되면 안 됨
        assertThat(reUser.getPoints()).isEqualTo(points);
        // 좌석은 결제 실패했으니 PAID로 바뀌면 안 됨 (RESERVED 유지)
        assertThat(reSeat.getStatus()).isEqualTo(SeatStatus.RESERVED);
        // 예약도 결제 실패했으니 PAID로 바뀌면 안 됨 (NOT_PAID 유지)
        assertThat(reReservation.getStatus()).isEqualTo(ReservationStatus.NOT_PAID);
    }

    /**
     * [케이스 2] 예약자 불일치 -> 결제 실패 -> 롤백 검증
     *
     * payment()에서 가장 먼저 reservation.userId == request.userId 검증을 한다.
     * 여기서 예외가 터지면 seat/user 조회(락)까지도 가지 않는 게 정상.
     */
    @Test
    void payment_userMismatch_rollsBackAll() {
        // given
        User owner = userRepository.saveAndFlush(User.create("owner"));
        User attacker = userRepository.saveAndFlush(User.create("attacker"));

        Seat seat = seatRepository.saveAndFlush(Seat.create(1L, "A2"));
        Reservation reservation =
                reservationRepository.saveAndFlush(Reservation.create(owner.getId(), seat.getId()));

        // 결제 가능 상태/가격 세팅(사실 이 케이스에서는 필수는 아니지만 현실적인 상황 구성)
        jdbcTemplate.update("update seat set seat_status=? where seat_id=?",
                SeatStatus.RESERVED.name(), seat.getId());
        jdbcTemplate.update("update seat set price=? where seat_id=?",
                1000L, seat.getId());

        // attacker 포인트 충분히 줘도 "예약자 불일치"면 무조건 실패해야 한다.
        long attackerPoints = 999999L;
        jdbcTemplate.update("update users set points=? where user_id=?",
                attackerPoints, attacker.getId());

        PaymentRequest req = new PaymentRequest(attacker.getId(), reservation.getId());

        // when & then
        assertThatThrownBy(() -> ticketingService.payment(req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getCode()).isEqualTo(ExceptionCode.RESERVATION_USER_MISMATCH);
                    assertThat(ce.getMessage()).isEqualTo(ExceptionCode.RESERVATION_USER_MISMATCH.getMessage());
                });

        // then: DB 재조회로 "변경이 없었는지" 확인
        User reAttacker = userRepository.findById(attacker.getId()).orElseThrow();
        Seat reSeat = seatRepository.findById(seat.getId()).orElseThrow();
        Reservation reReservation = reservationRepository.findById(reservation.getId()).orElseThrow();

        // attacker 포인트 차감이 없어야 함
        assertThat(reAttacker.getPoints()).isEqualTo(attackerPoints);
        // 예약/좌석 상태도 결제 실패로 인해 바뀌면 안 됨
        assertThat(reSeat.getStatus()).isEqualTo(SeatStatus.RESERVED);
        assertThat(reReservation.getStatus()).isEqualTo(ReservationStatus.NOT_PAID);
    }

    /**
     * [케이스 3] 이미 결제된 예약 -> 결제 실패 -> 롤백 검증
     *
     * payment()에서 reservation.isNotPaid()가 false면 RESERVATION_ALREADY_PAID로 막는다.
     * 즉, reservation.status가 PAID인 상태에서 결제를 다시 시도하면 실패해야 한다.
     */
    @Test
    void payment_alreadyPaid_rollsBackAll() {
        // given
        User user = userRepository.saveAndFlush(User.create("payer2"));
        Seat seat = seatRepository.saveAndFlush(Seat.create(1L, "A3"));

        // reservation을 만들고 도메인 메서드로 PAID 상태로 변경 (정상 플로우)
        Reservation reservation = Reservation.create(user.getId(), seat.getId());
        reservation.pay(); // NOT_PAID -> PAID (여기서 유효성 검사도 같이 됨)
        reservation = reservationRepository.saveAndFlush(reservation);

        // 좌석/포인트는 충분해도 "이미 결제"면 무조건 실패해야 함
        jdbcTemplate.update("update seat set seat_status=? where seat_id=?",
                SeatStatus.RESERVED.name(), seat.getId());
        jdbcTemplate.update("update seat set price=? where seat_id=?",
                1000L, seat.getId());

        long points = 999999L;
        jdbcTemplate.update("update users set points=? where user_id=?",
                points, user.getId());

        PaymentRequest req = new PaymentRequest(user.getId(), reservation.getId());

        // when & then
        assertThatThrownBy(() -> ticketingService.payment(req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getCode()).isEqualTo(ExceptionCode.RESERVATION_ALREADY_PAID);
                    assertThat(ce.getMessage()).isEqualTo(ExceptionCode.RESERVATION_ALREADY_PAID.getMessage());
                });

        // then: DB 재조회
        User reUser = userRepository.findById(user.getId()).orElseThrow();
        Seat reSeat = seatRepository.findById(seat.getId()).orElseThrow();
        Reservation reReservation = reservationRepository.findById(reservation.getId()).orElseThrow();

        // 실패했으니 포인트/좌석 상태가 바뀌면 안 됨
        assertThat(reUser.getPoints()).isEqualTo(points);
        assertThat(reSeat.getStatus()).isEqualTo(SeatStatus.RESERVED);
        // 이미 PAID였으니 그대로 PAID 유지
        assertThat(reReservation.getStatus()).isEqualTo(ReservationStatus.PAID);
    }
}
