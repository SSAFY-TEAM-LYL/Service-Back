package com.lyl.domain.member.exception;

import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;

public class OAuthAccountConflictException extends BusinessException {

    public OAuthAccountConflictException() {
        super(ErrorCode.OAUTH_ACCOUNT_CONFLICT);
    }
}
