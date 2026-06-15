package com.lyl.domain.problem.exception;

import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;

public class ProblemBankUnavailableException extends BusinessException {

    public ProblemBankUnavailableException() {
        super(ErrorCode.PROBLEM_BANK_UNAVAILABLE);
    }
}
