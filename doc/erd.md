# ERD
- User
  - 우리 서비스를 사용해주는 고객님입니다. 충성충성! 포인트 잔액 필드가 있습니다.
- WaitQueue
  - Queue 처럼 사용하기 위한 테이블입니다.
  - 사용자가 대기열에 들어가면 여기에 기록됩니다.
- Concert
  - 공연 정보입니다
  - ex) 2024 윤하 연말 콘서트, 2024 레드벨벳 콘서트
- ConcertSchedule
  - 공연 일정 정보입니다. 
  - ex) 2024 윤하 연말 콘서트 1회차, 2024 레드벨벳 콘서트 2회차
- Ticket
  - 티켓 정보입니다.
  - 공석, 예약, 결제완료 상태를 가집니다.
- PointHistory
  - 포인트 변동 내역입니다.
  - 충전, 결제, 환불을 기록합니다.

```mermaid
erDiagram
    User {
        long id PK
        varchar uuid
        varchar name
    }
    
    Point {
        long id PK
        long user_id FK
        int point_balance "포인트 잔액"
    }
    Point ||--|| User : "1:1"

    PointHistory {
        long id PK
        long point_id FK
        long ticket_id FK
        enum type "충전,결제"
        decimal amount "변경 금액"
        datetime dateTransaction "변경일"
    }
    PointHistory }o--|| Point : ""

    WaitQueue {
        long id PK
        long user_id FK
        char uuid "대기열 식별자"
        enum status "대기중, 통과"
        datetime created_at "대기열 진입 시간"
        datetime passed_at "통과 시간"
    }
    WaitQueue ||--|| User : "1:1"

    Concert {
        long id PK
        varchar name "공연이름"
        text info "공연정보"
        varchar artist "공연자"
        varchar host "주최자"
    }

    ConcertSchedule {
        long id PK
        long concert_id FK
        tinyint isSold "판매 중,판매 완료"
        int total_seat "총 좌석수"
        int available_seat "남은 좌석수"
        datetime dateConcert "공연일"
    }
    ConcertSchedule }o--|| Concert : "콘서트는 여러 일정을 가질 수 있음"

    Ticket {
        long id PK
        long concert_schedule_id FK
        enum type "VIP,1등석,2등석"
        enum status "공석, 예약, 결제완료"
        decimal price "가격"
        varchar seat_no "좌석번호"
        long reserve_user_id FK "예약한 사용자"
        datetime reserve_end_at "예약 마감일"
    }
    Ticket }o--|| ConcertSchedule : "콘서트 일정이 여러 티켓을 가질 수 있음"
    Ticket }o--|| User : "예약한 사용자"

    TicketTransaction {
        long id PK
        long user_id FK
        long ticket_id FK
        long point_history_id FK
        varchar idempotency_key "멱등성 키"
        varchar transaction_status "트랜잭션 상태 (pending, completed, failed, expired)"
        varchar payment_method "결제 수단 (포인트, 카드, 계좌이체)"
        jsonb payment_data "결제 데이터"
        decimal amount "결제 금액"
        datetime created_at "생성일"
        datetime updated_at "마지막 업데이트 시간"
        tinyint is_deleted "만료로 인한 삭제 여부"
    }
    TicketTransaction }o--|| Ticket : "1:1"
    TicketTransaction ||--|| PointHistory : "1:1, 결제 완료시 포인트이력에 저장"
```
