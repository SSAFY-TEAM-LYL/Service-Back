package com.lyl.domain.board.exception;

import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;

public class BoardPostNotFoundException extends BusinessException {

    public BoardPostNotFoundException() {
        super(ErrorCode.BOARD_POST_NOT_FOUND);
    }
}
