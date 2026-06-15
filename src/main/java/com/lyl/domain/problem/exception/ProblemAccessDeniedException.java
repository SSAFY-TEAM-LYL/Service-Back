package com.lyl.domain.problem.exception;

import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;

public class ProblemAccessDeniedException extends BusinessException {

    public ProblemAccessDeniedException() {
        super(ErrorCode.ACCESS_DENIED);
    }
}
