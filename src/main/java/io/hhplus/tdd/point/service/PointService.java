package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.common.LockHelper;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.enums.TransactionType;
import io.hhplus.tdd.point.exception.ErrorCode;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.hhplus.tdd.point.enums.TransactionType.CHARGE;
import static io.hhplus.tdd.point.enums.TransactionType.USE;
import static io.hhplus.tdd.point.exception.ErrorCode.INVALID_CHARGE_POINT;
import static io.hhplus.tdd.point.exception.ErrorCode.NOT_ENOUGH_POINT;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final LockHelper lockHelper;

    //포인트 조회
    public UserPoint getPoint(long id) {
        return userPointRepository.selectById(id);
    }

    //포인트 충전
    public UserPoint charge(long id, long amount) {
        validateAmount(amount, INVALID_CHARGE_POINT, "0보다 작은 포인트는 충전되지 않습니다.");
        return executeWithLockAndUpdate(id, amount, CHARGE, this::addPoints);
    }

    //포인트 사용
    public UserPoint use(long id, long amount) {
        validateAmount(amount, INVALID_CHARGE_POINT, "0보다 작은 포인트는 사용할 수 없습니다.");
        return executeWithLockAndUpdate(id, amount, USE, this::subtractPoints);
    }

    //포인트 내역 조회
    public List<PointHistory> getHistory(long id) {
        return pointHistoryRepository.selectAllByUserId(id);
    }

    private void validateAmount(long amount, ErrorCode errorCode, String errorMessage) {
        if (!isValidPoint(amount)) {
            throw new PointException(errorCode, errorMessage);
        }
    }

    private boolean isValidPoint(long amount) {
        return amount >= 0;
    }

    private UserPoint executeWithLockAndUpdate(long id, long amount, TransactionType type, BiFunction<UserPoint, Long, UserPoint> operation) {
        return lockHelper.executeWithLock(id, () -> {
            UserPoint curUser = userPointRepository.selectById(id);
            UserPoint updatedUser = operation.apply(curUser, amount);
            pointHistoryRepository.insert(id, amount, type, System.currentTimeMillis());
            return updatedUser;
        });
    }

    private UserPoint addPoints(UserPoint user, long amount) {
        return userPointRepository.insertOrUpdate(user.id(), user.point() + amount);
    }

    private UserPoint subtractPoints(UserPoint user, long amount) {
        if (!hasEnoughPoints(user, amount)) {
            throw new PointException(NOT_ENOUGH_POINT, "포인트가 부족합니다.");
        }
        return userPointRepository.insertOrUpdate(user.id(), user.point() - amount);
    }

    private boolean hasEnoughPoints(UserPoint user, long amount) {
        return amount <= user.point();
    }
}
