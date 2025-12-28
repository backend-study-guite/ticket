package com.study.ticket;

import com.study.ticket.domain.Entity.Seat;
import com.study.ticket.domain.Entity.User;
import com.study.ticket.domain.dto.request.ReserveSeatRequest;
import com.study.ticket.domain.repository.SeatRepository;
import com.study.ticket.domain.repository.UserRepository;
import com.study.ticket.domain.service.TicketingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TicketingServiceConcurrencyTest {

    @Autowired
    private TicketingService ticketingService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    private Long seatId;
    private List<Long> userIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Seat seat = Seat.create(1L, "A1");
        seatRepository.save(seat);
        seatId = seat.getId();

        for (int i = 0; i < 100; i++) {
            User user = User.create("user-" + i);
            userRepository.save(user);
            userIds.add(user.getId());
        }
    }


    @Test
    void 동시에_100명이_좌석을_예약하면_1명만_성공한다() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Long userId = userIds.get(i);

            results.add(
                    executorService.submit(() -> {
                        try {
                            ticketingService.reserveSeat(
                                    new ReserveSeatRequest(seatId, userId)
                            );
                            return true; // 성공
                        } catch (Exception e) {
                            return false; // 실패
                        } finally {
                            latch.countDown();
                        }
                    })
            );
        }

        latch.await();
        executorService.shutdown();

        long successCount = results.stream()
                .filter(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();

        assertThat(successCount).isEqualTo(1);
    }
}
