package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.domain.PointHistory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static io.hhplus.tdd.point.enums.TransactionType.CHARGE;
import static io.hhplus.tdd.point.enums.TransactionType.USE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class PointHistoryRepositoryTest {

    @Mock
    private PointHistoryRepository pointHistoryRepository;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @DisplayName("포인트를 충전하면 포인트 충전 내역에 남는다.")
    @Test
    void chargeHistory() {
        //given
        long pointId = 1L;
        long userId = 1L;
        long chargeAmount = 1000L;
        long updateMillis = System.currentTimeMillis();

        PointHistory pointHistory = new PointHistory(pointId, userId, chargeAmount, CHARGE, updateMillis);
        when(pointHistoryRepository.insert(userId, chargeAmount, CHARGE, updateMillis)).thenReturn(pointHistory);

        //when
        PointHistory result = pointHistoryRepository.insert(userId, chargeAmount, CHARGE, updateMillis);


        //then
        assertThat(result)
                .extracting("userId", "amount", "type")
                .containsExactly(userId, chargeAmount, CHARGE);


    }

    @DisplayName("유저에 대한 포인트 사용 내역을 조회한다.")
    @Test
    void getHistories() {
        //given
        long pointId = 2L;
        long userId = 2L;
        long chargeAmount = 1000L;

        long pointId2 = 3L;
        long userId2 = 3L;
        long useAmount = 500L;

        PointHistory pointHistory = new PointHistory(pointId, userId, chargeAmount, CHARGE, System.currentTimeMillis());
        PointHistory pointHistory2 = new PointHistory(pointId2, userId2, useAmount, USE, System.currentTimeMillis());
        when(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(List.of(pointHistory,pointHistory2));

        //when
        List<PointHistory> result = pointHistoryRepository.selectAllByUserId(userId);

        //then
        Assertions.assertThat(result).hasSize(2);
    }
}