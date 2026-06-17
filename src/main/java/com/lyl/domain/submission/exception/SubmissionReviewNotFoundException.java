package com.lyl.domain.submission.exception;

import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;

public class SubmissionReviewNotFoundException extends BusinessException {

    public SubmissionReviewNotFoundException() {
        super(ErrorCode.SUBMISSION_REVIEW_NOT_FOUND);
    }
}
