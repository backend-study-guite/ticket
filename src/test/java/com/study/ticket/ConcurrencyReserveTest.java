package com.study.ticket;

import com.study.ticket.domain.Entity.Seat;
import com.study.ticket.domain.Entity.User;
import com.study.ticket.domain.dto.request.ReserveSeatRequest;
import com.study.ticket.domain.repository.SeatRepository;
import com.study.ticket.domain.repository.UserRepository;
import com.study.ticket.domain.service.TicketingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ConcurrencyReserveTest {

    @Autowired
    TicketingService ticketingService;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void reserveSeat_100Users_1Seat_onlyOneSuccess() throws Exception {

        // =========================
        // 1) GIVEN: 테스트 데이터(seed) 준비
        // =========================

        // 좌석 1개 생성
        // - Seat.create(...)가 AVAILABLE 상태로 만들어준다고 가정
        Seat seat = Seat.create(1L, "A1");

        // saveAndFlush:
        // - save만 하면 INSERT SQL이 즉시 안 나갈 수 있음(커밋 때 나갈 수도)
        // - flush로 지금 바로 DB에 INSERT를 확정시켜서
        //   멀티스레드가 동시에 조회할 때 "좌석이 DB에 존재"하도록 보장
        seatRepository.saveAndFlush(seat);

        // 동시에 시도할 요청 수 (유저 100명)
        int threadCount = 100;

        // 실제로 동시에 돌릴 워커 스레드 수
        // - 100개 요청을 "최대 32개씩" 병렬로 처리
        ExecutorService executor = Executors.newFixedThreadPool(32);

        // CountDownLatch:
        // - 100개의 작업이 전부 끝날 때까지 테스트 스레드가 기다리도록 만든다
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 성공/실패 카운트: 멀티스레드 환경에서도 안전하게 증가시키기 위해 AtomicInteger 사용
        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        // 유저 100명 생성 (각 스레드가 사용할 userId를 마련)
        List<User> users = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            User u = User.create("u" + i);
            users.add(userRepository.save(u));
        }

        // 유저도 flush로 DB에 확정
        userRepository.flush();

        // =========================
        // 2) WHEN: 100명이 동시에 reserveSeat 호출
        // =========================

        // 로그를 보기 좋게 하기 위해 시작 신호 출력
        System.out.println("\n========== CONCURRENCY TEST START ==========");
        System.out.println("SeatId=" + seat.getId() + ", Users=" + threadCount);
        System.out.println("===========================================\n");

        // users 각각에 대해 "예약 시도 작업"을 스레드풀에 제출
        for (User u : users) {
            executor.submit(() -> {
                String threadName = Thread.currentThread().getName();
                long startMs = System.currentTimeMillis();

                try {
                    // (A) 이 시점: 스레드가 서비스 호출을 시작한다
                    // - 여기서부터 트랜잭션이 열리고
                    // - 내부에서 SELECT ... FOR UPDATE 로 락을 걸려고 시도한다
                    System.out.println(now() + " [" + threadName + "] START reserveSeat userId=" + u.getId());

                    // 실제 예약 시도
                    ticketingService.reserveSeat(new ReserveSeatRequest(seat.getId(), u.getId()));

                    // 예외 없이 끝났으면 성공
                    success.incrementAndGet();

                    long took = System.currentTimeMillis() - startMs;
                    System.out.println(now() + " [" + threadName + "] ✅ SUCCESS (took " + took + "ms) userId=" + u.getId());

                } catch (Exception e) {
                    // 예외가 나면 실패
                    fail.incrementAndGet();

                    long took = System.currentTimeMillis() - startMs;
                    System.out.println(now() + " [" + threadName + "] ❌ FAIL (took " + took + "ms) userId=" + u.getId()
                            + " / ex=" + e.getClass().getSimpleName()
                            + (e.getMessage() != null ? " / msg=" + e.getMessage() : ""));

                } finally {
                    // 이 작업이 끝났다는 신호
                    latch.countDown();
                }
            });
        }

        // 100개 작업이 전부 끝날 때까지 기다린다
        latch.await();

        // 스레드 풀 종료 (새 작업 제출 금지 + 종료 준비)
        executor.shutdown();

        // =========================
        // 3) THEN: 결과 검증
        // =========================

        System.out.println("\n========== CONCURRENCY TEST RESULT ==========");
        System.out.println("SUCCESS=" + success.get());
        System.out.println("FAIL=" + fail.get());
        System.out.println("============================================\n");

        // 기대값: 한 명만 성공, 나머지 실패
        assertThat(success.get()).isEqualTo(1);
        assertThat(fail.get()).isEqualTo(99);
    }

    private static String now() {
        return LocalTime.now().toString();
    }
}
