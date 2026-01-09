package com.study.ticket.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class WaitingQueueController {

    /**
     * SSE를 통한 실시간 순번 구독
     * [구현 가이드]
     * 1. SseEmitter 생성 및 registerQueue 호출.
     * 2. 반복 주기마다 getRank()를 조회해 클라이언트에 전송.
     * 3. isActive()가 true가 되면 '입장' 이벤트 전송 후 emitter.complete().
     */
    @GetMapping(value = "/stream/{optionId}/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long optionId, @PathVariable Long userId) { return null; }

    /**
     * 세션 연장 요청 API
     */
    @PostMapping("/extend/{optionId}/{userId}")
    public ResponseEntity<Void> extendSession(@PathVariable Long optionId, @PathVariable Long userId) { return null; }
}