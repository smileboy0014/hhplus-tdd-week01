package io.hhplus.tdd.point.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    INVALID_CHARGE_POINT("0 미만의 포인트는 충전되지 않습니다.","400"),
    NOT_ENOUGH_POINT("포인트가 부족합니다.","400"),
    ZERO_POINT("포인트가 0입니다.","400");

    private final String message;
    private final String statusCode;


}
