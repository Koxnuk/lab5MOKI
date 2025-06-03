package com.example.currency.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Before("execution(* com.example.currency.controller.*.*(..))")
    public void logBeforeControllerMethods(JoinPoint joinPoint) {
        System.out.println("Entering method: " + joinPoint.getSignature().getName() + " with arguments: " + joinPoint.getArgs());
    }

    @AfterThrowing(pointcut = "execution(* com.example.currency.controller.*.*(..))", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        System.out.println("Exception in method: " + joinPoint.getSignature().getName() + " with message: " + e.getMessage());
    }
}