package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.common.LockHelper;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.function.Supplier;

import static io.hhplus.tdd.point.enums.TransactionType.CHARGE;
import static io.hhplus.tdd.point.exception.ErrorCode.INVALID_CHARGE_POINT;
import static io.hhplus.tdd.point.exception.ErrorCode.NOT_ENOUGH_POINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class PointServiceTest {

    @Mock
    private PointHistoryRepository pointHistoryRepository;
    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private LockHelper lockHelper;

    @InjectMocks
    private PointService pointService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("유저 id를 받아, 해당 유저의 포인트를 조회한다")
    @Test
    void getPoint() {
        //given
        long userId = 1L;
        long amount = 1000L;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        when(userPointRepository.selectById(userId)).thenReturn(userPoint);

        // when
        UserPoint result = pointService.getPoint(userId);

        //then
        assertThat(result.point()).isEqualTo(amount);
    }

    @DisplayName("받은 포인트만큼 포인트를 충전한다.")
    @Test
    void charge() {
        //given
        long userId = 2L;
        long remainAmount = 1000L;
        long chargeAmount = 1000L;
        long expectedAmount = 2000L;

        UserPoint curUserPoint = new UserPoint(userId, remainAmount, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(userId, expectedAmount, System.currentTimeMillis());

        when(userPointRepository.selectById(userId)).thenReturn(curUserPoint);
        when(userPointRepository.insertOrUpdate(userId, expectedAmount)).thenReturn(updatedUserPoint);
        when(lockHelper.executeWithLock(eq(userId), Mockito.<Supplier<UserPoint>>any())).thenAnswer(invocation -> {
            Supplier<UserPoint> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        //when
        UserPoint result = pointService.charge(userId, chargeAmount);

        //then
        assertThat(result.point()).isEqualTo(expectedAmount);
    }

    @DisplayName("0 미만의 포인트를 충전하려고하면 예외를 반환한다.")
    @Test
    void chargeInvalidPoint() {
        //given
        long userId = 3L;
        long amount = -1000L;

        //when //then
        assertThatThrownBy(() -> pointService.charge(userId, amount))
                .isInstanceOf(PointException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_CHARGE_POINT);

    }


    @DisplayName("사용하는 포인트만큼 차감이 된다.")
    @Test
    void use() {
        //given
        long userId = 4L;
        long initAmount = 1000L;
        long useAmount = 100L;
        long resultAmount = 900L;

        UserPoint initUserPoint = new UserPoint(userId, initAmount, System.currentTimeMillis());
        UserPoint resultUserPoint = new UserPoint(userId, initAmount - useAmount, System.currentTimeMillis());

        when(userPointRepository.insertOrUpdate(userId, initAmount - useAmount)).thenReturn(resultUserPoint);
        when(userPointRepository.selectById(userId)).thenReturn(initUserPoint);
        when(lockHelper.executeWithLock(eq(userId), Mockito.<Supplier<UserPoint>>any())).thenAnswer(invocation -> {
            Supplier<UserPoint> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        //when
        UserPoint result = pointService.use(userId, useAmount);

        //then
        assertThat(result.point()).isEqualTo(resultAmount);
    }

    @DisplayName("0 미만의 포인트를 사용하려고 하면 예외를 반환한다.")
    @Test
    void useInvalidPoint() {
        //given
        long userId = 5L;
        long amount = -1000L;

        //when //then
        assertThatThrownBy(() -> pointService.use(userId, amount))
                .isInstanceOf(PointException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_CHARGE_POINT);

    }

    @DisplayName("가지고 있는 포인트 이상의 포인트를 사용하려고 하면 예외를 반환한다.")
    @Test
    void useOverPoint() {
        //given
        long userId = 6L;
        long initAmount = 1000L;
        long useAmount = 2000L;

        UserPoint initUserPoint = new UserPoint(userId, initAmount, System.currentTimeMillis());
        UserPoint resultUserPoint = new UserPoint(userId, initAmount - useAmount, System.currentTimeMillis());

        when(userPointRepository.insertOrUpdate(userId, initAmount - useAmount)).thenReturn(resultUserPoint);
        when(userPointRepository.selectById(userId)).thenReturn(initUserPoint);
        when(lockHelper.executeWithLock(eq(userId), Mockito.<Supplier<UserPoint>>any())).thenAnswer(invocation -> {
            Supplier<UserPoint> supplier = invocation.getArgument(1);
            return supplier.get();
        });


        //when //then
        assertThatThrownBy(() -> pointService.use(userId, useAmount))
                .isInstanceOf(PointException.class)
                .extracting("errorCode")
                .isEqualTo(NOT_ENOUGH_POINT);
    }

    @DisplayName("포인트 사용 내역을 조회한다.")
    @Test
    void history() {
        //given
        long pointId = 1L;
        long pointId2 = 2L;
        long userId = 7L;
        long chargeAmount = 1000L;
        long useAmount = 500L;

        PointHistory pointHistory = new PointHistory(pointId, userId, chargeAmount, CHARGE, System.currentTimeMillis());
        PointHistory pointHistory2 = new PointHistory(pointId2, userId, useAmount, CHARGE, System.currentTimeMillis());
        List<PointHistory> histories = List.of(pointHistory, pointHistory2);

        when(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(histories);

        //when
        List<PointHistory> result = pointService.getHistory(userId);

        //then
        assertThat(result.size()).isEqualTo(2);
    }
}