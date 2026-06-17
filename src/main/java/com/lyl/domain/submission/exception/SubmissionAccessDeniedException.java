package com.lyl.domain.submission.exception;

import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;

public class SubmissionAccessDeniedException extends BusinessException {

    public SubmissionAccessDeniedException() {
        super(ErrorCode.ACCESS_DENIED);
    }
}
