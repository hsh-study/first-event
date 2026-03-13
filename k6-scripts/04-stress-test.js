/**
 * 스트레스 테스트 (Stress Test)
 *
 * 테스트 목적:
 * 1. 시스템의 한계점 찾기
 * 2. 어느 시점에서 시스템이 실패하는지 확인
 * 3. 병목 지점 파악
 * 4. 장애 발생 시 시스템 동작 관찰
 *
 * 특징:
 * - 부하를 계속 증가시켜 시스템이 견딜 수 있는 최대치 확인
 * - 실패 지점에서 어떤 일이 발생하는지 관찰
 * - 시스템이 얼마나 우아하게(gracefully) 실패하는지 확인
 *
 * 실제 사용 사례:
 * - 용량 계획 수립
 * - 하드웨어 스펙 결정
 * - 오토스케일링 임계값 설정
 * - SLA 정의
 *
 * 주의사항:
 * ⚠️  이 테스트는 시스템을 의도적으로 실패시킵니다!
 * ⚠️  프로덕션 환경에서는 절대 실행하지 마세요!
 * ⚠️  테스트 환경에서도 사전에 팀에 공지하세요!
 *
 * 실행 방법:
 * k6 run 04-stress-test.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend, Gauge } from 'k6/metrics';
import { randomIntBetween } from './utils.js';

// ======================
// 환경 설정
// ======================
// const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080'; // K6를 설치한 경우
const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080'; // docker 에서 실행하고 local을 테스트 하는 경우

// ======================
// 커스텀 메트릭
// ======================
const stressLevelGauge = new Gauge('stress_level'); // 현재 스트레스 레벨
const breakingPoint = new Counter('breaking_point'); // 실패 시작 지점
const recoveryTime = new Trend('recovery_time'); // 회복 시간
const errorsByStage = new Counter('errors_by_stage'); // 단계별 에러

// ======================
// 테스트 옵션
// ======================
export const options = {
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

  // 스트레스 테스트 시나리오
  // 단계적으로 부하를 증가시켜 시스템 한계를 찾습니다
  stages: [
    // Stage 1: 워밍업 (Warm-up)
    { duration: '2m', target: 100 },    // Level 1: 기본 부하

    // Stage 2: 정상 부하
    { duration: '3m', target: 200 },    // Level 2: 예상 최대 부하

    // Stage 3: 스트레스 시작
    { duration: '3m', target: 400 },    // Level 3: 예상의 2배

    // Stage 4: 높은 스트레스
    { duration: '3m', target: 600 },    // Level 4: 예상의 3배

    // Stage 5: 극심한 스트레스
    { duration: '3m', target: 800 },    // Level 5: 예상의 4배

    // Stage 6: 한계 테스트
    { duration: '3m', target: 1000 },   // Level 6: 예상의 5배

    // Stage 7: 회복 테스트 (Recovery)
    { duration: '5m', target: 100 },    // 빠르게 정상 부하로 복귀

    // Stage 8: 종료
    { duration: '2m', target: 0 },
  ],

  // Thresholds - 스트레스 테스트이므로 여유롭게 설정
  thresholds: {
    // 전체적으로 20% 이하의 에러율
    'http_req_failed': ['rate<0.2'],

    // P99가 5초 이내
    'http_req_duration': ['p(99)<5000'],
  },
};

// ======================
// Setup
// ======================
export function setup() {
  console.log('\n========================================');
  console.log('### 스트레스 테스트 준비');
  console.log('========================================\n');

  console.log('⚠️  경고: 이 테스트는 시스템을 한계까지 밀어붙입니다!');
  console.log('');
  console.log('목적:');
  console.log('  - 시스템이 견딜 수 있는 최대 부하 확인');
  console.log('  - 실패 지점에서의 동작 관찰');
  console.log('  - 회복 능력 확인');
  console.log('');
  console.log('테스트 구간:');
  console.log('  Level 1: 100 VUs  (워밍업)');
  console.log('  Level 2: 200 VUs  (정상)');
  console.log('  Level 3: 400 VUs  (스트레스)');
  console.log('  Level 4: 600 VUs  (높은 스트레스)');
  console.log('  Level 5: 800 VUs  (극심한 스트레스)');
  console.log('  Level 6: 1000 VUs (한계 테스트)');
  console.log('  회복:    100 VUs  (회복 테스트)');
  console.log('');
  console.log('총 소요 시간: 약 24분');
  console.log('');

  const headers = { 'Content-Type': 'application/json' };

  // 테스트용 이벤트 생성
  console.log('테스트 이벤트 생성 중...');
  const eventPayload = JSON.stringify({
    title: `스트레스 테스트 이벤트 ${Date.now()}`,
    description: '시스템 한계 테스트',
    capacity: 5000, // 충분히 큰 정원
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
    console.log('⚠️  이벤트 생성 실패 - 기본 엔드포인트 테스트로 진행');
  }

  console.log('\n========================================');
  console.log('준비 완료 - 스트레스 테스트 시작!\n');

  return {
    eventId,
    baseUrl: BASE_URL,
    testStartTime: Date.now(),
  };
}

// ======================
// Default: 메인 테스트
// ======================
export default function (data) {
  const { eventId, baseUrl, testStartTime } = data;
  const headers = { 'Content-Type': 'application/json' };

  // 현재 스트레스 레벨 계산
  const currentVu = __VU;
  let stressLevel;
  if (currentVu <= 100) stressLevel = 1;
  else if (currentVu <= 200) stressLevel = 2;
  else if (currentVu <= 400) stressLevel = 3;
  else if (currentVu <= 600) stressLevel = 4;
  else if (currentVu <= 800) stressLevel = 5;
  else stressLevel = 6;

  stressLevelGauge.add(stressLevel);

  // 요청 전송
  const startTime = Date.now();

  const response = http.get(`${baseUrl}/events/${eventId}`, {
    headers,
    tags: {
      name: 'stress_test',
      stress_level: `level_${stressLevel}`,
    },
  });

  const duration = Date.now() - startTime;

  // 응답 검증
  const success = check(response, {
    '상태 코드가 성공 (2xx-4xx)': (r) => r.status >= 200 && r.status < 500,
    '응답 시간 10초 이내': (r) => r.timings.duration < 10000,
  });

  // 실패 감지
  if (!success) {
    errorsByStage.add(1, { stage: `level_${stressLevel}` });

    // Breaking point 기록 (처음 실패한 시점)
    if (__ITER === 0 && stressLevel >= 3) {
      breakingPoint.add(1);
      console.log(`🔴 Breaking Point 감지! VU: ${currentVu}, Level: ${stressLevel}`);
    }
  }

  // 로깅 (간헐적으로만)
  if (__ITER % 100 === 0) {
    const elapsedMinutes = Math.floor((Date.now() - testStartTime) / 60000);
    console.log(
      `[Level ${stressLevel}] VU: ${currentVu}, ` +
      `Status: ${response.status}, ` +
      `Duration: ${duration}ms, ` +
      `Elapsed: ${elapsedMinutes}분`
    );
  }

  // 대기 시간 (스트레스 레벨에 따라 조정)
  if (stressLevel <= 2) {
    sleep(randomIntBetween(1, 2)); // 정상: 1-2초
  } else if (stressLevel <= 4) {
    sleep(randomIntBetween(0, 1)); // 스트레스: 0-1초
  } else {
    sleep(0.5); // 극심한 스트레스: 0.5초
  }
}

// ======================
// Teardown
// ======================
export function teardown(data) {
  console.log('\n========================================');
  console.log('스트레스 테스트 종료');
  console.log('========================================\n');

  const testDuration = Math.floor((Date.now() - data.testStartTime) / 60000);
  console.log(`총 소요 시간: ${testDuration}분`);
}

// ======================
// 커스텀 요약
// ======================
export function handleSummary(data) {
  console.log('\n========== @@@@@ 스트레스 테스트 결과 @@@@@ ==========\n');

  const metrics = data.metrics;

  console.log('### 전체 메트릭:');
  console.log(`  - 총 요청 수: ${(metrics.http_reqs?.values?.count ?? 0).toLocaleString()}`);
  console.log(`  - 실패 요청 수: ${(metrics.http_req_failed?.values?.passes ?? 0).toLocaleString()}`);
  console.log(`  - 전체 에러율: ${((metrics.http_req_failed?.values?.rate ?? 0) * 100).toFixed(2)}%`);
  console.log(`  - RPS (초당 요청): ${(metrics.http_reqs?.values?.rate ?? 0).toFixed(2)}`);

  console.log('\n### 응답 시간:');
  const duration = metrics.http_req_duration?.values ?? {};
  console.log(`  - 평균: ${duration.avg != null ? duration.avg.toFixed(2) : 'N/A'}ms`);
  console.log(`  - 중앙값 (P50): ${duration.med != null ? duration.med.toFixed(2) : 'N/A'}ms`);
  console.log(`  - P90: ${duration['p(90)'] != null ? duration['p(90)'].toFixed(2) : 'N/A'}ms`);
  console.log(`  - P95: ${duration['p(95)'] != null ? duration['p(95)'].toFixed(2) : 'N/A'}ms`);
  console.log(`  - P99: ${duration['p(99)'] != null ? duration['p(99)'].toFixed(2) : 'N/A'}ms`);
  console.log(`  - 최대: ${duration.max != null ? duration.max.toFixed(2) : 'N/A'}ms`);

  console.log('\n### 스트레스 분석:');
  const errorRate = metrics.http_req_failed?.values?.rate ?? 0;
  const p95 = metrics.http_req_duration?.values?.['p(95)'] ?? 0;
  const p99 = metrics.http_req_duration?.values?.['p(99)'] ?? 0;

  console.log(`  - Breaking Point 도달 횟수: ${metrics.breaking_point?.values.count || 0}`);

  // 시스템 한계 판단
  console.log('\n🔍 시스템 한계 분석:');

  if (errorRate < 0.01 && p95 < 1000) {
    console.log('  ✅ 등급 S: 극한 상황에서도 매우 안정적입니다!');
    console.log('     - 1000 VUs 이상도 처리 가능');
  } else if (errorRate < 0.05 && p95 < 2000) {
    console.log('  ✅ 등급 A: 높은 부하에서도 양호한 성능을 보입니다.');
    console.log('     - 800-1000 VUs까지 안정적');
  } else if (errorRate < 0.1 && p95 < 3000) {
    console.log('  ⚠️  등급 B: 중간 정도의 스트레스까지 견딥니다.');
    console.log('     - 400-600 VUs까지 권장');
    console.log('     - 최적화 필요');
  } else if (errorRate < 0.2 && p95 < 5000) {
    console.log('  ⚠️  등급 C: 낮은 수준의 스트레스만 견딥니다.');
    console.log('     - 200-400 VUs까지 권장');
    console.log('     - 시급한 최적화 필요');
  } else {
    console.log('  🔴 등급 F: 스트레스 상황을 감당하기 어렵습니다.');
    console.log('     - 기본 부하(100-200 VUs)만 권장');
    console.log('     - 전면적인 아키텍처 개선 필요');
  }

  console.log('\n### 권장 조치:');

  if (errorRate > 0.1) {
    console.log('  🔴 긴급 조치 필요:');
    console.log('     1. DB 커넥션 풀 크기 확인 및 증가');
    console.log('     2. 애플리케이션 스레드 풀 설정 검토');
    console.log('     3. 메모리 설정 확인 (Heap size)');
    console.log('     4. DB 슬로우 쿼리 분석 및 최적화');
  }

  if (p95 > 2000) {
    console.log('  ⚠️  성능 개선 필요:');
    console.log('     1. 캐싱 전략 도입/개선');
    console.log('     2. DB 인덱스 최적화');
    console.log('     3. N+1 쿼리 제거');
    console.log('     4. 비동기 처리 도입');
  }

  console.log('\n  ### 인프라 고려사항:');
  console.log('     1. 수평 확장 (Scale-out) 검토');
  console.log('     2. 오토스케일링 정책 수립');
  console.log('     3. 로드 밸런서 설정 최적화');
  console.log('     4. CDN 도입 검토');

  console.log('\n  ### 모니터링:');
  console.log('     1. APM 도구 도입 (New Relic, DataDog 등)');
  console.log('     2. 실시간 알림 설정');
  console.log('     3. 대시보드 구성');
  console.log('     4. 로그 집계 시스템 구축');

  console.log('\n================================================\n');

  return {
    '04-stress-test-summary.json': JSON.stringify(data, null, 2),
  };
}

/**
 * ======================
 * 학습 포인트
 * ======================
 *
 * 1. 스트레스 테스트의 목적:
 *    - 시스템이 견딜 수 있는 최대 부하 확인
 *    - 실패 지점 파악
 *    - 실패 시 동작 방식 관찰
 *    - 회복 능력 테스트
 *
 * 2. 부하 vs 스트레스 vs 스파이크:
 *    - 부하: 예상 트래픽으로 성능 확인
 *    - 스트레스: 시스템 한계까지 밀어붙임
 *    - 스파이크: 갑작스러운 트래픽 급증
 *
 * 3. 관찰 포인트:
 *    - 어느 VU 수에서 에러가 급증하는가?
 *    - 응답 시간이 급격히 증가하는 구간은?
 *    - DB 커넥션 풀이 고갈되는가?
 *    - 메모리 사용량은 어떻게 변하는가?
 *    - 정상 부하로 돌아왔을 때 회복되는가?
 *
 * 4. 일반적인 병목 지점:
 *    - DB 커넥션 풀
 *    - 스레드 풀
 *    - 메모리 (Heap/Stack)
 *    - CPU
 *    - 네트워크 대역폭
 *    - 디스크 I/O
 *
 * 5. 실전 팁:
 *    - APM 도구와 함께 사용하기
 *    - 각 구간마다 시스템 리소스 확인
 *    - 로그 레벨을 ERROR로 설정 (INFO는 부하 증가)
 *    - 테스트 전후 DB 백업
 */
