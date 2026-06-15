package com.lyl.domain.member.exception;

import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;

public class OAuthEmailRequiredException extends BusinessException {

    public OAuthEmailRequiredException() {
        super(ErrorCode.OAUTH_EMAIL_REQUIRED);
    }
}
