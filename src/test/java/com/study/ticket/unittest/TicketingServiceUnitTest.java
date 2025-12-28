package com.study.ticket.unittest;

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
import com.study.ticket.domain.repository.ReservationRepository;
import com.study.ticket.domain.repository.SeatRepository;
import com.study.ticket.domain.repository.UserRepository;
import com.study.ticket.domain.service.TicketingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketingServiceUnitTest {

    @InjectMocks
    private TicketingService ticketingService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private User user;

    @BeforeEach
    public void setBeforeTest() throws Exception {
        User user = createTestUser(1L, "홍길동", 10000L);
        lenient().when(userRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(user));
    }

    @Test
    @DisplayName("포인트 충전 성공 테스트 : " +
            "1. 기존 포인트에 충전 금액이 합산되어야 한다.")
    void chargePointTest_success() {
        // given

        // when
        Long result = ticketingService.chargePoint(new ChargePointRequest(1L, 10000L));

        // then
        assertThat(result).isEqualTo(20000L);
    }

    @Test
    @DisplayName("결제 성공 테스트 : " +
            "1. 결제된 좌석의 좌석번호가 반환되어야 한다. " +
            "2. 포인트가 좌석의 가격만큼 깎여야 한다." +
            "3. 좌석과 예약의 상태가 모두 PAID로 바뀌어야 한다.")
    void paymentTest_success() throws Exception {
        // given
        Seat seat = createTestSeat(1L, "A-1", 1L, 8000L, SeatStatus.RESERVED);
        given(seatRepository.findById(1L))
                .willReturn(Optional.of(seat));

        Reservation reservation = createTestReservation(1L, 1L, 1L, ReservationStatus.NOT_PAID);
        given(reservationRepository.findById(1L))
                .willReturn(Optional.of(reservation));

        // when
        String resultSeatNumber = ticketingService.payment(new PaymentRequest(1L, 1L));
        Long resultPoint = user.getPoints();

        // then
        assertThat(resultSeatNumber).isEqualTo("A-1");
        assertThat(resultPoint).isEqualTo(2000L);
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.PAID);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PAID);

        verify(userRepository, times(1)).findByIdWithLock(1L); // userRepository에서 findByIdWithLock이라는 메서드가 실행되었는지
    }

    @Test
    @DisplayName("결제 실패 테스트 : " +
            "1. 포인트가 충분하지 않은 경우 NOT_ENOUGH_POINTS 예외가 던져져야 한다.")
    void paymentTest_failure_notEnoughPoints() throws Exception {
        // given
        Seat seat = createTestSeat(1L, "A-1", 1L, 15000L, SeatStatus.RESERVED);
        given(seatRepository.findById(1L))
                .willReturn(Optional.of(seat));

        Reservation reservation = createTestReservation(1L, 1L, 1L, ReservationStatus.NOT_PAID);
        given(reservationRepository.findById(1L))
                .willReturn(Optional.of(reservation));

        // when
        CustomException result = assertThrows(CustomException.class, () -> {
            ticketingService.payment(new PaymentRequest(1L, 1L));
        });

        // then
        assertThat(result.getCode()).isEqualTo(ExceptionCode.NOT_ENOUGH_POINTS);
        assertThat(result.getCode().getMessage()).isEqualTo(ExceptionCode.NOT_ENOUGH_POINTS.getMessage());
    }

    @Test
    @DisplayName("티켓팅 성공 테스트 : " +
            "1. 예약된 좌석의 좌석번호가 반환되어야 한다." +
            "2. 예약된 좌석의 상태가 RESERVED로 바뀌어야 한다." +
            "3. 새로운 예약이 생성되어 save() 메서드가 실행되어야 한다.")
    void ticketingTest_success() throws Exception {
        // given
        Seat seat = createTestSeat(1L, "A-1", 1L, 8000L, SeatStatus.AVAILABLE);
        given(seatRepository.findByIdWithLock(1L))
                .willReturn(Optional.of(seat));

        // when
        String resultSeatNumber = ticketingService.reserveSeat(new ReserveSeatRequest(1L, 1L));

        // then
        assertThat(resultSeatNumber).isEqualTo("A-1");
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.RESERVED);

        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("티켓팅 실패 테스트 : " +
            "1. 이미 예약된 좌석의 경우 SEAT_ALREADY_RESERVED 예외가 던져져야 한다.")
    void ticketingTest_failure_seatAlreadyReserved() throws Exception {
        // given
        Seat seat = createTestSeat(1L, "A-1", 1L, 8000L, SeatStatus.RESERVED);
        given(seatRepository.findByIdWithLock(1L))
                .willReturn(Optional.of(seat));

        // when
        CustomException result = assertThrows(CustomException.class, () -> {
           ticketingService.reserveSeat(new ReserveSeatRequest(1L, 1L));
        });

        // then
        assertThat(result.getCode()).isEqualTo(ExceptionCode.SEAT_ALREADY_RESERVED);
        assertThat(result.getCode().getMessage()).isEqualTo(ExceptionCode.SEAT_ALREADY_RESERVED.getMessage());
    }

    // ------ 내부 메서드 -------

    private User createTestUser(Long id, String name, Long points) throws Exception {
        Constructor<User> constructor = User.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        User user = constructor.newInstance();

        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "name", name);
        ReflectionTestUtils.setField(user, "points", points);

        return user;
    }

    private Seat createTestSeat(
            Long id,
            String seatNumber,
            Long concertOptionId,
            Long price,
            SeatStatus status
    ) throws Exception {
        Constructor<Seat> constructor = Seat.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        Seat seat = constructor.newInstance();

        ReflectionTestUtils.setField(seat, "id", id);
        ReflectionTestUtils.setField(seat, "seatNumber", seatNumber);
        ReflectionTestUtils.setField(seat, "concertOptionId", concertOptionId);
        ReflectionTestUtils.setField(seat, "price", price);
        ReflectionTestUtils.setField(seat, "status", status);

        return seat;
    }

    private Reservation createTestReservation(
            Long id,
            Long userId,
            Long seatId,
            ReservationStatus status
    ) throws Exception {
        Constructor<Reservation> constructor = Reservation.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        Reservation reservation = new Reservation();

        ReflectionTestUtils.setField(reservation, "id", id);
        ReflectionTestUtils.setField(reservation, "userId", userId);
        ReflectionTestUtils.setField(reservation, "seatId", seatId);
        ReflectionTestUtils.setField(reservation, "status", status);

        return reservation;
    }
}
