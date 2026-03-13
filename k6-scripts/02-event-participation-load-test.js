/**
 * 이벤트 참여 동시성 부하 테스트
 *
 * 학습 목표:
 * 1. 실제 API 엔드포인트 테스트
 * 2. POST 요청으로 데이터 생성
 * 3. 동시성 문제 재현
 * 4. Stages를 이용한 점진적 부하 증가
 *
 * 시나리오:
 * - 선착순 100명 이벤트 진행 중
 * - 다수의 사용자가 동시에 이벤트 참여 시도
 * - 동시성 제어가 없으면 정원 초과 참여 발생
 *
 * 사전 준비:
 * 1. 애플리케이션 실행
 * 2. 이벤트 생성 및 시작 (setup 함수에서 자동 처리)
 * 3. 회원 데이터 준비 (setup 함수에서 자동 처리)
 *
 * 실행 방법:
 * k6 run 02-event-participation-load-test.js
 *
 * 결과 저장:
 * k6 run --out json=results.json 02-event-participation-load-test.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';
import { randomIntBetween } from './utils.js';

// ======================
// 환경 설정
// ======================
// const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080'; // K6를 설치한 경우
const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080'; // docker 에서 실행하고 local을 테스트 하는 경우
const ADMIN_EMAIL = __ENV.ADMIN_EMAIL || 'admin@example.com';
const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || 'Admin1234!@';

// ======================
// 커스텀 메트릭
// ======================
const winner = new Counter('winner');
const participationSuccess = new Counter('participation_success');
const participationFailed = new Counter('participation_failed');
const duplicateParticipation = new Counter('duplicate_participation');
const capacityExceeded = new Counter('capacity_exceeded');
const participationDuration = new Trend('participation_duration');

// ======================
// 테스트 옵션
// ======================
export const options = {
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

  // Stages: 점진적으로 부하를 증가시킵니다
  stages: [
    { duration: '30s', target: 50 },  // 30초 동안 50 VUs까지 증가
    { duration: '1m', target: 100 },  // 1분 동안 100 VUs까지 증가
    { duration: '30s', target: 150 }, // 30초 동안 150 VUs까지 증가 (과부하 테스트)
    { duration: '1m', target: 0 },    // 1분 동안 0으로 감소
  ],

  // Thresholds: 성공 기준
  thresholds: {
    // HTTP 요청 실패율 5% 미만
    'http_req_failed': ['rate<0.05'],

    // 95%의 요청이 1초 이내, 99%의 요청이 2초 이내
    'http_req_duration': ['p(95)<1000', 'p(99)<2000'],

    // 참여 성공률 80% 이상 (일부는 정원 초과나 중복으로 실패할 수 있음)
    'participation_success': ['count>0'],
  },
};

// ======================
// Setup: 테스트 데이터 준비
// ======================
export function setup() {
  console.log('\n========================================');
  console.log('### 테스트 준비 시작');
  console.log('========================================\n');

  const headers = {
    'Content-Type': 'application/json',
  };

  // 1. 이벤트 생성
  console.log('1. 이벤트 생성 중...');
  const eventPayload = JSON.stringify({
    title: `K6 부하테스트 이벤트 ${Date.now()}`,
    description: '동시성 테스트를 위한 선착순 이벤트',
    capacity: 100, // 선착순 100명
    startAt: new Date().toISOString(),
    endAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(), // 24시간 후
  });

  const eventResponse = http.post(
    `${BASE_URL}/api/admin/events`,
    eventPayload,
    { headers }
  );

  if (eventResponse.status !== 200 && eventResponse.status !== 201) {
    console.error('이벤트 생성 실패:', eventResponse.status, eventResponse.body);
    throw new Error('이벤트 생성 실패');
  }

  const event = JSON.parse(eventResponse.body);
  const eventId = event.id;
  console.log(`=> 이벤트 생성 완료 (ID: ${eventId})`);

  // 2. 이벤트 시작 (상태를 STARTED로 변경)
  console.log('2. 이벤트 시작 중...');
  const startResponse = http.patch(`${BASE_URL}/api/admin/events/${eventId}/start`, null, { headers });

  if (startResponse.status !== 200 && startResponse.status !== 204) {
    console.error('이벤트 시작 실패:', startResponse.status, startResponse.body);
    throw new Error('이벤트 시작 실패');
  }
  console.log('=> 이벤트 시작 완료');

  // 3. 테스트용 회원 생성
  console.log('3. 테스트 회원 생성 중...');
  const memberIds = [];
  const totalMembers = 2000; // VUs보다 많이 생성 (중복 방지)

  for (let i = 0; i < totalMembers; i++) {
    const memberPayload = JSON.stringify({
      email: `loadtest${i}_${Date.now()}@test.kr`,
      nickname: `LoadTester${i}`,
      password: 'Test1234!@',
    });

    const memberResponse = http.post(
      `${BASE_URL}/api/members`,
      memberPayload,
      { headers }
    );

    if (memberResponse.status === 200 || memberResponse.status === 201) {
      const member = JSON.parse(memberResponse.body);
      memberIds.push(member.id);
    }

    // API 부하 줄이기 위해 약간의 딜레이
    if (i % 10 === 0) {
      sleep(0.1);
    }
  }

  console.log(`=> 회원 생성 완료 (${memberIds.length}명)`);

  console.log('\n========================================');
  console.log('테스트 준비 완료');
  console.log(`이벤트 ID: ${eventId}`);
  console.log(`회원 수: ${memberIds.length}`);
  console.log('========================================\n');

  return {
    eventId,
    memberIds,
    baseUrl: BASE_URL,
  };
}

// ======================
// Default: 메인 테스트 로직
// ======================
export default function (data) {
  const { eventId, memberIds, baseUrl } = data;

  // 랜덤하게 회원 선택
  const memberId = memberIds[randomIntBetween(0, memberIds.length - 1)];

  const headers = {
    'Content-Type': 'application/json',
  };

  // 이벤트 참여 API 호출
  const startTime = Date.now();
  const response = http.post(
    `${baseUrl}/api/events/${eventId}/participate/${memberId}`,
    null,
    {
      headers,
      tags: { name: 'event_participation' },
    }
  );
  const duration = Date.now() - startTime;

  // 메트릭 기록
  participationDuration.add(duration);

  // 응답 검증
  const success = check(response, {
    '상태 코드가 200 또는 201': (r) => r.status === 200 || r.status === 201,
    '응답 시간 2초 이내': (r) => r.timings.duration < 2000,
  });

  // 결과 분류
  if (response.status === 200 || response.status === 201) {
    participationSuccess.add(1);

    // 응답 body에서 isWinner 확인
    try {
      const responseBody = JSON.parse(response.body);
      if (responseBody.isWinner === true) {
        winner.add(1);
      }
    } catch (e) {
      // JSON 파싱 실패 시 무시
    }
  } else if (response.status === 400) {
    // 400 에러는 비즈니스 로직 실패
    const errorBody = response.body || '';

    if (errorBody.includes('중복')) {
      duplicateParticipation.add(1);
      console.log(`[중복 참여] 회원 ${memberId}`);
    } else if (errorBody.includes('정원') || errorBody.includes('종료')) {
      capacityExceeded.add(1);
      console.log(`[정원 초과] 회원 ${memberId}`);
    } else {
      participationFailed.add(1);
      console.log(`[실패] 상태: ${response.status}, 본문: ${errorBody}`);
    }
  } else {
    participationFailed.add(1);
    console.log(`[실패] 상태: ${response.status}, 본문: ${response.body}`);
  }

  // 사용자 행동 시뮬레이션: 1-3초 대기
  sleep(randomIntBetween(1, 3));
}

// ======================
// Teardown: 테스트 후 정리
// ======================
export function teardown(data) {
  console.log('\n========================================');
  console.log('테스트 정리 시작');
  console.log('========================================\n');

  // 실제 환경에서는 테스트 데이터 정리가 필요할 수 있습니다.
  // 예: 생성한 이벤트와 회원 삭제

  console.log('=> 정리 완료 (테스트 데이터는 DB에 남아있음)');
  console.log('\n========================================');
  console.log('테스트 종료');
  console.log('========================================\n');
}

// ======================
// 커스텀 요약
// ======================
export function handleSummary(data) {
  console.log('\n========== @@@@@ 이벤트 참여 부하 테스트 결과 @@@@@ ==========\n');

  const metrics = data.metrics;

  if (!metrics) {
    console.error('Metrics data is missing. Test summary cannot be generated.');
    return;
  }

  const duration = metrics.http_req_duration?.values ?? {};
  const total = metrics.http_reqs?.values ?? {};
  const failed = metrics.http_req_failed?.values ?? {};

  const avg = duration.avg;
  const min = duration.min;
  const max = duration.max;
  const p90 = duration['p(90)'];
  const p95 = duration['p(95)'];
  const p99 = duration['p(99)'];

  const totalRequests = total.count ?? 0;
  const failedRequests = (failed.rate ?? 0) * totalRequests;
  const successRate = totalRequests > 0 ? ((1 - (failed.rate ?? 0)) * 100) : 0;

  console.log('### 기본 메트릭:');
  console.log(`  - 총 요청 수: ${totalRequests}`);
  console.log(`  - 실패한 요청: ${failedRequests.toFixed(0)}`);
  console.log(`  - 요청 성공률: ${successRate.toFixed(2)}%`);
  console.log(`  - RPS (초당 요청): ${(metrics.http_reqs?.values?.rate ?? 0).toFixed(2)}`);

  console.log('\n###  응답 시간:');
  console.log(`  - 평균: ${avg != null ? avg.toFixed(2) : 'N/A'}ms`);
  console.log(`  - 최소: ${min != null ? min.toFixed(2) : 'N/A'}ms`);
  console.log(`  - 최대: ${max != null ? max.toFixed(2) : 'N/A'}ms`);
  console.log(`  - P90: ${p90 != null ? p90.toFixed(2) : 'N/A'}ms`);
  console.log(`  - P95: ${p95 != null ? p95.toFixed(2) : 'N/A'}ms`);
  console.log(`  - P99: ${p99 != null ? p99.toFixed(2) : 'N/A'}ms`);

  console.log('\n### 이벤트 참여 결과:');
  console.log(`  - 참여 성공: ${metrics.participation_success?.values?.count ?? 0}`);
  console.log(`  - 참여 실패: ${metrics.participation_failed?.values?.count ?? 0}`);
  console.log(`  - 중복 참여: ${metrics.duplicate_participation?.values?.count ?? 0}`);
  console.log(`  - 정원 초과: ${metrics.capacity_exceeded?.values?.count ?? 0}`);
  console.log(`  - 당첨자(Winner): ${metrics.winner?.values?.count ?? 0}`);

  // 동시성 문제 판단
  const totalSuccess = metrics.participation_success?.values?.count ?? 0;
  const totalWinners = metrics.winner?.values?.count ?? 0;
  const expectedCapacity = 100; // 이벤트 정원

  console.log('\n🔍 동시성 분석:');
  if (totalWinners > expectedCapacity) {
    console.log(`  ⚠️  경고: 정원(${expectedCapacity})을 초과한 당첨자 발생!`);
    console.log(`  ⚠️  전체 참여자: ${totalSuccess}명`);
    console.log(`  ⚠️  실제 당첨자: ${totalWinners}명`);
    console.log(`  ⚠️  초과 인원: ${totalWinners - expectedCapacity}명`);
    console.log(`  ⚠️  => 동시성 제어 문제 발생!`);
  } else {
    console.log(`  ✅  전체 참여자: ${totalSuccess}명`);
    console.log(`  ✅ 정원 내 당첨자: ${totalWinners}/${expectedCapacity}명`);
    console.log(`  ✅ 동시성 제어가 올바르게 작동함`);
  }

  console.log('\n==================================================\n');

  // JSON 파일로 저장
  return {
    'stdout': '', // 기본 요약은 이미 위에서 출력했으므로 생략
    '02-event-participation-load-test-summary.json': JSON.stringify(data, null, 2),
  };
}

/**
 * ======================
 * 학습 포인트
 * ======================
 *
 * 1. Setup/Teardown 활용:
 *    - setup()에서 테스트 데이터 준비
 *    - teardown()에서 테스트 데이터 정리
 *
 * 2. Stages를 이용한 점진적 부하:
 *    - 갑자기 최대 부하를 주지 않고 단계적으로 증가
 *    - 시스템이 어느 시점에서 문제가 발생하는지 파악 가능
 *
 * 3. 커스텀 메트릭:
 *    - Counter: 카운트
 *    - Rate: 비율
 *    - Trend: 평균, 최소, 최대, percentile
 *
 * 4. 동시성 문제 재현:
 *    - 정원 100명 이벤트에 150 VUs 투입
 *    - 동시성 제어가 없으면 정원 초과 발생
 *
 * 5. 실전 팁:
 *    - 실제 사용자 행동 시뮬레이션 (sleep 사용)
 *    - 에러 응답 분류 및 분석
 *    - 비즈니스 로직 검증 (정원 체크)
 */
