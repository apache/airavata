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
 * Registry-related exceptions. Use nested classes for type-safe catch and clarity.
 */
public final class RegistryExceptions {

    private RegistryExceptions() {}

    public static class AppRegistryException extends Exception {
        private static final long serialVersionUID = -2849422320139467602L;

        public AppRegistryException(Throwable e) {
            super(e);
        }

        public AppRegistryException(String message) {
            super(message, null);
        }

        public AppRegistryException(String message, Throwable e) {
            super(message, e);
        }
    }

    public static class ExperimentRegistryException extends Exception {
        private static final long serialVersionUID = -2849422320139467602L;

        public ExperimentRegistryException(Throwable e) {
            super(e);
        }

        public ExperimentRegistryException(String message) {
            super(message, null);
        }

        public ExperimentRegistryException(String message, Throwable e) {
            super(message, e);
        }
    }

    /**
     * Exception thrown by registry service operations.
     */
    public static class RegistryException extends Exception {
        private static final long serialVersionUID = 1L;

        public RegistryException() {
            super();
        }

        public RegistryException(String message) {
            super(message);
        }

        public RegistryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ReplicaRegistryException extends RegistryException {

        public ReplicaRegistryException(Throwable e) {
            super(e.getMessage(), e);
        }

        public ReplicaRegistryException(String message) {
            super(message, null);
        }

        public ReplicaRegistryException(String message, Throwable e) {
            super(message, e);
        }
    }

    public static class WorkflowRegistryException extends Exception {
        private static final long serialVersionUID = -2849422320139467602L;

        public WorkflowRegistryException(Throwable e) {
            super(e);
        }

        public WorkflowRegistryException(String message) {
            super(message, null);
        }

        public WorkflowRegistryException(String message, Throwable e) {
            super(message, e);
        }
    }
}
