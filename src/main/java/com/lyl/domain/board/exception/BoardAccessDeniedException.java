package com.lyl.domain.board.exception;

import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;

public class BoardAccessDeniedException extends BusinessException {

    public BoardAccessDeniedException() {
        super(ErrorCode.ACCESS_DENIED);
    }
}
