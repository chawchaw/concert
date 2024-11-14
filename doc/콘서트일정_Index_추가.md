# 콘서트 일정 조회 쿼리 최적화
- 조회 쿼리
    ```sql
    SELECT *
    FROM concert_schedule
    WHERE
        concert_id = 1
        AND is_sold_out = 0
        AND date_concert BETWEEN '2024-12-01' AND '2024-12-31';
    ```
    
- 현실세계 파악: 인터파크 분석
  - 월간 300개 공연 * 30일 = 9000 개
    - 모든 공연이 30일 있다고 가정하여 최대치 추측
    - 년간 공연수 = 9000 * 12 = 10만개
    - 400만개는 40년치 이기 때문에 충분
    - 참조: [https://tickets.interpark.com/contents/ranking](https://tickets.interpark.com/contents/ranking?genre=MUSICAL)

- 테스트 데이터 생성
  - Row: 400만개
  - concert_id: 1 ~ 100
  - is_sold_out: 0 or 1
  - date_concert: 오늘 ~ 1년 뒤

- Index 추가
    ```sql
    CREATE INDEX idx_concert_schedule ON concert_schedule (date_concert, is_sold_out, concert_id); -- 100ms
    CREATE INDEX idx_concert_schedule ON concert_schedule (date_concert, concert_id, is_sold_out); -- 100ms
    CREATE INDEX idx_concert_schedule ON concert_schedule (concert_id, is_sold_out, date_concert); -- 60ms
    ```
  - concert_id, is_sold_out, date_concert 순으로 복합인덱스 구성시 가장 빠름
- Explain 결과
  - 인덱스 추가 전: type == all, possible_keys == NULL, rows == 3986466, filtered == 0.56
  - 인덱스 추가 후: type == range, possible_keys == idx_concert_schedule, rows == 1624, filtered == 100
- 결론
  - 인덱스 추가 전: 400ms
  - 인덱스 추가 후: 60ms
  - 실행 속도 개선: 660% 상승
