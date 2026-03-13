/**
 * K6 기본 부하 테스트 스크립트
 *
 * 학습 목표:
 * 1. K6의 기본 구조 이해
 * 2. HTTP 요청 보내기
 * 3. Checks로 응답 검증하기
 * 4. 메트릭 확인하기
 *
 * 사전 준비:
 * 1. 애플리케이션 실행
 *
 * 실행 방법:
 * k6 run 01-basic-load-test.js
 *
 * 옵션 오버라이드:
 * k6 run --vus 10 --duration 30s 01-basic-load-test.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// ======================
// 1. 설정 (Options)
// ======================
// 테스트 실행 설정을 정의합니다.
export const options = {
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

  // VUs (Virtual Users): 동시 실행될 가상 사용자 수
  vus: 10,

  // Duration: 테스트 지속 시간
  duration: '30s',

  // Thresholds: 성공 기준 (이 조건을 만족하지 못하면 테스트 실패)
  thresholds: {
    // HTTP 요청 실패율이 10% 미만이어야 함
    http_req_failed: ['rate<0.1'],

    // 95%의 요청이 2000ms 이내에 완료되어야 함
    http_req_duration: ['p(95)<2000'],
  },
};

// ======================
// 2. 커스텀 메트릭
// ======================
// 성공률을 추적하는 커스텀 메트릭
const successRate = new Rate('custom_success_rate');

// ======================
// 3. Setup 함수 (선택사항)
// ======================
// 테스트 시작 전 한 번만 실행됩니다.
// 예: 테스트 데이터 준비, 인증 토큰 획득 등
export function setup() {
  console.log('========================================');
  console.log('### 테스트 시작: 기본 부하 테스트');
  console.log('========================================');

  // Setup에서 반환한 데이터는 default 함수의 파라미터로 전달됩니다.
  return {
    // baseUrl: 'http://localhost:8080', // 동일한 네트워크이면
    baseUrl: 'http://host.docker.internal:8080', // docker 에서 실행하고 local을 테스트 하는 경우
    testStartTime: new Date().toISOString(),
  };
}

// ======================
// 4. Default 함수 (필수)
// ======================
// 각 VU가 반복적으로 실행하는 메인 함수입니다.
export default function (data) {
  // Setup에서 전달받은 데이터 사용
  const BASE_URL = data.baseUrl;

  // ----------------
  // 예제 1: 간단한 GET 요청
  // ----------------
  const response1 = http.get(`${BASE_URL}/`);

  // Check: 응답 검증
  const check1 = check(response1, {
    '상태 코드가 200 또는 404이다': (r) => r.status === 200 || r.status === 404,
    '응답 시간이 500ms 미만이다': (r) => r.timings.duration < 500,
  });

  // 커스텀 메트릭에 결과 기록
  successRate.add(check1);

  // ----------------
  // 예제 2: HTTP 헤더 추가
  // ----------------
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'User-Agent': 'K6-LoadTest/1.0',
    },
  };

  const response2 = http.get(`${BASE_URL}/`, params);

  check(response2, {
    'Content-Type 헤더가 올바르다': (r) => {
      // 응답이 없거나 헤더가 없을 수 있으므로 체크
      return r.status === 404 || (r.headers['Content-Type'] && r.headers['Content-Type'].includes('text/html'));
    },
  });

  // ----------------
  // 예제 3: 태그를 이용한 메트릭 그룹화
  // ----------------
  // 태그를 사용하면 메트릭을 그룹별로 필터링할 수 있습니다.
  const taggedParams = {
    tags: {
      name: 'index',
      type: 'static',
    },
  };

  http.get(`${BASE_URL}/`, taggedParams);

  // ----------------
  // 사용자 행동 시뮬레이션
  // ----------------
  // 실제 사용자는 페이지 간 이동 시 약간의 시간이 걸립니다.
  // sleep()으로 이를 시뮬레이션합니다.
  sleep(1); // 1초 대기

  // ----------------
  // 예제 4: 응답 본문 파싱 (JSON API의 경우)
  // ----------------
  // API 엔드포인트가 있다면 이렇게 사용합니다:
  const apiResponse = http.get(`${BASE_URL}/api/events`);

  check(apiResponse, {
    '상태 코드가 200이다': (r) => r.status === 200,
    '응답이 JSON이다': (r) => r.headers['Content-Type'].includes('application/json'),
  });

  // JSON 파싱
  try {
    const jsonData = JSON.parse(apiResponse.body);
    check(jsonData, {
      '데이터가 존재한다': (d) => d !== null,
    });
  } catch (e) {
    console.error('JSON 파싱 실패:', e);
  }
}

// ======================
// 5. Teardown 함수 (선택사항)
// ======================
// 테스트 종료 후 한 번만 실행됩니다.
// 예: 테스트 데이터 정리, 리소스 해제 등
export function teardown(data) {
  console.log('========================================');
  console.log('### 테스트 종료');
  console.log(`시작 시간: ${data.testStartTime}`);
  console.log(`종료 시간: ${new Date().toISOString()}`);
  console.log('========================================');
}

// ======================
// 6. 커스텀 요약 함수 (선택사항)
// ======================
// 테스트 결과를 커스터마이징하여 출력합니다.
export function handleSummary(data) {
  const duration = data.metrics.http_req_duration?.values ?? {};
  const total = data.metrics.http_reqs?.values ?? {};
  const failed = data.metrics.http_req_failed?.values ?? {};

  const avg = duration.avg;
  const p95 = duration['p(95)'];
  const p99 = duration['p(99)'];

  console.log('\n========== @@@@@ 테스트 결과 요약 @@@@@ ==========');
  console.log(`총 요청 수: ${total.count ?? 0}`);
  console.log(`실패율: ${failed.rate ?? 0}`);

  console.log(`평균 응답 시간: ${avg != null ? avg.toFixed(2) : 'N/A'}ms`);
  console.log(`P95 응답 시간: ${p95 != null ? p95.toFixed(2) : 'N/A'}ms`);
  console.log(`P99 응답 시간: ${p99 != null ? p99.toFixed(2) : 'N/A'}ms`);
  console.log('=====================================\n');

  return {
    '01-basic-load-test-summary.json': JSON.stringify(data, null, 2),
  };
}

/**
 * ======================
 * 학습 포인트 정리
 * ======================
 *
 * 1. K6 스크립트 구조:
 *    - setup(): 테스트 전 초기화
 *    - default(): 메인 테스트 로직 (각 VU가 반복 실행)
 *    - teardown(): 테스트 후 정리
 *
 * 2. 주요 함수:
 *    - http.get/post/put/delete(): HTTP 요청
 *    - check(): 응답 검증
 *    - sleep(): 대기 시간
 *
 * 3. Options 설정:
 *    - vus: 가상 사용자 수
 *    - duration: 테스트 지속 시간
 *    - thresholds: 성공 기준
 *
 * 4. 메트릭:
 *    - http_req_duration: 요청 응답 시간
 *    - http_req_failed: 실패한 요청 비율
 *    - http_reqs: 초당 요청 수 (RPS)
 *
 * 5. 실행 명령어:
 *    - 기본: k6 run 01-basic-load-test.js
 *    - VUs 변경: k6 run --vus 50 01-basic-load-test.js
 *    - Duration 변경: k6 run --duration 1m 01-basic-load-test.js
 *    - 결과 저장: k6 run --out json=results.json 01-basic-load-test.js
 */
