package io.hhplus.tdd.point.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static io.hhplus.tdd.point.enums.TransactionType.CHARGE;
import static io.hhplus.tdd.point.enums.TransactionType.USE;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @Autowired
    private ObjectMapper objectMapper;


    @DisplayName("유저의 포인트를 조회한다")
    @Test
    void point() throws Exception {
        //given
        long userId = 1L;
        long amount = 1000L;
        UserPoint result = new UserPoint(userId, amount, System.currentTimeMillis());
        when(pointService.getPoint(userId)).thenReturn(result);

        //when //then
        mockMvc.perform(get("/point/%s".formatted(userId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(amount));
    }

    @DisplayName("유저의 포인트를 충전한다.")
    @Test
    void charge() throws Exception {
        //given
        long userId = 2L;
        long amount = 1000L;
        UserPoint result = new UserPoint(userId, amount, System.currentTimeMillis());
        when(pointService.charge(userId, amount)).thenReturn(result);

        //when //then
        mockMvc.perform(patch("/point/%s/charge".formatted(userId))
                        .content(objectMapper.writeValueAsString(amount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(amount));

    }


    @DisplayName("유저의 포인트를 사용한다.")
    @Test
    void use() throws Exception {
        //given
        long userId = 3L;
        long amount = 1000L;
        long useAmount = 500L;

        UserPoint result = new UserPoint(userId, useAmount, System.currentTimeMillis());
        when(pointService.use(userId, amount)).thenReturn(result);

        //when //then
        mockMvc.perform(patch("/point/%s/use".formatted(userId))
                        .content(objectMapper.writeValueAsString(amount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(useAmount));


    }

    @DisplayName("유저의 포인트 충전/이용 내역을 조회한다.")
    @Test
    void history() throws Exception {
        //given
        long userId = 4;
        List<PointHistory> result = List.of(new PointHistory(userId, userId, 100, CHARGE, System.currentTimeMillis()),
                new PointHistory(userId, userId, 10, USE, System.currentTimeMillis()));

        when(pointService.getHistory(userId)).thenReturn(result);

        //when //then
        mockMvc.perform(get("/point/%s/histories".formatted(userId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));


    }
}