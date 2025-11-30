package com.test.webtest.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ControllerLoggingAspect {
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    private void restController() {}

    @Around("restController()")
    public Object logController(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();

        String className = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();

        log.info("[CONTROLLER] {}.{}() 호출", className, methodName);

        try{
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[CONTROLLER] {}.{}() 성공 ({} ms)", className, methodName, elapsed);
            return result;
        } catch(Exception ex){
            long elapsed = System.currentTimeMillis() - start;

            log.warn("[CONTROLLER] {}.{}() 실패 ({} ms) - ex={}", className, methodName, elapsed, ex.toString());
            throw ex;
        }
    }
}
