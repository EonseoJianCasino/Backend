package com.test.webtest.global.error.exception;

import com.test.webtest.global.error.model.ErrorCode;
import lombok.Getter;

@Getter
public class AiCallFailedException extends BusinessException{
    private final boolean isTimeout;

    public AiCallFailedException() { 
        super(ErrorCode.AI_CALL_FAILED);
        this.isTimeout = false;
    }
    
    public AiCallFailedException(String msg) { 
        super(ErrorCode.AI_CALL_FAILED, msg);
        this.isTimeout = false;
    }
    
    public AiCallFailedException(Throwable cause) { 
        super(ErrorCode.AI_CALL_FAILED, cause);
        this.isTimeout = false;
    }
    
    public AiCallFailedException(String msg, Throwable cause) { 
        super(ErrorCode.AI_CALL_FAILED, msg, cause);
        this.isTimeout = false;
    }
    
    public AiCallFailedException(Throwable cause, boolean isTimeout) {
        super(ErrorCode.AI_CALL_FAILED, cause);
        this.isTimeout = isTimeout;
    }
    
    public AiCallFailedException(String msg, Throwable cause, boolean isTimeout) {
        super(ErrorCode.AI_CALL_FAILED, msg, cause);
        this.isTimeout = isTimeout;
    }
}
