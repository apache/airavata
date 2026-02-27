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
package org.apache.airavata.iam.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class providing common service operation patterns.
 * Reduces boilerplate for try-catch-log-rethrow patterns in service classes.
 */
public final class ServiceOperationHelper {

    private static final Logger logger = LoggerFactory.getLogger(ServiceOperationHelper.class);

    private ServiceOperationHelper() {
        // Utility class
    }

    /**
     * Functional interface for operations that return a value and may throw an exception.
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T, E extends Exception> {
        T get() throws E;
    }

    /**
     * Functional interface for operations that don't return a value and may throw an exception.
     */
    @FunctionalInterface
    public interface ThrowingRunnable<E extends Exception> {
        void run() throws E;
    }

    /**
     * Executes an action, logging and rethrowing any exception with a formatted message.
     *
     * @param action         the action to execute
     * @param exceptionType  the type of exception to throw
     * @param messageFormat  the message format (uses String.format)
     * @param args           the format arguments
     * @param <T>            the return type
     * @param <E>            the exception type
     * @return the result of the action
     * @throws E if the action throws an exception
     */
    public static <T, E extends Exception> T execute(
            ThrowingSupplier<T, E> action, Class<E> exceptionType, String messageFormat, Object... args) throws E {
        try {
            return action.get();
        } catch (Exception e) {
            String message = String.format(messageFormat, args);
            logger.error(message, e);
            throw wrapException(exceptionType, message, e);
        }
    }

    /**
     * Executes a void action, logging and rethrowing any exception with a formatted message.
     *
     * @param action         the action to execute
     * @param exceptionType  the type of exception to throw
     * @param messageFormat  the message format (uses String.format)
     * @param args           the format arguments
     * @param <E>            the exception type
     * @throws E if the action throws an exception
     */
    public static <E extends Exception> void executeVoid(
            ThrowingRunnable<E> action, Class<E> exceptionType, String messageFormat, Object... args) throws E {
        try {
            action.run();
        } catch (Exception e) {
            String message = String.format(messageFormat, args);
            logger.error(message, e);
            throw wrapException(exceptionType, message, e);
        }
    }

    /**
     * Executes an action returning boolean, logging and rethrowing any exception.
     *
     * @param action         the action to execute
     * @param exceptionType  the type of exception to throw
     * @param messageFormat  the message format (uses String.format)
     * @param args           the format arguments
     * @param <E>            the exception type
     * @return the boolean result
     * @throws E if the action throws an exception
     */
    public static <E extends Exception> boolean executeBool(
            ThrowingSupplier<Boolean, E> action, Class<E> exceptionType, String messageFormat, Object... args)
            throws E {
        return execute(action, exceptionType, messageFormat, args);
    }

    private static <E extends Exception> E wrapException(Class<E> exceptionType, String message, Exception cause) {
        try {
            // Try constructor with message and cause
            return exceptionType.getConstructor(String.class, Throwable.class).newInstance(message, cause);
        } catch (NoSuchMethodException e1) {
            try {
                // Try constructor with message only
                return exceptionType.getConstructor(String.class).newInstance(message);
            } catch (Exception e2) {
                // Fallback: wrap in RuntimeException
                throw new RuntimeException(message, cause);
            }
        } catch (Exception e) {
            throw new RuntimeException(message, cause);
        }
    }
}
