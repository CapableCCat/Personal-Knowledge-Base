package com.pkb.common.exception;

import com.pkb.common.result.ResultCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常：抛出后由 {@link GlobalExceptionHandler} 统一转为标准返回体。
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
