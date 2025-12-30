package com.test.webtest.global.error.exception;

import com.test.webtest.global.error.model.ErrorCode;

public class SecurityScanFailedException extends BusinessException{
    public SecurityScanFailedException() {
        super(ErrorCode.SECURITY_SCAN_FAILED);
    }

    public SecurityScanFailedException(String msg) {
        super(ErrorCode.SECURITY_SCAN_FAILED, msg);
    }

    public SecurityScanFailedException(String msg, Throwable cause) {
        super(ErrorCode.SECURITY_SCAN_FAILED, cause);
    }
}
