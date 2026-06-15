package com.lyl.domain.member.exception;

import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;

public class OAuthLoginCodeException extends BusinessException {

    public OAuthLoginCodeException() {
        super(ErrorCode.OAUTH_LOGIN_CODE_INVALID);
    }
}
