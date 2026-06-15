package com.lyl.domain.problem.exception;

import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;

public class ProblemNotFoundException extends BusinessException {

    public ProblemNotFoundException() {
        super(ErrorCode.PROBLEM_NOT_FOUND);
    }
}
