package com.study.ticket.domain.dto.event;

import java.time.LocalDateTime;

/**
 * 좌석 예약 완료 이벤트
 * @param reservationId 생성된 예약 ID
 * @param userId 예약자 ID
 * @param seatId 예약된 좌석 ID
 * @param createdAt 이벤트 발생 시간
 */
public record ReservationEvent(
        Long reservationId,
        Long userId,
        Long seatId,
        LocalDateTime createdAt
) {}