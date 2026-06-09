package com.lyl.domain.board.exception;

import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;

public class BoardCommentNotFoundException extends BusinessException {

    public BoardCommentNotFoundException() {
        super(ErrorCode.BOARD_COMMENT_NOT_FOUND);
    }
}
