# ERD
- User: 공연을 예약하는 사용자입니다. 포인트 잔액정보가 있습니다.
- Token: 대기열 정보가 담긴 토큰입니다. UUID 로 구분하고 한 사용자는 하나의 토큰만 가질수 있습니다.
- Hall: 공연장소 정보입니다. ex) KSPO DOME, 잠실실내체육관
- Concert: 공연 정보입니다. ex) 2024 윤하 연말 콘서트, 2024 레드벨벳 콘서트
- Seat: 공연장소의 좌석 정보입니다. ex) VIP A1, 1등석 B2
- Ticket: 티켓 정보입니다. 공석, 임시예약, 예약완료 상태를 가집니다. Concert와 Seat의 조합으로 Unique Key를 가집니다.
- PointHistory: 포인트 변동 내역입니다. 충전, 결제, 환불을 기록합니다.

```mermaid
erDiagram
    User {
        long id PK
        varchar name
        int pointBalance
    }

    Hall {
        long id PK
        varchar name "공연장소"
        varchar address "주소"
        varchar address_detail "상세주소"
        geometry location "위/경도"
    }

    Concert {
        long id PK
        long hall_id FK
        enum status "판매 중,판매 완료,취소"
        varchar name "공연이름"
        text info "공연정보"
        varchar artist "공연자"
        varchar host "주최자"
        datetime date "공연일"
        datetime can_buy_from "예매 시작일"
        datetime can_buy_to "예매 종료일"
    }
    Concert }o--|| Hall : ""

    Seat {
        long id PK
        long hall_id PK
        varchar zone "구역"
        varchar no "좌석번호"
        enum type "VIP,1등석,2등석"
        int price "가격"
    }
    Seat }o--|| Hall : ""

    Ticket {
        long id PK
        long seat_id FK
        long concert_id FK
        enum status "공석, 임시예약, 예약완료"
        long temp_booking_user_id FK "임시예약한 사용자"
        datetime temp_booking_end_at "임시예약 마감일"
        long booking_user_id FK "예약완료한 사용자"
        datetime booked_at "예약완료 일"
    }
    Ticket }o--|| Seat : ""
    Ticket }o--|| Concert : "Concert 생성시 Seat와 1:1 매핑되는 Ticket 자동생성"
    Ticket }o--|| User : ""

    PointHistory {
        long id PK
        long user_id FK
        long ticket_id FK
        enum type "충전,결제,환불"
        decimal amount "변경 금액"
        datetime date "변경일"
    }
    PointHistory }o--|| User : ""
    PointHistory }o--|| Ticket : "결제,환불,재결제를 고려하여 1:N"

    Token {
        long id PK
        long user_id PK
        char uuid
        int queue_position "0이면 예약가능, 0인 토큰 수 제한"
    }
    Token ||--|| User : "1:1로 하여 한 사용자가 여러 토큰을 발급받는것을 방지"

```
