# 좌석 조회 Index 안 쓴 이유
- 조회 쿼리
    ```sql
    SELECT *
    FROM ticket
    WHERE
    concert_schedule_id = 4;
    ```
    
- 현실세계 파악
  - 년간 공연일정 1만개 가정
  - 공연일정당 좌석수 1000개
  - 년간 좌석수 1000만개

- 테스트 데이터 생성
  - Row: 1000만개
  - concert_schedule_id: 1 ~ 1000

- Explain 결과
  - 인덱스 추가 전: type == SIMPLE, rows == 9973923, filtered == 10
- 결론
  - 인덱스 추가 전: 60ms
  - 인덱스를 사용하지 않아도 충분히 빠름
