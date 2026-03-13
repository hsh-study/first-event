/**
 * 스파이크 테스트 (Spike Test)
 *
 * 테스트 목적:
 * 1. 갑작스러운 트래픽 급증에 대한 시스템 대응 능력 테스트
 * 2. 오토스케일링, 서킷 브레이커 등의 동작 확인
 * 3. 급격한 부하 증가 시 에러율 변화 관찰
 *
 * 실제 시나리오:
 * - 플래시 세일 오픈
 * - 선착순 이벤트 시작
 * - 마케팅 이메일 발송 직후
 * - SNS에서 바이럴 시작
 *
 * 특징:
 * - 매우 짧은 시간에 트래픽이 급증
 * - 일반적인 부하 테스트보다 훨씬 가혹한 조건
 * - 시스템이 얼마나 빠르게 대응하는지 확인
 *
 * 실행 방법:
 * k6 run 03-spike-test.js
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

// ======================
// 커스텀 메트릭
// ======================
const spikeRequests = new Counter('spike_requests');
const spikeErrors = new Counter('spike_errors');
const spikeSuccessRate = new Rate('spike_success_rate');
const requestDurationDuringSpike = new Trend('request_duration_during_spike');

// ======================
// 테스트 옵션
// ======================
export const options = {
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

  // 스파이크 테스트 시나리오
  stages: [
    // 1단계: 정상 상태 (baseline)
    { duration: '30s', target: 100 },   // 30초간 100 VUs로 정상 상태 확인

    // 2단계: 스파이크! (급증)
    { duration: '10s', target: 2000 },  // 10초 만에 2000 VUs로 급증!!!!

    // 3단계: 스파이크 유지
    { duration: '1m', target: 2000 },   // 1분간 고부하 유지

    // 4단계: 급감
    { duration: '10s', target: 100 },   // 10초 만에 100 VUs로 급감

    // 5단계: 회복 확인
    { duration: '1m', target: 100 },    // 1분간 정상 상태로 회복되는지 확인

    // 6단계: 종료
    { duration: '30s', target: 0 },     // 30초간 0으로 감소
  ],

  // Thresholds
  thresholds: {
    // 스파이크 동안 에러율 10% 미만
    'http_req_failed': ['rate<0.1'],

    // 99%의 요청이 3초 이내 (스파이크 상황이므로 여유있게)
    'http_req_duration': ['p(99)<3000'],

    // 스파이크 성공률 90% 이상
    'spike_success_rate': ['rate>0.9'],
  },
};

// ======================
// Setup
// ======================
export function setup() {
  console.log('\n========================================');
  console.log('### 스파이크 테스트 준비');
  console.log('========================================\n');

  console.log('시나리오: 선착순 이벤트 오픈 직후 트래픽 급증');
  console.log('- 정상: 100 VUs');
  console.log('- 스파이크: 2000 VUs (20배 증가!)');
  console.log('- 지속: 1분');

  console.log('\n주의: 이 테스트는 시스템에 높은 부하를 줍니다.');
  console.log('      로컬 개발 환경에서는 리소스 부족으로 실패할 수 있습니다.\n');

  const headers = { 'Content-Type': 'application/json' };

  // 테스트용 이벤트 생성
  console.log('테스트 이벤트 생성 중...');
  const eventPayload = JSON.stringify({
    title: `스파이크 테스트 이벤트 ${Date.now()}`,
    description: '급격한 트래픽 증가 테스트',
    capacity: 1000,
    startAt: new Date().toISOString(),
    endAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
  });

  const eventResponse = http.post(
    `${BASE_URL}/api/admin/events`,
    eventPayload,
    { headers }
  );

  let eventId = null;
  if (eventResponse.status === 200 || eventResponse.status === 201) {
    const event = JSON.parse(eventResponse.body);
    eventId = event.id;
    console.log(`=> 이벤트 생성 완료 (ID: ${eventId})`);
  } else {
    console.log('⚠️  이벤트 생성 실패');
  }

  // 이벤트 시작
  console.log('테스트 이벤트 시작 중...');
  const eventStartResponse = http.patch(
      `${BASE_URL}/api/admin/events/${eventId}/start`,
      null,
      { headers }
  );

  if (eventStartResponse.status === 200 || eventStartResponse.status === 201) {
    console.log(`=> 이벤트 시작 완료 (ID: ${eventId})`);
  } else {
    console.log('⚠️  이벤트 시작 실패');
  }

  // 테스트용 회원 생성 (적은 수로 충분)
  console.log('테스트 회원 생성 중...');
  const memberIds = [];
  for (let i = 0; i < 2000; i++) {
    const memberPayload = JSON.stringify({
      email: `spike${i}_${Date.now()}@example.com`,
      nickname: `Spiker${i}`,
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
  }

  console.log(`=> 회원 생성 완료 (${memberIds.length}명)`);
  console.log('\n========================================');
  console.log('준비 완료 - 스파이크 테스트 시작!\n');

  return {
    eventId,
    memberIds,
    baseUrl: BASE_URL,
  };
}

// ======================
// Default: 메인 테스트
// ======================
export default function (data) {
  const { eventId, memberIds, baseUrl } = data;
  const headers = { 'Content-Type': 'application/json' };

  // 현재 VU 수와 iteration 로깅 (스파이크 모니터링)
  const currentVu = __VU;
  const currentIter = __ITER;

  // 스파이크 구간인지 확인 (VU > 1000이면 스파이크 구간)
  const isSpike = currentVu > 1000;

  if (isSpike && currentIter === 0) {
    console.log(`### [SPIKE] VU ${currentVu} 활성화!`);
  }

  // 요청 전송
  const startTime = Date.now();

  let response;
  if (eventId && memberIds.length > 0) {
    // 이벤트 참여 API 호출
    const memberId = memberIds[randomIntBetween(0, memberIds.length - 1)];
    response = http.post(
      `${baseUrl}/api/events/${eventId}/participate/${memberId}`,
      null,
      {
        headers,
        tags: {
          name: 'spike_participation',
          spike: isSpike ? 'true' : 'false',
        },
      }
    );
  } else {
    // 대체 엔드포인트 (index)
    response = http.get(baseUrl, {
      tags: {
        name: 'spike_index',
        spike: isSpike ? 'true' : 'false',
      },
    });
  }

  const duration = Date.now() - startTime;

  // 메트릭 기록
  spikeRequests.add(1);

  if (isSpike) {
    requestDurationDuringSpike.add(duration);
  }

  // 응답 검증
  const success = check(response, {
    '상태 코드가 2xx 또는 4xx': (r) =>
      r.status >= 200 && r.status < 500,
    '응답 시간 3초 이내': (r) => r.timings.duration < 3000,
  });

  spikeSuccessRate.add(success);

  if (!success) {
    spikeErrors.add(1);
    if (isSpike) {
      console.error(`❌ [SPIKE ERROR] VU ${currentVu}: ${response.status} - ${duration}ms`);
    }
  }

  // 스파이크 상황에서는 대기 시간 짧게
  if (isSpike) {
    sleep(randomIntBetween(0, 1)); // 0-1초
  } else {
    sleep(randomIntBetween(1, 2)); // 1-2초
  }
}

// ======================
// Teardown
// ======================
export function teardown(data) {
  console.log('\n========================================');
  console.log('스파이크 테스트 종료');
  console.log('========================================\n');
}

// ======================
// 커스텀 요약
// ======================
export function handleSummary(data) {
  console.log('\n========== @@@@@ 스파이크 테스트 결과 @@@@@ ==========\n');

  const metrics = data.metrics;

  console.log('### 전체 메트릭:');
  console.log(`  - 총 요청 수: ${metrics.http_reqs?.values?.count ?? 0}`);
  console.log(`  - 실패 요청 수: ${metrics.http_req_failed?.values?.passes ?? 0}`);
  console.log(`  - 에러율: ${((metrics.http_req_failed?.values?.rate ?? 0) * 100).toFixed(2)}%`);
  console.log(`  - RPS (초당 요청): ${(metrics.http_reqs?.values?.rate ?? 0).toFixed(2)}`);

  console.log('\n### 응답 시간 (전체):');
  const duration = metrics.http_req_duration?.values ?? {};
  console.log(`  - 평균: ${duration.avg != null ? duration.avg.toFixed(2) : 'N/A'}ms`);
  console.log(`  - P50 (중앙값): ${duration.med != null ? duration.med.toFixed(2) : 'N/A'}ms`);
  console.log(`  - P90: ${duration['p(90)'] != null ? duration['p(90)'].toFixed(2) : 'N/A'}ms`);
  console.log(`  - P95: ${duration['p(95)'] != null ? duration['p(95)'].toFixed(2) : 'N/A'}ms`);
  console.log(`  - P99: ${duration['p(99)'] != null ? duration['p(99)'].toFixed(2) : 'N/A'}ms`);
  console.log(`  - 최대: ${duration.max != null ? duration.max.toFixed(2) : 'N/A'}ms`);

  if (metrics.request_duration_during_spike) {
    console.log('\n### 응답 시간 (스파이크 구간만):');
    const spikeDuration = metrics.request_duration_during_spike.values;
    console.log(`  - 평균: ${spikeDuration.avg != null ? spikeDuration.avg.toFixed(2) : 'N/A'}ms`);
    console.log(`  - P95: ${spikeDuration['p(95)'] != null ? spikeDuration['p(95)'].toFixed(2) : 'N/A'}ms`);
    console.log(`  - P99: ${spikeDuration['p(99)'] != null ? spikeDuration['p(99)'].toFixed(2) : 'N/A'}ms`);
  }

  console.log('\n### 스파이크 분석:');
  console.log(`  - 스파이크 요청 수: ${metrics.spike_requests?.values?.count ?? 0}`);
  console.log(`  - 스파이크 에러 수: ${metrics.spike_errors?.values?.count ?? 0}`);
  console.log(`  - 스파이크 성공률: ${((metrics.spike_success_rate?.values?.rate ?? 0) * 100).toFixed(2)}%`);

  // 판단
  console.log('\n### 결과 분석:');
  const errorRate = metrics.http_req_failed?.values?.rate ?? 0;
  const p99 = metrics.http_req_duration?.values?.['p(99)'] ?? 0;

  if (errorRate < 0.01 && p99 < 1000) {
    console.log('  ✅ 우수: 스파이크 상황에서도 안정적으로 동작합니다.');
  } else if (errorRate < 0.05 && p99 < 2000) {
    console.log('  ⚠️  양호: 스파이크 시 약간의 성능 저하가 있지만 허용 범위입니다.');
  } else if (errorRate < 0.1 && p99 < 3000) {
    console.log('  ⚠️  주의: 스파이크 시 성능 저하가 눈에 띕니다. 최적화 권장.');
  } else {
    console.log('  🔴 경고: 스파이크 상황을 감당하기 어렵습니다.');
    console.log('      - 오토스케일링 설정 검토');
    console.log('      - 캐싱 전략 도입');
    console.log('      - DB 커넥션 풀 크기 증가');
    console.log('      - Rate Limiting 도입 검토');
  }

  console.log('\n### 권장 사항:');
  console.log('  1. CloudWatch/Grafana로 실시간 모니터링');
  console.log('  2. 오토스케일링 정책 점검');
  console.log('  3. DB 커넥션 풀 크기 검토');
  console.log('  4. 캐시 히트율 확인');
  console.log('  5. 서킷 브레이커 패턴 적용 검토');

  console.log('\n================================================\n');

  return {
    '03-spike-test-summary.json': JSON.stringify(data, null, 2),
  };
}

/**
 * ======================
 * 학습 포인트
 * ======================
 *
 * 1. 스파이크 테스트의 목적:
 *    - 갑작스러운 트래픽 급증에 대한 대응 능력 확인
 *    - 오토스케일링이 빠르게 작동하는지 확인
 *    - 급증 후 시스템이 정상으로 회복되는지 확인
 *
 * 2. 스파이크 vs 부하 테스트:
 *    - 부하: 점진적으로 증가
 *    - 스파이크: 급격하게 증가 (10초 안에 20배!)
 *
 * 3. 실제 시나리오:
 *    - 선착순 이벤트 오픈
 *    - 플래시 세일 시작
 *    - 바이럴 마케팅 효과
 *    - TV 광고 방영 직후
 *
 * 4. 주의사항:
 *    - 프로덕션 환경에서는 절대 금지!
 *    - 로컬 환경에서는 리소스 부족 가능
 *    - 사전에 인프라 팀과 협의 필요
 *
 * 5. 대응 방안:
 *    - 오토스케일링 설정
 *    - CDN 활용
 *    - 캐싱 전략
 *    - Rate Limiting
 *    - 큐(Queue) 시스템
 */
