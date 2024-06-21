package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.domain.UserPoint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

class UserPointRepositoryTest {

    @Mock
    private UserPointRepository userPointRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("포인트를 충전하면 유저의 포인트가 충전된다.")
    @Test
    void insertPoint() {

        //given
        long userId = 1;
        long chargeAmount = 1000;
        long updateMillis = System.currentTimeMillis();

        UserPoint userPoint = new UserPoint(userId, chargeAmount, updateMillis);
        when(userPointRepository.insertOrUpdate(userId, chargeAmount)).thenReturn(userPoint);

        //when
        UserPoint result = userPointRepository.insertOrUpdate(userId, chargeAmount);

        //then
        Assertions.assertThat(result.point()).isEqualTo(chargeAmount);
    }


    @DisplayName("유저의 포인트를 조회한다.")
    @Test
    void getPoint() {

        //given
        long userId = 2L;
        long chargeAmount = 1000L;
        long updateMillis = System.currentTimeMillis();

        UserPoint userPoint = new UserPoint(userId, chargeAmount, updateMillis);
        when(userPointRepository.selectById(userId)).thenReturn(userPoint);

        //when
        UserPoint result = userPointRepository.selectById(userId);

        //then
        Assertions.assertThat(result.point()).isEqualTo(chargeAmount);
    }
}