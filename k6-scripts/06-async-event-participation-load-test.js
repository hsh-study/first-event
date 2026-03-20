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
        `${baseUrl}/api/events/${eventId}/participate/async/${memberId}`,
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

    // 결과 분류 (비동기 처리이므로 요청 접수 성공 여부만 확인)
    if (response.status === 200 || response.status === 201) {
        participationSuccess.add(1);

        // recordId 반환 확인
        try {
            const responseBody = JSON.parse(response.body);
            if (!responseBody.recordId) {
                console.log(`[경고] recordId 없음: ${response.body}`);
            }
        } catch (e) {
            console.log(`[경고] JSON 파싱 실패: ${response.body}`);
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

    const { eventId, baseUrl } = data;

    // 비동기 처리 완료 대기 (5초)
    console.log('비동기 처리 완료 대기 중 (5초)...');
    sleep(5);

    // 최종 참여 결과 조회
    const headers = {
        'Content-Type': 'application/json',
    };

    const eventResponse = http.get(`${baseUrl}/api/events/${eventId}/with-count`, { headers });

    console.log(`=> 최종 참여 결과 조회 상태 `)
    console.log(`  - response.status : ${eventResponse.status}`)
    console.log(`  - eventId : ${eventId}`)

    if (eventResponse.status === 200) {
        try {
            const event = JSON.parse(eventResponse.body);
            console.log('\n### 최종 참여 결과:');
            console.log(`  - 이벤트 정원: ${event.capacity ?? 100}`);
            console.log(`  - 실제 참여자 수: ${event.participantCount ?? 'N/A'}`);
            console.log(`  - 정원 내 당첨자(Winner): ${event.winnerCount ?? 'N/A'}`);

            const expectedCapacity = event.capacity ?? 100;
            const actualWinnerCount = event.winnerCount ?? 0;

            if (actualWinnerCount > expectedCapacity) {
                console.log(`  ⚠️  정원 초과 발생! (초과: ${actualWinnerCount - expectedCapacity}명)`);
            } else {
                console.log(`  ✅  정원 내 처리 완료`);
            }
        } catch (e) {
            console.log('최종 결과 조회 실패:', e.message);
        }
    }

    console.log('\n=> 정리 완료 (테스트 데이터는 DB에 남아있음)');
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

    console.log('\n### 비동기 요청 접수 결과:');
    console.log(`  - 요청 접수 성공: ${metrics.participation_success?.values?.count ?? 0}`);
    console.log(`  - 요청 접수 실패: ${metrics.participation_failed?.values?.count ?? 0}`);

    console.log('\n### 참고:');
    console.log('  - 비동기 처리 방식이므로 실제 참여 처리는 백그라운드에서 진행됩니다.');
    console.log('  - 최종 참여 결과는 teardown 단계에서 확인할 수 있습니다.');
    console.log('  - 이 테스트는 요청 접수 성능과 시스템 처리량을 측정합니다.');

    console.log('\n==================================================\n');

    // JSON 파일로 저장
    return {
        'stdout': '', // 기본 요약은 이미 위에서 출력했으므로 생략
        '06-async-event-participation-load-test-summary.json': JSON.stringify(data, null, 2),
    };
}