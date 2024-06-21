package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.common.LockHelper;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

        // 포인트의 유효성 체크
        if (!validPoint(amount)) {
            throw new PointException(INVALID_CHARGE_POINT, "0보다 작은 포인트는 충전되지 않습니다.");
        }

        // 동시성 이슈를 개선하기 위해 lockHelper 를 사용
        return lockHelper.executeWithLock(id, () -> {

            UserPoint curUser = userPointRepository.selectById(id);

            // 포인트 충전
            UserPoint userPoint = userPointRepository.insertOrUpdate(id, curUser.point() + amount);

            // 포인트 충전 내역 추가
            pointHistoryRepository.insert(id, amount, CHARGE, System.currentTimeMillis());

            return userPoint;
        });
    }

    //포인트 사용
    public UserPoint use(long id, long amount) {

        // 포인트의 유효성 체크
        if (!validPoint(amount)) {
            throw new PointException(INVALID_CHARGE_POINT, "0보다 작은 포인트는 사용할 수 없습니다.");
        }
        // 동시성 이슈를 개선하기 위해 lockHelper 를 사용
        return lockHelper.executeWithLock(id, () -> {
            UserPoint curUser = userPointRepository.selectById(id);

            // 포인트가 부족하지 않은지 체크
            if (!isPossibleUse(amount, curUser.point())) {
                throw new PointException(NOT_ENOUGH_POINT, "포인트가 부족합니다.");
            }

            // 포인트 차감
            UserPoint userPoint = userPointRepository.insertOrUpdate(id, curUser.point() - amount);

            // 포인트 차감 내역 추가
            pointHistoryRepository.insert(id, amount, USE, System.currentTimeMillis());

            return userPoint;
        });
    }

    //포인트 내역 조회
    public List<PointHistory> getHistory(long id) {

        return pointHistoryRepository.selectAllByUserId(id);
    }

    // 충전하려는 포인트가 0 미만인지 체크
    private boolean validPoint(long amount) {
        return amount >= 0;
    }

    // 사용하려는 포인트가 남아있는 포인트보다 적은지 체크
    private boolean isPossibleUse(long amount, long userPoint) {
        return amount <= userPoint;
    }
}
