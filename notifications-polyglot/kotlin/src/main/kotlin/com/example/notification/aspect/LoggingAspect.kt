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
        val methodName = joinPoint.signature.name
        val start = System.currentTimeMillis()
        
        return try {
            val result = joinPoint.proceed()
            val duration = System.currentTimeMillis() - start
            logger.debug("Request processed: {} ({}ms)", methodName, duration)
            result
        } catch (e: Exception) {
            logger.error("Request failed: {} - {}", methodName, e.message)
            throw e
        }
    }
}
