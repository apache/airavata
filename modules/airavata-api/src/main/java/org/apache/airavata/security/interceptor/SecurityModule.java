/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.security.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Spring AOP aspect for security interception.
 * Replaces the Guice-based SecurityModule with Spring AOP.
 * Methods annotated with @SecurityCheck will be intercepted by this aspect.
 */
@Aspect
@Component
@ConditionalOnBean(SecurityInterceptor.class)
public class SecurityModule {
    private static final Logger logger = LoggerFactory.getLogger(SecurityModule.class);

    private final SecurityInterceptor securityInterceptor;

    public SecurityModule(SecurityInterceptor securityInterceptor) {
        this.securityInterceptor = securityInterceptor;
        logger.info("Security AOP aspect initialized");
    }

    @Pointcut("@annotation(org.apache.airavata.security.interceptor.SecurityCheck)")
    public void securityCheckPointcut() {
        // Pointcut definition for methods annotated with @SecurityCheck
    }

    @Around("securityCheckPointcut()")
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        // Delegate to SecurityInterceptor which implements MethodInterceptor
        return securityInterceptor.invoke(new MethodInvocationAdapter(joinPoint));
    }

    /**
     * Adapter to convert ProceedingJoinPoint to MethodInvocation for SecurityInterceptor.
     */
    private static class MethodInvocationAdapter implements org.aopalliance.intercept.MethodInvocation {
        private final ProceedingJoinPoint joinPoint;
        private final java.lang.reflect.Method method;

        public MethodInvocationAdapter(ProceedingJoinPoint joinPoint) {
            this.joinPoint = joinPoint;
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            this.method = signature.getMethod();
        }

        @Override
        public Object proceed() throws Throwable {
            return joinPoint.proceed();
        }

        @Override
        public Object getThis() {
            return joinPoint.getThis();
        }

        @Override
        public java.lang.reflect.Method getMethod() {
            return method;
        }

        @Override
        public Object[] getArguments() {
            return joinPoint.getArgs();
        }

        @Override
        public java.lang.reflect.AccessibleObject getStaticPart() {
            // Return the method as the static part - this is required by the interface
            // but not actually used by SecurityInterceptor
            return method;
        }
    }
}
