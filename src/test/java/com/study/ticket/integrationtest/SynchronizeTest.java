package com.study.ticket.integrationtest;

import com.study.ticket.domain.dto.request.ReserveSeatRequest;
import com.study.ticket.domain.repository.ReservationRepository;
import com.study.ticket.domain.repository.SeatRepository;
import com.study.ticket.domain.repository.UserRepository;
import com.study.ticket.domain.service.TicketingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SynchronizeTest {

    @Autowired
    private TicketingService ticketingService;

    @Test
    @DisplayName("티켓팅 동시성 제어 테스트 : DB 비관적 락을 통한 제어")
    void ticketingSynchronizeTest() throws Exception {
        // given
        Long targetSeatId = 1L;
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            long userId = (long) i + 1;
            executorService.execute(() -> {
                try {
                    startLatch.await();

                    ticketingService.reserveSeat(new ReserveSeatRequest(targetSeatId, userId));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        doneLatch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(99);
    }

    @Test
    @DisplayName("동시에 100개의 요청이 올 때 캐시 락이 정상 작동하는지 검증")
    void concurrency_test() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long concertOptionId = 1L;

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    ticketingService.getAvailableSeats(concertOptionId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 종료 대기

        // 검증 로직: DB 로그나 레디스 호출 횟수 등을 통해
        // 실제 DB 조회가 최소화되었는지(1번만 수행되었는지) 확인
    }
}

