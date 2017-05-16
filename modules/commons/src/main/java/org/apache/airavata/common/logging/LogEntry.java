/**
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
 */
package org.apache.airavata.common.logging;


import java.lang.*;
import java.util.Map;

public class LogEntry {

    private ServerId serverId;

    private String message;

    private String timestamp;

    private String level;

    private String loggerName;

    private Map<String, String> mdc;

    private String threadName;

    private Exception exception;

    public LogEntry(ServerId serverId, String message, String timestamp, String level, String loggerName, Map<String,
            String> mdc, String threadName, Exception exception) {
        this.serverId = serverId;
        this.message = message;
        this.timestamp = timestamp;
        this.level = level;
        this.loggerName = loggerName;
        this.mdc = mdc;
        this.threadName = threadName;
        this.exception = exception;
    }

    public LogEntry(ServerId serverId, String message, String timestamp, String level, String loggerName, Map<String,
            String> mdc, String threadName) {
        this.serverId = serverId;
        this.message = message;
        this.timestamp = timestamp;
        this.level = level;
        this.loggerName = loggerName;
        this.mdc = mdc;
        this.threadName = threadName;
    }


    public ServerId getServerId() {
        return serverId;
    }

    public void setServerId(ServerId serverId) {
        this.serverId = serverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public Map<String, String> getMdc() {
        return mdc;
    }

    public void setMdc(Map<String, String> mdc) {
        this.mdc = mdc;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
