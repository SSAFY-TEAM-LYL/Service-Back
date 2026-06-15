package com.lyl.domain.problem.exception;

import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;

public class ProblemPublicationNotFoundException extends BusinessException {

    public ProblemPublicationNotFoundException() {
        super(ErrorCode.PROBLEM_PUBLICATION_NOT_FOUND);
    }
}
