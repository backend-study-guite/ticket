package com.study.ticket.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * ✅ 전역 예외 처리기(Global Exception Handler)
 *
 * - 컨트롤러에서 예외가 발생하면, 스프링이 예외를 위로 계속 던진다.
 * - 그 예외를 이 클래스가 "한 곳에서" 잡아서 HTTP 응답으로 바꿔준다.
 *
 * 👉 장점:
 * 1) 컨트롤러마다 try-catch를 안 써도 된다.
 * 2) 에러 응답 형식/상태코드를 프로젝트 전체에서 통일할 수 있다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * ✅ CustomException 전용 처리기
     *
     * - 서비스/도메인에서 throw new CustomException(ExceptionCode.XXX) 를 던지면
     * - 이 메서드가 그 예외를 잡아서
     *   "상태코드 + 메시지" 형태의 HTTP 응답으로 바꿔준다.
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> handleCustomException(CustomException e) {

        /**
         * 1) 예외 로그 남기기
         *
         * - 운영/디버깅에서 매우 중요:
         *   "어떤 코드의 에러가, 어떤 메시지로 터졌는지" 서버 로그에 기록됨
         *
         * - 왜 e.getMessage()를 찍나?
         *   CustomException 생성자에서 super(code.getMessage())를 넣어뒀다면
         *   e.getMessage()가 의미있는 메시지가 됨.
         *
         * - log.error vs log.warn?
         *   비즈니스 예외(예: 포인트 부족)는 사실 서버 장애가 아니라
         *   "예상 가능한 실패"라서 warn이 더 자연스러운 경우가 많음.
         *   (팀 규칙에 따라 error로 유지해도 됨)
         */
        log.warn("[CustomException] {} : {}", e.getCode().name(), e.getMessage());

        /**
         * 2) 예외 안에 들어있는 ExceptionCode를 꺼낸다.
         *
         * - ExceptionCode에는:
         *   - HttpStatus (code.getCode())
         *   - 사용자에게 보여줄 메시지 (code.getMessage())
         *   가 들어있음.
         */
        ExceptionCode code = e.getCode();

        /**
         * 3) HTTP 응답을 만든다.
         *
         * - status: ExceptionCode에 적힌 상태코드로 내려줌
         * - body  : ExceptionCode에 적힌 메시지로 내려줌
         *
         * 예)
         * - SEAT_NOT_FOUND -> 404 + "좌석을 찾을 수 없습니다."
         * - NOT_ENOUGH_POINTS -> 409 + "포인트가 부족합니다."
         */
        return ResponseEntity
                .status(code.getCode())    // HttpStatus
                .body(code.getMessage());  // 메시지(현재는 String)
    }

    /**
     * ✅ (추천) CustomException이 아닌 "예상치 못한 예외" 처리기
     *
     * - NullPointerException, DB 연결 오류, 버그 등
     *   우리가 의도하지 않은 예외도 서버에서 종종 발생한다.
     *
     * - 이게 없으면:
     *   스프링 기본 에러 응답(화이트라벨) 또는 스택트레이스가 노출될 수 있음(환경에 따라)
     *
     * - 그래서 안전하게 500 응답으로 통일하고,
     *   로그에만 상세 원인을 남긴다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpectedException(Exception e) {
        log.error("[UnexpectedException] {}", e.getMessage(), e);

        return ResponseEntity
                .status(500)
                .body("서버 내부 오류입니다.");
    }
}
