/**
 * K6 부하 테스트 유틸리티 함수
 *
 * 이 파일은 K6 테스트 스크립트에서 공통으로 사용되는 함수들을 모아둔 것입니다.
 */

/**
 * 랜덤 정수 생성
 * @param {number} min - 최소값 (포함)
 * @param {number} max - 최대값 (포함)
 * @returns {number} min과 max 사이의 랜덤 정수
 */
export function randomIntBetween(min, max) {
  return Math.floor(Math.random() * (max - min + 1) + min);
}

/**
 * 랜덤 이메일 생성
 * @returns {string} 랜덤 이메일 주소
 */
export function randomEmail() {
  const randomString = Math.random().toString(36).substring(7);
  return `test_${randomString}@example.com`;
}

/**
 * 랜덤 닉네임 생성
 * @returns {string} 랜덤 닉네임
 */
export function randomNickname() {
  const adjectives = ['Happy', 'Lucky', 'Brave', 'Swift', 'Clever'];
  const nouns = ['Tiger', 'Eagle', 'Dragon', 'Phoenix', 'Lion'];
  const adj = adjectives[randomIntBetween(0, adjectives.length - 1)];
  const noun = nouns[randomIntBetween(0, nouns.length - 1)];
  const number = randomIntBetween(1, 9999);
  return `${adj}${noun}${number}`;
}

/**
 * HTTP 에러 로깅 함수
 * @param {object} response - K6 HTTP 응답 객체
 * @param {string} requestName - 요청 이름
 */
export function logError(response, requestName) {
  if (response.status !== 200 && response.status !== 201) {
    console.error(`[ERROR] ${requestName} failed:`);
    console.error(`  Status: ${response.status}`);
    console.error(`  Body: ${response.body}`);
  }
}

/**
 * 현재 시간을 ISO 형식으로 반환
 * @returns {string} ISO 형식의 현재 시간
 */
export function getCurrentTime() {
  return new Date().toISOString();
}

/**
 * 미래 시간 생성 (현재 시간 + 지정된 시간)
 * @param {number} hours - 더할 시간
 * @returns {string} ISO 형식의 미래 시간
 */
export function getFutureTime(hours) {
  const future = new Date();
  future.setHours(future.getHours() + hours);
  return future.toISOString();
}

/**
 * 테스트 데이터 생성 - 회원
 * @returns {object} 회원 생성 요청 데이터
 */
export function generateMemberData() {
  return {
    email: randomEmail(),
    nickname: randomNickname(),
    password: 'Test1234!@',
  };
}

/**
 * 테스트 데이터 생성 - 이벤트
 * @param {number} capacity - 이벤트 정원
 * @returns {object} 이벤트 생성 요청 데이터
 */
export function generateEventData(capacity = 100) {
  return {
    title: `부하테스트 이벤트 ${randomIntBetween(1000, 9999)}`,
    description: 'K6 부하 테스트를 위한 이벤트입니다.',
    capacity: capacity,
    startAt: getCurrentTime(),
    endAt: getFutureTime(24),
  };
}

/**
 * 요약 리포트 생성
 * @param {object} data - K6 데이터 객체
 * @returns {string} 요약 리포트 문자열
 */
export function generateSummary(data) {
  const summary = {
    'Total Requests': data.metrics.http_reqs.values.count,
    'Failed Requests': data.metrics.http_req_failed.values.passes,
    'Request Rate (req/s)': data.metrics.http_reqs.values.rate.toFixed(2),
    'Average Duration (ms)': data.metrics.http_req_duration.values.avg.toFixed(2),
    'P95 Duration (ms)': data.metrics.http_req_duration.values['p(95)'].toFixed(2),
    'P99 Duration (ms)': data.metrics.http_req_duration.values['p(99)'].toFixed(2),
  };

  let report = '\n========== Test Summary ==========\n';
  for (const [key, value] of Object.entries(summary)) {
    report += `${key}: ${value}\n`;
  }
  report += '==================================\n';

  return report;
}

/**
 * 체크 함수 - HTTP 응답 상태 코드 검증
 * @param {object} response - K6 HTTP 응답 객체
 * @param {number} expectedStatus - 예상 상태 코드
 * @returns {boolean} 검증 결과
 */
export function checkStatus(response, expectedStatus = 200) {
  return response.status === expectedStatus;
}

/**
 * 체크 함수 - 응답 시간 검증
 * @param {object} response - K6 HTTP 응답 객체
 * @param {number} maxDuration - 최대 허용 응답 시간 (ms)
 * @returns {boolean} 검증 결과
 */
export function checkDuration(response, maxDuration = 1000) {
  return response.timings.duration < maxDuration;
}

/**
 * 환경 변수 가져오기 (기본값 포함)
 * @param {string} key - 환경 변수 키
 * @param {string} defaultValue - 기본값
 * @returns {string} 환경 변수 값 또는 기본값
 */
export function getEnv(key, defaultValue) {
  return __ENV[key] || defaultValue;
}

/**
 * 부하 테스트 시나리오별 기본 옵션
 */
export const scenarios = {
  // 스모크 테스트: 최소한의 부하로 기본 기능 확인
  smoke: {
    executor: 'constant-vus',
    vus: 1,
    duration: '1m',
  },

  // 부하 테스트: 예상 트래픽으로 성능 확인
  load: {
    executor: 'ramping-vus',
    startVUs: 0,
    stages: [
      { duration: '2m', target: 100 },
      { duration: '5m', target: 100 },
      { duration: '2m', target: 0 },
    ],
  },

  // 스트레스 테스트: 시스템 한계 확인
  stress: {
    executor: 'ramping-vus',
    startVUs: 0,
    stages: [
      { duration: '2m', target: 100 },
      { duration: '5m', target: 200 },
      { duration: '2m', target: 300 },
      { duration: '5m', target: 400 },
      { duration: '10m', target: 0 },
    ],
  },

  // 스파이크 테스트: 급격한 트래픽 증가
  spike: {
    executor: 'ramping-vus',
    startVUs: 0,
    stages: [
      { duration: '10s', target: 100 },
      { duration: '1m', target: 2000 },
      { duration: '10s', target: 100 },
      { duration: '3m', target: 0 },
    ],
  },

  // Soak 테스트: 장시간 안정성 확인
  soak: {
    executor: 'constant-vus',
    vus: 100,
    duration: '1h',
  },
};

/**
 * 공통 임계값 (Thresholds)
 * 성공 기준을 정의합니다.
 */
export const commonThresholds = {
  // HTTP 요청 실패율이 1% 미만이어야 함
  http_req_failed: ['rate<0.01'],

  // 95%의 요청이 500ms 이내에 완료되어야 함
  'http_req_duration{expected_response:true}': ['p(95)<500'],

  // 99%의 요청이 1000ms 이내에 완료되어야 함
  'http_req_duration{expected_response:true}': ['p(99)<1000'],
};

/**
 * 엄격한 임계값 (프로덕션용)
 */
export const strictThresholds = {
  http_req_failed: ['rate<0.001'],
  'http_req_duration{expected_response:true}': ['p(95)<200', 'p(99)<500'],
};

/**
 * 느슨한 임계값 (개발 환경용)
 */
export const relaxedThresholds = {
  http_req_failed: ['rate<0.05'],
  'http_req_duration{expected_response:true}': ['p(95)<1000', 'p(99)<2000'],
};
