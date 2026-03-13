/**
 * Soak 테스트 (장시간 안정성 테스트, Endurance Test)
 *
 * 테스트 목적:
 * 1. 장시간 운영 시 안정성 확인
 * 2. 메모리 누수 (Memory Leak) 감지
 * 3. 리소스 고갈 확인
 * 4. 성능 저하 패턴 파악
 *
 * 특징:
 * - 적당한 부하를 오랜 시간 유지
 * - 시간이 지남에 따라 성능이 저하되는지 관찰
 * - 메모리, DB 커넥션, 파일 핸들 등의 리소스 누수 확인
 *
 * 실제 사용 사례:
 * - 장기 운영 서비스 안정성 검증
 * - 메모리 누수 탐지
 * - 캐시 동작 확인
 * - GC(Garbage Collection) 패턴 분석
 *
 * 일반적으로 발견되는 문제:
 * - 메모리 누수로 인한 OOM (Out of Memory)
 * - DB 커넥션 미반환
 * - 파일 핸들 미닫힘
 * - 캐시 무한 증가
 * - 로그 파일 비대화
 *
 * 실행 방법:
 * k6 run 05-soak-test.js
 *
 * 빠른 테스트 (개발용):
 * k6 run -e DURATION=5m 05-soak-test.js
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

// Soak 테스트 지속 시간 (기본: 1시간, 환경변수로 변경 가능)
const SOAK_DURATION = __ENV.DURATION || '1h';

// ======================
// 커스텀 메트릭
// ======================
const memoryIssueDetector = new Counter('memory_issue_detected');
const performanceDegradation = new Counter('performance_degradation');
const timeWindowMetric = new Trend('response_time_by_window');
const errorRateByTime = new Rate('error_rate_by_time');

// ======================
// 테스트 옵션
// ======================
export const options = {
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

  // Soak 테스트 시나리오
  stages: [
    // 1단계: 워밍업
    { duration: '5m', target: 100 },

    // 2단계: Soak - 장시간 일정한 부하 유지
    { duration: SOAK_DURATION, target: 100 },

    // 3단계: 종료
    { duration: '5m', target: 0 },
  ],

  // Thresholds
  thresholds: {
    // 에러율 1% 미만 유지
    'http_req_failed': ['rate<0.01'],

    // P95 응답 시간이 1초 이내, P99 응답 시간이 2초 이내, 평균 응답 시간이 500ms 이내
    'http_req_duration': ['p(95)<1000', 'p(99)<2000', 'avg<500'],

  },
};

// ======================
// Setup
// ======================
export function setup() {
  console.log('\n========================================');
  console.log('### Soak 테스트 (장시간 안정성 테스트) 준비');
  console.log('========================================\n');

  console.log('목적:');
  console.log('  - 장시간 운영 시 안정성 확인');
  console.log('  - 메모리 누수 감지');
  console.log('  - 리소스 고갈 확인');
  console.log('  - 성능 저하 패턴 파악');
  console.log('');

  console.log('###  테스트 구성:');
  console.log(`  - 워밍업: 5분 (0 → 100 VUs)`);
  console.log(`  - Soak: ${SOAK_DURATION} (100 VUs 유지)`);
  console.log(`  - 종료: 5분 (100 → 0 VUs)`);
  console.log('');

  const durationInMinutes = SOAK_DURATION.includes('h')
    ? parseInt(SOAK_DURATION) * 60
    : parseInt(SOAK_DURATION);
  console.log(`예상 총 소요 시간: ${durationInMinutes + 10}분`);
  console.log('');

  console.log('### 모니터링 포인트:');
  console.log('  1. 메모리 사용량 증가 추이');
  console.log('  2. 응답 시간 증가 추이');
  console.log('  3. 에러율 변화');
  console.log('  4. DB 커넥션 수');
  console.log('  5. CPU 사용률');
  console.log('  6. GC 빈도 및 소요 시간');
  console.log('');

  console.log('### 팁:');
  console.log('  - JVM 모니터링: jstat -gcutil <pid> 1000');
  console.log('  - 메모리 덤프: jmap -dump:live,format=b,file=heap.bin <pid>');
  console.log('  - 스레드 덤프: jstack <pid>');
  console.log('');

  const headers = { 'Content-Type': 'application/json' };

  // 테스트용 이벤트 생성
  console.log('테스트 이벤트 생성 중...');
  const eventPayload = JSON.stringify({
    title: `Soak 테스트 이벤트 ${Date.now()}`,
    description: '장시간 안정성 테스트',
    capacity: 10000,
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
  console.log('준비 완료 - Soak 테스트 시작!\n');

  return {
    eventId,
    baseUrl: BASE_URL,
    testStartTime: Date.now(),
    initialResponseTime: null,
  };
}

// ======================
// Default: 메인 테스트
// ======================
export default function (data) {
  const { eventId, baseUrl, testStartTime } = data;
  const headers = { 'Content-Type': 'application/json' };

  // 경과 시간 계산
  const elapsedMinutes = Math.floor((Date.now() - testStartTime) / 60000);
  const timeWindow = Math.floor(elapsedMinutes / 10); // 10분 단위로 그룹화

  // 요청 전송
  const startTime = Date.now();

  const response = http.get(`${baseUrl}/events/${eventId}`, {
    headers,
    tags: {
      name: 'soak_test',
      time_window: `window_${timeWindow}`,
    },
  });

  const duration = Date.now() - startTime;

  // 시간대별 응답 시간 기록
  timeWindowMetric.add(duration, { window: `window_${timeWindow}` });

  // 초기 응답 시간 저장 (비교 기준)
  if (!data.initialResponseTime && __ITER === 0) {
    data.initialResponseTime = duration;
  }

  // 성능 저하 감지 (초기 대비 2배 이상 느려지면)
  if (data.initialResponseTime && duration > data.initialResponseTime * 2) {
    performanceDegradation.add(1);

    if (__ITER % 100 === 0) {
      console.log(
        `⚠️  성능 저하 감지! ` +
        `초기: ${data.initialResponseTime}ms, 현재: ${duration}ms, ` +
        `경과: ${elapsedMinutes}분`
      );
    }
  }

  // 응답 검증
  const success = check(response, {
    '상태 코드 OK': (r) => r.status >= 200 && r.status < 500,
    '응답 시간 3초 이내': (r) => r.timings.duration < 3000,
  });

  errorRateByTime.add(!success, { window: `window_${timeWindow}` });

  // 에러 시 로깅
  if (!success) {
    console.error(
      `❌ 에러 발생! ` +
      `Status: ${response.status}, ` +
      `Duration: ${duration}ms, ` +
      `경과: ${elapsedMinutes}분`
    );
  }

  // 주기적인 상태 리포트 (10분마다)
  if (__VU === 1 && __ITER % 100 === 0 && elapsedMinutes > 0) {
    console.log(`\n### [${elapsedMinutes}분 경과] 상태 체크:`);
    console.log(`  - 현재 VUs: ${__VU}`);
    console.log(`  - 반복 횟수: ${__ITER}`);
    console.log(`  - 최근 응답 시간: ${duration}ms`);
  }

  // 사용자 행동 시뮬레이션
  sleep(randomIntBetween(1, 3));
}

// ======================
// Teardown
// ======================
export function teardown(data) {
  const testDurationMinutes = Math.floor((Date.now() - data.testStartTime) / 60000);

  console.log('\n========================================');
  console.log('Soak 테스트 종료');
  console.log('========================================\n');
  console.log(`총 소요 시간: ${testDurationMinutes}분`);
  console.log('\n다음 단계:');
  console.log('  1. 메모리 덤프 분석 (힙 덤프)');
  console.log('  2. GC 로그 분석');
  console.log('  3. 애플리케이션 로그 확인');
  console.log('  4. 데이터베이스 커넥션 풀 상태 확인');
  console.log('');
}

// ======================
// 커스텀 요약
// ======================
export function handleSummary(data) {
  console.log('\n========== ### Soak 테스트 결과 ==========\n');

  const metrics = data.metrics;

  console.log('### 전체 메트릭:');
  console.log(`  - 총 요청 수: ${metrics.http_reqs.values.count.toLocaleString()}`);
  console.log(`  - 실패 요청 수: ${metrics.http_req_failed.values.passes.toLocaleString()}`);
  console.log(`  - 에러율: ${(metrics.http_req_failed.values.rate * 100).toFixed(3)}%`);
  console.log(`  - RPS (초당 요청): ${metrics.http_reqs.values.rate.toFixed(2)}`);

  console.log('\n### 응답 시간 분석:');
  console.log(`  - 평균: ${metrics.http_req_duration.values.avg.toFixed(2)}ms`);
  console.log(`  - 최소: ${metrics.http_req_duration.values.min.toFixed(2)}ms`);
  console.log(`  - 최대: ${metrics.http_req_duration.values.max.toFixed(2)}ms`);
  console.log(`  - P50: ${metrics.http_req_duration.values.med.toFixed(2)}ms`);
  console.log(`  - P90: ${metrics.http_req_duration.values['p(90)'].toFixed(2)}ms`);
  console.log(`  - P95: ${metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`);
  console.log(`  - P99: ${metrics.http_req_duration.values['p(99)'].toFixed(2)}ms`);

  // 성능 저하 분석
  const performanceDegradationCount = metrics.performance_degradation?.values.count || 0;
  const totalRequests = metrics.http_reqs.values.count;
  const degradationRate = (performanceDegradationCount / totalRequests) * 100;

  console.log('\n### 성능 저하 분석:');
  console.log(`  - 성능 저하 감지 횟수: ${performanceDegradationCount.toLocaleString()}`);
  console.log(`  - 성능 저하 비율: ${degradationRate.toFixed(2)}%`);

  // 시간대별 분석 (만약 데이터가 있다면)
  if (metrics.response_time_by_window) {
    console.log('\n### 시간대별 응답 시간 추이:');
    console.log('  (10분 단위로 그룹화)');
    // 실제로는 Grafana 등으로 시각화하는 것이 더 효과적
  }

  // 종합 판정
  console.log('\n### 종합 판정:');
  const errorRate = metrics.http_req_failed.values.rate;
  const avgResponseTime = metrics.http_req_duration.values.avg;
  const p95ResponseTime = metrics.http_req_duration.values['p(95)'];

  let grade = 'F';
  let message = '';

  if (errorRate < 0.001 && p95ResponseTime < 500 && degradationRate < 1) {
    grade = 'S';
    message = '완벽한 안정성! 장시간 운영에도 성능 저하 없음.';
  } else if (errorRate < 0.01 && p95ResponseTime < 1000 && degradationRate < 5) {
    grade = 'A';
    message = '매우 안정적. 장시간 운영 가능.';
  } else if (errorRate < 0.05 && p95ResponseTime < 2000 && degradationRate < 10) {
    grade = 'B';
    message = '양호. 일부 최적화 필요.';
  } else if (errorRate < 0.1 && p95ResponseTime < 3000 && degradationRate < 20) {
    grade = 'C';
    message = '보통. 메모리 누수나 리소스 문제 가능성 있음.';
  } else {
    grade = 'F';
    message = '불안정. 장시간 운영 어려움. 긴급 조치 필요.';
  }

  console.log(`  등급: ${grade}`);
  console.log(`  평가: ${message}`);

  // 문제 징후 및 대응 방안
  if (degradationRate > 5) {
    console.log('\n⚠️  메모리 누수 의심:');
    console.log('  1. 힙 덤프 분석 (jmap, MAT 도구 사용)');
    console.log('  2. 객체 생성/해제 패턴 확인');
    console.log('  3. 캐시 크기 제한 확인');
    console.log('  4. Connection/Stream 미닫힘 확인');
  }

  if (errorRate > 0.01) {
    console.log('\n⚠️  리소스 고갈 의심:');
    console.log('  1. DB 커넥션 풀 설정 확인');
    console.log('  2. 스레드 풀 크기 확인');
    console.log('  3. 파일 핸들 수 확인 (ulimit)');
    console.log('  4. 네트워크 소켓 상태 확인');
  }

  if (p95ResponseTime > 2000) {
    console.log('\n⚠️  성능 저하 대응:');
    console.log('  1. GC 튜닝 (Young/Old Generation 크기 조정)');
    console.log('  2. DB 쿼리 최적화');
    console.log('  3. 캐시 전략 개선');
    console.log('  4. 비동기 처리 도입');
  }

  console.log('\n### 권장 모니터링:');
  console.log('  - JVM 메모리: VisualVM, JConsole');
  console.log('  - APM: Prometheus + Grafana, DataDog, Pinpoint');
  console.log('  - 로그: ELK Stack, Splunk, Grafana Loki');
  console.log('  - 메트릭: Prometheus + Grafana');

  console.log('\n================================================\n');

  return {
    '05-soak-test-summary.json': JSON.stringify(data, null, 2),
  };
}

/**
 * ======================
 * 학습 포인트
 * ======================
 *
 * 1. Soak 테스트의 목적:
 *    - 장시간 운영 시 안정성 확인
 *    - 메모리 누수, 리소스 고갈 감지
 *    - 성능 저하 패턴 파악
 *
 * 2. 일반적인 문제들:
 *    - 메모리 누수 (Memory Leak)
 *    - DB 커넥션 미반환
 *    - 파일 핸들 미닫힘
 *    - 캐시 무한 증가
 *    - GC Overhead
 *
 * 3. 모니터링 도구:
 *    - JVM: jstat, jmap, jstack, VisualVM
 *    - APM: New Relic, DataDog, Pinpoint, Prometheus + Grafana + Loki
 *    - Profiler: YourKit, JProfiler
 *    - 로그: ELK, Splunk
 *
 * 4. 지표 해석:
 *    - 응답 시간이 점진적으로 증가 → GC 빈도 증가, 메모리 부족
 *    - 에러율이 점진적으로 증가 → 리소스 고갈
 *    - 특정 시점에 급증 → 메모리 Full GC, Connection Pool 고갈
 *
 * 5. 실전 팁:
 *    - 실제 프로덕션 트래픽의 70-80% 수준으로 테스트
 *    - 최소 몇 시간 이상 (이상적으로는 24시간)
 *    - 다른 모니터링 도구와 함께 사용
 *    - 테스트 전후 힙 덤프 비교
 *
 * 6. 문제 해결:
 *    - 메모리 누수: 힙 덤프 분석, 객체 생성 패턴 확인
 *    - DB 이슈: 슬로우 쿼리 로그, 커넥션 풀 모니터링
 *    - GC 이슈: GC 로그 분석, Heap 크기 조정
 *
 * ======================
 * 다음 단계
 * ======================
 * - 모니터링 대시보드 구성
 * - 알림 시스템 설정
 * - CI/CD 파이프라인 통합
 * - 정기적인 부하 테스트 일정 수립
 */
