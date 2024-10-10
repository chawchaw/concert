package com.chaw.concert.interfaces.api.concert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
public class ConcertControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    public void getConcerts() throws Exception {
        RestDocumentationResultHandler documentationHandler =
                MockMvcRestDocumentation.document("concert/get-concerts",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("from").description("공연일 시작"),
                                fieldWithPath("to").description("공연일 끝")
                        ),
                        responseFields(
                                fieldWithPath("id").description("콘서트 ID"),
                                fieldWithPath("name").description("콘서트 이름"),
                                fieldWithPath("status").description("콘서트 상태 (판매 중, 판매 완료, 취소)"),
                                fieldWithPath("info").description("콘서트 정보"),
                                fieldWithPath("artist").description("출연 아티스트"),
                                fieldWithPath("host").description("주최사"),
                                fieldWithPath("date").description("공연일"),
                                fieldWithPath("can_buy_from").description("티켓 구매 가능 시작일"),
                                fieldWithPath("can_buy_to").description("티켓 구매 가능 종료일"),
                                fieldWithPath("hall_name").description("공연장 이름"),
                                fieldWithPath("hall_address").description("공연장 주소"),
                                fieldWithPath("hall_address_detail").description("공연장 상세 주소"),
                                fieldWithPath("hall_location").description("공연장 위치")
                        ));

        String json = "{\"from\": \"2024-10-01T00:00:00\", \"to\": \"2024-12-31T23:59:59\"}";

        mockMvc.perform(post("/api/concerts/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andDo(documentationHandler);
    }

    @Test
    public void getTickets() throws Exception {
        RestDocumentationResultHandler documentationHandler =
                MockMvcRestDocumentation.document("concert/get-tickets",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("콘서트 ID")
                        ),
                        responseFields(
                                fieldWithPath("items[].id").description("티켓 ID"),
                                fieldWithPath("items[].status").description("예매 상태 (공석, 임시예약, 예약완료)"),
                                fieldWithPath("items[].seat_zone").description("구역"),
                                fieldWithPath("items[].seat_no").description("좌석 번호"),
                                fieldWithPath("items[].seat_type").description("좌석 종류 (VIP, 1등석, 2등석)"),
                                fieldWithPath("items[].seat_price").description("가격")
                        )
                );

        mockMvc.perform(get("/api/concerts/{id}/tickets", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(documentationHandler);
    }

    @Test
    public void tempBooking() throws Exception {
        RestDocumentationResultHandler documentationHandler =
                MockMvcRestDocumentation.document("concert/temp-booking",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("티켓 ID")
                        ),
                        responseFields(
                                fieldWithPath("id").description("티켓 ID"),
                                fieldWithPath("status").description("예매 상태 (공석, 임시예약, 예약완료)"),
                                fieldWithPath("temp_booking_end_at").description("결제 유효 시간")
                        )
                );

        mockMvc.perform(post("/api/concerts/tickets/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(documentationHandler);
    }

    @Test
    public void pay() throws Exception {
        RestDocumentationResultHandler documentationHandler =
                MockMvcRestDocumentation.document("concert/pay",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("티켓 ID")
                        ),
                        responseFields(
                                fieldWithPath("id").description("티켓 ID"),
                                fieldWithPath("status").description("예매 상태 (공석, 임시예약, 예약완료)"),
                                fieldWithPath("booked_at").description("예약일"),
                                fieldWithPath("point_used").description("결제한 포인트"),
                                fieldWithPath("point_balance").description("포인트 잔액")
                        )
                );

        mockMvc.perform(post("/api/concerts/tickets/{id}/pay", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(documentationHandler);
    }
}
