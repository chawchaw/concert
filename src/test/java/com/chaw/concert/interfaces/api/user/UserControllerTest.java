//package com.chaw.concert.interfaces.api.user;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.restdocs.RestDocumentationContextProvider;
//import org.springframework.restdocs.RestDocumentationExtension;
//import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
//import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
//import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
//import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
//import static org.springframework.restdocs.payload.PayloadDocumentation.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
//@SpringBootTest
//@AutoConfigureMockMvc
//public class UserControllerTest {
//
//    @Autowired
//    private WebApplicationContext context;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @BeforeEach
//    public void setUp(RestDocumentationContextProvider restDocumentation) {
//        mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
//                .apply(documentationConfiguration(restDocumentation))
//                .build();
//    }
//
//    @Test
//    public void requestToken() throws Exception {
//        RestDocumentationResultHandler documentationHandler =
//                MockMvcRestDocumentation.document("user/request-token",
//                        preprocessRequest(prettyPrint()),
//                        preprocessResponse(prettyPrint()),
//                        responseFields(
//                                fieldWithPath("token").description("대기열 토큰")
//                        ));
//
//        mockMvc.perform(post("/api/users/token")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        )
//                .andExpect(status().isCreated())
//                .andDo(documentationHandler);
//    }
//
//    @Test
//    public void chargePoint() throws Exception {
//        RestDocumentationResultHandler documentationHandler =
//                MockMvcRestDocumentation.document("user/charge-point",
//                        preprocessRequest(prettyPrint()),
//                        preprocessResponse(prettyPrint()),
//                        requestFields(
//                                fieldWithPath("point").description("충전할 포인트")
//                        ),
//                        responseFields(
//                                fieldWithPath("balance").description("충전 후 잔액"),
//                                fieldWithPath("point").description("충전한 포인트")
//                        ));
//        String json = "{\"point\": 100}";
//
//        mockMvc.perform(post("/api/users/point")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isCreated())
//                .andDo(documentationHandler);
//    }
//
//    @Test
//    public void getPoint() throws Exception {
//        RestDocumentationResultHandler documentationHandler =
//                MockMvcRestDocumentation.document("user/get-point",
//                        preprocessRequest(prettyPrint()),
//                        preprocessResponse(prettyPrint()),
//                        responseFields(
//                                fieldWithPath("balance").description("현재 포인트")
//                        ));
//
//        mockMvc.perform(get("/api/users/point")
//                        .contentType(MediaType.APPLICATION_JSON)
//                )
//                .andExpect(status().isOk())
//                .andDo(documentationHandler);
//    }
//}
