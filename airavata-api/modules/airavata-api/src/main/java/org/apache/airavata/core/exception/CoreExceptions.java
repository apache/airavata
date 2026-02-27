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
package org.apache.airavata.core.exception;

/**
 * Core exceptions for Airavata.
 */
public final class CoreExceptions {

    private CoreExceptions() {}

    public static class AiravataException extends Exception {
        private static final long serialVersionUID = -5665822765183116821L;

        public AiravataException() {}

        public AiravataException(String message, Throwable e) {
            super(message, e);
        }

        public AiravataException(String message) {
            super(message);
        }
    }

    public static class AiravataClientException extends Exception {
        private static final long serialVersionUID = 1L;
        private String parameter;

        public AiravataClientException() {
            super();
        }

        public AiravataClientException(String message) {
            super(message);
        }

        public AiravataClientException(String message, Throwable cause) {
            super(message, cause);
        }

        public AiravataClientException(String message, String parameter) {
            super(message);
            this.parameter = parameter;
        }

        public String getParameter() {
            return parameter;
        }

        public void setParameter(String parameter) {
            this.parameter = parameter;
        }
    }

    public static class AiravataConfigurationException extends AiravataException {
        private static final long serialVersionUID = -9124231436834631249L;

        public AiravataConfigurationException() {}

        public AiravataConfigurationException(String message) {
            this(message, null);
        }

        public AiravataConfigurationException(String message, Throwable e) {
            super(message, e);
        }
    }

    public static class AiravataSystemException extends Exception {
        private static final long serialVersionUID = 1L;

        public AiravataSystemException() {
            super();
        }

        public AiravataSystemException(String message) {
            super(message);
        }

        public AiravataSystemException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AiravataStartupException extends Exception {
        private static final long serialVersionUID = 495204868100143133L;

        public AiravataStartupException() {
            super();
        }

        public AiravataStartupException(String message) {
            super(message);
        }

        public AiravataStartupException(String message, Throwable cause) {
            super(message, cause);
        }

        public AiravataStartupException(Throwable cause) {
            super(cause);
        }

        protected AiravataStartupException(
                String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    public static class InvalidRequestException extends Exception {
        private static final long serialVersionUID = 1L;

        public InvalidRequestException() {
            super();
        }

        public InvalidRequestException(String message) {
            super(message);
        }

        public InvalidRequestException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ApplicationSettingsException extends AiravataException {
        private static final long serialVersionUID = -4901850535475160411L;

        public ApplicationSettingsException(String message) {
            super(message);
        }

        public ApplicationSettingsException(String message, Throwable e) {
            super(message, e);
        }
    }

    public static class TimedOutException extends Exception {
        private static final long serialVersionUID = 1L;

        public TimedOutException() {
            super();
        }

        public TimedOutException(String message) {
            super(message);
        }

        public TimedOutException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
