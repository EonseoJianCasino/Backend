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
public class ServiceLoggingAspect {
    @Pointcut("@annotation(monitored)")
    private void monitoredMethods(Monitored monitored) {}

    @Around("monitoredMethods(monitored)")
    public Object logService(ProceedingJoinPoint pjp, Monitored monitored) throws Throwable{
        long start = System.currentTimeMillis();

        String tag = monitored.value();
        String className = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();

        if (!tag.isBlank()) {
            log.info("[SERVICE tag = {} {}.{}() 시작", tag, className, methodName);
        } else {
            log.info("[SERVICE] {}.{}() 시작", className, methodName);
        }

        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[SERVICE] {}.{}() 성공 ({} ms)", className, methodName, elapsed);
            return result;
        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[SERVICE] {}.{}() 예외 ({} ms) - ex={}", className, methodName, elapsed, ex.toString());
            throw ex;
        }
    }
}
