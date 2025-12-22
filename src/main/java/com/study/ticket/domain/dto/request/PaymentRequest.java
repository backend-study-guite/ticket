package com.study.ticket.domain.dto.request;

public record PaymentRequest(Long userId, Long reservationId, Long usePoint) {
}
