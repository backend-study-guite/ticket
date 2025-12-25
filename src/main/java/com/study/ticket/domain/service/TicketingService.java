package com.study.ticket.domain.service;

import com.study.ticket.common.exception.CustomException;
import com.study.ticket.common.exception.ExceptionCode;
import com.study.ticket.domain.Entity.Reservation;
import com.study.ticket.domain.Entity.Seat;
import com.study.ticket.domain.Entity.User;
import com.study.ticket.domain.dto.request.ChargePointRequest;
import com.study.ticket.domain.dto.request.PaymentRequest;
import com.study.ticket.domain.dto.request.ReserveSeatRequest;
import com.study.ticket.domain.dto.response.ConcertListResponse;
import com.study.ticket.domain.dto.response.ConcertOptionListResponse;
import com.study.ticket.domain.dto.response.SeatListResponse;
import com.study.ticket.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.study.ticket.domain.repository.SeatRepository;
import com.study.ticket.domain.repository.ReservationRepository;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class TicketingService {

    // 1️⃣ 이 서비스가 다루는 대상(Repository)
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    /**
     * 좌석을 예약하는 메서드
     * 1. 동시성 제어하는 로직 구현
     * @param request
     * @return
     */
    @Transactional
    public String reserveSeat(ReserveSeatRequest request) {
        /*
         * [트랜잭션 시작]
         *
         * @Transactional 의미:
         * - 이 메서드 안에서 일어나는 DB 작업을
         *   하나의 "작업 단위"로 묶는다
         * - 중간에 예외가 터지면 → 전부 롤백
         */
        Long seatId = request.seatId();
        Long userId = request.userId();

        /*
         * [DTO에서 데이터 추출]
         *
         * - request는 단순한 데이터 묶음
         * - 실제 비즈니스 판단은 여기(Service)에서 한다
         */

        // 1️⃣ 좌석 조회 + 락
        Seat seat = seatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new CustomException(ExceptionCode.SEAT_NOT_FOUND));

        /*
         * [DB → Seat 엔티티]
         *
         * - seat 테이블에서 해당 row를 조회
         * - 동시에 PESSIMISTIC_WRITE 락을 건다
         *
         * 👉 의미:
         * - 다른 트랜잭션은 이 좌석 row에 접근 불가
         * - 동시 예약 방지
         */

        // 2️⃣ 좌석 상태 변경 요청
        seat.reserve();
        /*
         * [엔티티에게 메시지 전송]
         *
         * - "좌석아, 예약해"
         * - 상태 검증은 Seat 내부에서 수행
         *
         * Seat 내부 로직:
         * - AVAILABLE → RESERVED 만 허용
         * - 아니면 예외 발생
         *
         * 👉 이것이 '캡슐화'
         */

        // 3️⃣ 예약 엔티티 생성
        Reservation reservation = Reservation.create(userId, seat.getId());
        /*
         * [Reservation 객체 생성]
         *
         * - 상태: NOT_PAID
         * - 아직 결제 안 됨
         *
         * Reservation.create()를 쓰는 이유:
         * - 생성 규칙을 한 곳에 모으기 위해
         */

        // 4️⃣ DB 저장
        reservationRepository.save(reservation);
        /*
         * [INSERT INTO reservation]
         *
         * - JPA가 SQL을 대신 만들어 실행
         * - 이 시점에 reservationId 생성
         */
        return "예약 성공. reservationId=" + reservation.getId();
    }

    /**
     * 예약한 좌석을 결제하는 메서드
     * @param request
     * @return
     */
    @Transactional
    public String payment(PaymentRequest request) {

        /*
         * ===============================
         * [1] 이 메서드가 하는 일(목표)
         * ===============================
         * - "예약(reservation)"을 결제 처리한다.
         * - 결제는 3개의 데이터(엔티티)가 한 번에 함께 바뀌어야 한다.
         *
         *   1) User  : 포인트 차감
         *   2) Seat  : RESERVED -> PAID
         *   3) Reservation : NOT_PAID -> PAID
         *
         * - 셋 중 하나라도 실패하면 "결제 전체가 실패"해야 한다.
         *   → 그래서 @Transactional로 하나의 묶음(원자적 작업)으로 만든다.
         */
            Long userId = request.userId();
            Long reservationId = request.reservationId();
        /*
         * ===============================
         * [2] request(요청 DTO)에서 꺼낸 값들
         * ===============================
         * - request는 HTTP(JSON)로 들어온 데이터를 담은 "상자"다.
         *
         * 예: 결제 요청 JSON
         * {
         *   "userId": 10,
         *   "reservationId": 5
         * }
         *
         * 이 JSON이 @RequestBody에 의해 PaymentRequest 객체로 변환되었고,
         * 여기서 request.userId(), request.reservationId()로 꺼내는 것이다.
         */


        // 1) 예약(Reservation) 락 조회
            Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                    .orElseThrow(() -> new CustomException(ExceptionCode.RESERVATION_NOT_FOUND));
        /*
         * ===============================
         * [3] findByIdForUpdate + orElseThrow가 뭔데?
         * ===============================
         *
         * 1) reservationRepository.findByIdForUpdate(reservationId)
         *    - DB에서 reservationId에 해당하는 예약 row를 찾는다.
         *    - 동시에 "비관적 락(PESSIMISTIC_WRITE)"을 건다.
         *      → 같은 예약에 대해 다른 트랜잭션이 동시에 결제 못 하게 막음
         *
         * 2) 반환 타입이 Optional<Reservation> 인 이유
         *    - DB에 해당 id가 없을 수도 있으니 "있을 수도, 없을 수도"를 표현하기 위해 Optional을 쓴다.
         *
         * 3) Optional.orElseThrow(...) 의미
         *    - Optional 안에 값이 있으면 그 값을 꺼내서 반환
         *    - 값이 없으면(= DB에서 못 찾음) 예외를 던져서 메서드를 즉시 종료
         *
         * 즉, 아래 문장은 이렇게 읽으면 된다:
         * "예약을 DB에서 찾아와. 없으면 바로 예외로 끝내."
         */

        /*
         * ===============================
         * [4] IllegalArgumentException이 뭔데?
         * ===============================
         * - 자바에서 제공하는 기본 예외 중 하나다.
         * - 의미: "메서드에 전달된 인자(argument)가 잘못됐다"
         *
         * 여기서는:
         * - 존재하지 않는 reservationId를 줬다
         * - 예약자(userId)가 다르다
         * - 이미 결제된 예약이다
         * 같은 상황을 "잘못된 요청"으로 판단하고 예외를 던지는 것.
         *
         * 스프링에서는 예외를 던지면:
         * - 트랜잭션이면 자동으로 rollback 가능
         * - 컨트롤러까지 예외가 올라가면 보통 500/400 같은 에러 응답으로 처리됨
         *
         * (실무에서는 IllegalArgumentException 대신 커스텀 예외를 만들어 400/409로 내려주기도 함)
         */

        // 1-1) 예약자와 결제 요청자가 같은지 검증
        if (!reservation.getUserId().equals(userId)) {
            throw new CustomException(ExceptionCode.RESERVATION_USER_MISMATCH);
            }
        /*
         * ===============================
         * [5] equals()로 비교하는 이유
         * ===============================
         * - userId는 Long(객체)라서 == 비교가 위험할 수 있다.
         * - equals()는 값이 같은지 안전하게 비교하는 방법.
         */

        // 1-2) 이미 결제된 예약이면 막기(중복 결제 방지)
            if (!reservation.isNotPaid()) {
                throw new CustomException(ExceptionCode.RESERVATION_ALREADY_PAID);
            }

        // 2) 좌석(Seat) 락 조회
        Seat seat = seatRepository.findByIdForUpdate(reservation.getSeatId())
                .orElseThrow(() -> new CustomException(ExceptionCode.SEAT_NOT_FOUND));

        /*
        // 결제 처리 중 좌석 상태(SOLD/RESERVED)가 동시에 바뀌지 않도록 row lock을 건다.
         * ===============================
         * [7] 좌석을 왜 "예약에서 꺼내서" 찾지?
         * ===============================
         * - 결제는 reservationId로 들어온다.
         * - reservation 안에는 seatId가 저장돼 있다.
         * - 따라서 seatId를 reservation에서 꺼내서 Seat를 찾는 흐름이 된다.
         */

        // 3) 유저 락 조회 (동시 차감 방지)
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        /*
         * ===============================
         * [8] 유저에 락을 거는 이유(왜 필요해?)
         * ===============================
         * - 결제 시 포인트 차감이 발생한다.
         * - 같은 유저가 동시에 여러 결제를 하면 포인트가 꼬일 수 있다.
         * - 그래서 유저 row도 락으로 보호하는 것.
         */

        // 4) 좌석 가격 읽기
        Long price = seat.getPrice();

        /*
         * ===============================
         * [9] seat.getPrice()는 어떤 데이터?
         * ===============================
         * - Seat 엔티티의 price 필드(DB seat.price 컬럼) 값
         * - 결제할 때 필요한 금액
         */

        // 가격이 null이면 0으로 처리(방어 코드)
        if (price == null) price = 0L;
        /*
         * ===============================
         * [10] price가 null일 수도 있다는 건?
         * ===============================
         * - DB에 데이터가 제대로 안 들어갔거나(테스트 데이터 생성 시)
         * - 컬럼이 nullable인데 값을 안 넣었거나
         * - 임시 구현 단계일 수도 있다
         *
         * price = 0L로 처리하면 "무료 결제"처럼 동작한다.
         * (실무에서는 null이면 예외를 던지는 게 더 명확한 경우가 많음)
         */


        // 5) 포인트 충분한지 검증
        //포인트 검증이 이미 있어서 삭제

        /*
         * ===============================
         * [11] 여기까지는 "검증 단계"
         * ===============================
         * - 결제 가능한 상태인지 검사만 함
         * - 아직 DB 상태를 바꾸진 않음
         */

        // 6) 실제 상태 변경(도메인 메서드 호출)
        user.usePoint(price);   // User.points -= price
        seat.pay();             // Seat.status: RESERVED -> PAID
        reservation.pay();      // Reservation.status: NOT_PAID -> PAID

        /*
         * ===============================
         * [12] 여기서 "핵심 메시징"이 일어난다
         * ===============================
         * 서비스가 직접 status/points 값을 막 바꾸는게 아니라,
         * 엔티티에게 "행동"을 요청한다.
         *
         * - user에게: "포인트 써"
         * - seat에게: "결제 처리해"
         * - reservation에게: "결제 완료로 바꿔"
         *
         * 이게 객체지향적으로는:
         * - 엔티티가 자신의 상태를 스스로 책임지는 구조(캡슐화)
         */

        /*
         * ===============================
         * [13] DB에 UPDATE는 언제 되나?
         * ===============================
         * - JPA는 보통 "더티 체킹"으로 동작한다.
         * - 즉, 엔티티 필드가 바뀐 걸 감지해두었다가
         * - 트랜잭션이 끝나는 시점(메서드 종료 직전)에 UPDATE SQL을 날린다.
         *
         * 그래서 여기서 save()를 안 해도 반영될 수 있다.
         */

        return "결제 성공. reservationId=" + reservation.getId();

        /*
         * ===============================
         * [14] 결과 반환
         * ===============================
         * - 컨트롤러로 문자열이 돌아가고
         * - 컨트롤러는 ResponseEntity.ok(result)로 HTTP 응답을 만든다.
         */
    }


    /**
     * 포인트를 충전하는 메서드
     * @param request
     * @return
     */
    public Long chargePoint(ChargePointRequest request) {
        Long userId = request.userId();
        //충전할 유저 아이디
        Long amount = request.amount();
        //충전할 point 가격

        if(amount == null || amount <= 0) {
            throw new CustomException(ExceptionCode.ILLEGAL_POINTS);
        }
        //useId를 통해서 유저를 특정
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));
        //포인트 충전
        user.chargePoint(amount);
        //충전된 point를 get해서 전달
        return  user.getPoints();
    }
}
