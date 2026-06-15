package com.lyl.domain.member.exception;

import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;

public class DeletedMemberException extends BusinessException {

    public DeletedMemberException() {
        super(ErrorCode.DELETED_MEMBER);
    }
}
