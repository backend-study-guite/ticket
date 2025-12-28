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

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

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
}

