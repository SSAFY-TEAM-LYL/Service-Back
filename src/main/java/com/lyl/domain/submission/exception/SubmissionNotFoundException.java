package com.lyl.domain.submission.exception;

import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;

public class SubmissionNotFoundException extends BusinessException {

    public SubmissionNotFoundException() {
        super(ErrorCode.SUBMISSION_NOT_FOUND);
    }
}
