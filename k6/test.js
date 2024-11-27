import http from 'k6/http';
import { check, sleep } from 'k6';

const HOST = 'http://spring-boot:8080/api/v1';
const SIGNUP_URL = `${HOST}/auth/join`;
const LOGIN_URL = `${HOST}/auth/login`;
const QUEUE_URL = `${HOST}/queue`;

const REPEAT_COUNT = 10;
const SLEEP_SECOND = 1;
const TARGET_USER_COUNT_PER_STAGE = 500;

export const options = {
  thresholds: { // 성능 기준
    http_req_failed: ['rate<0.05'], // 5% 미만 실패율 허용
    http_req_duration: ['p(95)<3000'], // 95% 요청이 *ms 이내로 완료되어야 함
  },
  stages: [
    { duration: '1s', target: TARGET_USER_COUNT_PER_STAGE * 1 }, // 1초 후 동시 사용자
    { duration: '1s', target: TARGET_USER_COUNT_PER_STAGE * 2 }, // 2초 후 동시 사용자
    { duration: '1s', target: TARGET_USER_COUNT_PER_STAGE * 3 }, // 3초 후 동시 사용자
    { duration: '1s', target: TARGET_USER_COUNT_PER_STAGE * 4 }, // 4초 후 동시 사용자
    { duration: '1s', target: TARGET_USER_COUNT_PER_STAGE * 5 }, // 5초 후 동시 사용자
  ],
};

export default async function () {
  // 계정
  const username = `user_${Date.now()}`;
  const password = 'test_password';

  // 회원가입
  signup(username, password);

  // 로그인
  const loginResponse = login(username, password);

  // 로그인 성공 시 토큰 추출
  const authToken = loginResponse.json('token'); // 예: JWT 토큰

  // 대기열 입장
  joinQueue(authToken);

  // 대기열 상태 조회 (1초마다 반복)
  for (let i = 0; i < REPEAT_COUNT; i++) {
    getQueueStatus(authToken);
    sleep(SLEEP_SECOND); // 1초 대기
  }
}

function getHeadersWithAuth (authToken) {
  const authHeaders = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${authToken}`,
  };
  return { headers: authHeaders };
}

function signup(username, password) {
  const signupPayload = JSON.stringify({
    username,
    password,
  });

  const signupHeaders = {
    'Content-Type': 'application/json',
  };

  const signupResponse = http.post(SIGNUP_URL, signupPayload, {
    headers: signupHeaders,
  });

  check(signupResponse, {
    'Signup response status is 200': (r) => r.status === 200,
  });
}

function login(username, password) {
  const loginPayload = JSON.stringify({
    username,
    password,
  });

  const loginHeaders = {
    'Content-Type': 'application/json',
  };

  const loginResponse = http.post(LOGIN_URL, loginPayload, {
    headers: loginHeaders,
  });

  check(loginResponse, {
    'Login response status is 200': (r) => r.status === 200,
  });

  return loginResponse;
}

function joinQueue(authToken) {
  const headers = getHeadersWithAuth(authToken);
  const joinQueueResponse = http.post(QUEUE_URL, null, headers);

  check(joinQueueResponse, {
    'Join queue response status is 200': (r) => r.status === 200,
  });
}

function getQueueStatus(authToken) {
  const headers = getHeadersWithAuth(authToken);
  const resultQueue = http.post(QUEUE_URL, null, headers);

  check(resultQueue, {
    'Queue status response status is 200': (r) => r.status === 200,
  });
}
