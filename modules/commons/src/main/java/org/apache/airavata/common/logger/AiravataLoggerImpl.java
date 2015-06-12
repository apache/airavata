/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.common.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataLoggerImpl implements AiravataLogger{

    private Logger logger;

    public AiravataLoggerImpl(Class aClass) {
        logger = LoggerFactory.getLogger(aClass);
    }

    public AiravataLoggerImpl(String className) {
        logger = LoggerFactory.getLogger(className);
    }


    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        logger.trace(msg);
    }

    @Override
    public void traceId(String etjId, String msg) {
        logger.trace(getAiravataLogMessage(etjId, msg));
    }

    @Override
    public void trace(String format, Object arg) {
        logger.trace(format, arg);
    }

    @Override
    public void traceId(String etjId, String format, Object arg) {
        logger.trace(getAiravataLogMessage(etjId, format), arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logger.trace(format, arg1, arg2);
    }

    @Override
    public void traceId(String etjId, String format, Object arg1, Object arg2) {
        logger.trace(getAiravataLogMessage(etjId,format), arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        logger.trace(format, arguments);
    }

    @Override
    public void traceId(String etjId, String format, Object... arguments) {
        logger.trace(getAiravataLogMessage(etjId, format), arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        logger.trace(msg, t);
    }

    @Override
    public void traceId(String etjId, String msg, Throwable t) {
        logger.trace(getAiravataLogMessage(etjId, msg), t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    @Override
    public void debugId(String etjId, String msg) {
        logger.debug(getAiravataLogMessage(etjId, msg));
    }

    @Override
    public void debug(String format, Object arg) {
        logger.debug(format, arg);
    }

    @Override
    public void debugId(String etjId, String format, Object arg) {
        logger.debug(getAiravataLogMessage(etjId, format), arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.debug(format, arg1, arg2);
    }

    @Override
    public void debugId(String etjId, String format, Object arg1, Object arg2) {
        logger.debug(getAiravataLogMessage(etjId, format), arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    @Override
    public void debugId(String etjId, String format, Object... arguments) {
        logger.debug(getAiravataLogMessage(etjId, format), arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        logger.debug(msg, t);
    }

    @Override
    public void debugId(String etjId, String msg, Throwable t) {
        logger.debug(getAiravataLogMessage(etjId, msg), t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void infoId(String etjId, String msg) {
        logger.info(getAiravataLogMessage(etjId, msg));
    }

    @Override
    public void info(String format, Object arg) {
        logger.info(format, arg);
    }

    @Override
    public void infoId(String etjId, String format, Object arg) {
        logger.info(getAiravataLogMessage(etjId, format), arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.info(format, arg1, arg2);
    }

    @Override
    public void infoId(String etjId, String format, Object arg1, Object arg2) {
        logger.info(getAiravataLogMessage(etjId, format), arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    @Override
    public void infoId(String etjId, String format, Object... arguments) {
        logger.info(getAiravataLogMessage(etjId, format), arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        logger.info(msg, t);
    }

    @Override
    public void infoId(String etjId, String msg, Throwable t) {
        logger.info(getAiravataLogMessage(etjId, msg), t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    @Override
    public void warnId(String etjId, String msg) {
        logger.warn(getAiravataLogMessage(etjId, msg));
    }

    @Override
    public void warn(String format, Object arg) {
        logger.warn(format, arg);
    }

    @Override
    public void warnId(String etjId, String format, Object arg) {
        logger.warn(getAiravataLogMessage(etjId, format), arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    @Override
    public void warnId(String etjId, String format, Object... arguments) {
        logger.warn(getAiravataLogMessage(etjId, format), arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(format, arg1, arg2);
    }

    @Override
    public void warnId(String etjId, String format, Object arg1, Object arg2) {
        logger.warn(getAiravataLogMessage(etjId, format), arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
    }

    @Override
    public void warnId(String etjId, String msg, Throwable t) {
        logger.warn(getAiravataLogMessage(etjId, msg), t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    @Override
    public void errorId(String etjId, String msg) {
        logger.error(getAiravataLogMessage(etjId, msg));
    }

    @Override
    public void error(String format, Object arg) {
        logger.error(format, arg);
    }

    @Override
    public void errorId(String etjId, String format, Object arg) {
        logger.error(getAiravataLogMessage(etjId, format), arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.error(format, arg1, arg2);
    }

    @Override
    public void errorId(String etjId, String format, Object arg1, Object arg2) {
        logger.error(getAiravataLogMessage(etjId, format), arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    @Override
    public void errorId(String etjId, String format, Object... arguments) {
        logger.error(getAiravataLogMessage(etjId, format), arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

    @Override
    public void errorId(String etjId, String msg, Throwable t) {
        logger.error(getAiravataLogMessage(etjId, msg), t);
    }

    private String getAiravataLogMessage(String etjId, String msg) {
        return new StringBuilder("Id:").append(etjId).append(" : ").append(msg).toString();
    }
}
