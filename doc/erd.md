# ERD
- User
  - 우리 서비스를 사용해주는 고객님입니다. 충성충성! 포인트 잔액 필드가 있습니다.
- WaitQueue
  - Queue 처럼 사용하기 위한 테이블입니다.
  - 사용자가 대기열에 들어가면 여기에 기록됩니다.
  - Update 는 하지 않기 때문에 락이 걸릴일이 없습니다.
- QueuePositionTracker
  - 대기열의 마지막 사용자를 추적하기 위한 테이블입니다.
  - 대기열이 없을 때는 is_wait_queue_exist 를 false 로 설정합니다.
  - 스케줄러가 is_wait_queue_exist 가 true 인 것을 찾아서 예약가능상태로 이동시킵니다.
- ReservationPhase
  - 예약 가능한 사용자를 관리하기 위한 테이블입니다.
  - 콘서트 일정마다 예약 가능한 사용자의 최대 수를 유지합니다.
  - 예약이 완료되면 이 테이블에서 삭제됩니다.
- Hall
  - 공연장소 정보입니다. 
  - ex) KSPO DOME, 잠실실내체육관
- Concert
  - 공연 정보입니다
  - ex) 2024 윤하 연말 콘서트, 2024 레드벨벳 콘서트
- ConcertSchedule
  - 공연 일정 정보입니다. 
  - ex) 2024 윤하 연말 콘서트 1회차, 2024 레드벨벳 콘서트 2회차
- Ticket
  - 티켓 정보입니다.
  - 공석, 임시예약, 예약완료 상태를 가집니다.
- PointHistory
  - 포인트 변동 내역입니다.
  - 충전, 결제, 환불을 기록합니다.

```mermaid
erDiagram
    User {
        long id PK
        varchar name
        int point_balance
    }

    PointHistory {
        long id PK
        long user_id FK
        long ticket_id FK
        enum type "충전,결제"
        decimal amount "변경 금액"
        datetime dateTransaction "변경일"
    }
    PointHistory }o--|| User : ""
    PointHistory }o--|| Ticket : "결제,환불,재결제를 고려하여 1:N"

    WaitQueue {
        long id PK
        long user_id FK
        long concert_schedule_id FK
        char uuid
    }
    WaitQueue ||--|| ConcertSchedule : "콘서트 일정별로 존재"

    QueuePositionTracker {
        long id PK
        long concert_schedule_id FK
        long wait_queue_id FK
        bool is_wait_queue_exist "대기열 존재 여부"
    }
    QueuePositionTracker ||--|| WaitQueue : "마지막 대기자 정보"

    ReservationPhase {
        long id PK
        long user_id FK
        long concert_schedule_id FK "콘서트마다 예약가능 사용자 관리"
    }

    Hall {
        long id PK
        varchar name "공연장 이름"
        varchar address "주소"
        varchar address_detail "상세주소"
        geometry location "위/경도"
    }

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
        long hall_id FK
        tinyint isSold "판매 중,판매 완료"
        int total_seat "총 좌석수"
        int available_seat "남은 좌석수"
        datetime dateConcert "공연일"
    }
    ConcertSchedule }o--|| Concert : "콘서트는 여러 일정을 가질 수 있음"
    ConcertSchedule }o--|| Hall : "각 콘서트 일정은 여러 장소에서 열릴 수 있음"

    Ticket {
        long id PK
        long concert_schedule_id FK
        enum type "VIP,1등석,2등석"
        enum status "공석, 임시예약, 예약완료"
        int price "가격"
        long temp_booking_user_id FK "임시예약한 사용자"
        datetime temp_booking_end_at "임시예약 마감일"
    }
    Ticket }o--|| ConcertSchedule : "콘서트 일정이 여러 티켓을 가질 수 있음"
    Ticket }o--|| User : "임시예약한 사용자"

```
