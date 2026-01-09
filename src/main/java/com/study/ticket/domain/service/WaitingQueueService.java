package com.study.ticket.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WaitingQueueService {

    /**
     * 특정 공연 회차의 대기열에 진입하는 메서드
     * [구현 가이드]
     * 1. 1인 1큐 제한: 'user:status:{userId}' 키가 존재하는지 확인.
     * 2. 타 공연 대기 중이면 ALREADY_IN_QUEUE 예외 발생.
     * 3. Redis SortedSet(queue:wait:{optionId})에 유저 등록 (Score=현재시간).
     * 4. 유저 상태 키 생성 및 TTL 설정 (대기 중 이탈 대비).
     */
    public void registerQueue(Long concertOptionId, Long userId) {}

    /**
     * 대기열 유저를 활성 세션으로 전환 (스케줄러 호출용)
     * [구현 가이드]
     * 1. Redisson 분산 락을 사용하여 공연별/회차별 중복 실행 방지.
     * 2. 가용 자원량만큼 대기열 상위 유저 추출.
     * 3. 활성 세션 키(queue:active:{optionId}:{userId}) 생성 (기본 10분 TTL).
     * 4. 대기열(ZSET) 및 대기 상태 키 정리.
     */
    public void promoteToActive(Long concertOptionId) {}

    /**
     * 활성 세션 시간 연장
     * [구현 가이드]
     * 1. isActive() 검증 후 세션 키의 TTL을 다시 10분으로 업데이트.
     * 2. (추가) 별도 키를 사용해 연장 횟수 제한 로직 구현.
     */
    public void extendActiveSession(Long concertOptionId, Long userId) {}

    /**
     * 활성 세션 보유 여부 확인
     */
    public boolean isActive(Long concertOptionId, Long userId) { return false; }

    /**
     * 대기 순번 조회 (1부터 시작하는 순위 반환)
     */
    public Long getRank(Long concertOptionId, Long userId) { return null; }
}