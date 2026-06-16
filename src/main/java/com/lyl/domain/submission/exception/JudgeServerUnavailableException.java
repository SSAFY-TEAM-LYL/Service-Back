package com.lyl.domain.submission.exception;

import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;

public class JudgeServerUnavailableException extends BusinessException {

    public JudgeServerUnavailableException() {
        super(ErrorCode.JUDGE_SERVER_UNAVAILABLE);
    }
}
