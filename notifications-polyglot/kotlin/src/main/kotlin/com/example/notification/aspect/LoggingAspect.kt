package com.example.notification.aspect

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class LoggingAspect {
    private val logger = LoggerFactory.getLogger(LoggingAspect::class.java)

    @Around("execution(* com.example.notification.controller..*(..))")
    fun logAround(joinPoint: ProceedingJoinPoint): Any? {
        val start = System.currentTimeMillis()
        try {
            val result = joinPoint.proceed()
            val executionTime = System.currentTimeMillis() - start
            logger.info("${joinPoint.signature.declaringTypeName}.${joinPoint.signature.name} executed in $executionTime ms")
            return result
        } catch (e: Throwable) {
            logger.error("${joinPoint.signature.declaringTypeName}.${joinPoint.signature.name} failed: ${e.message}")
            throw e
        }
    }
}
