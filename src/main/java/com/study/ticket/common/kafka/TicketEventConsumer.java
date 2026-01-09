package com.study.ticket.common.kafka;

import com.study.ticket.domain.dto.event.ReservationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TicketEventConsumer {

    /**
     * 예약 완료 이벤트를 구독하여 후처리를 수행
     * [구현 로직]
     * 1. 알림 발송: 외부 API를 호출하여 유저에게 예약 성공 알림톡 전송.
     * 2. 만료 타이머 등록: Redis 또는 스케줄러를 통해 10분 내 미결제 시 자동 취소 로직 트리거.
     * 3. 멱등성 처리: 동일한 이벤트가 중복 전달되었을 때 중복 처리가 되지 않도록 보장.
     */
    @KafkaListener(topics = "reservation-topic", groupId = "ticket-group")
    public void handleReservationEvent(ReservationEvent event) {
        log.info("예약 후처리 시작: {}", event.reservationId());
    }
}