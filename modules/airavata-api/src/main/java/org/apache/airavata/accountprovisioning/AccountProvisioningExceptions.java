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
package org.apache.airavata.accountprovisioning;

/**
 * Account provisioning exceptions: invalid setup and invalid username.
 */
public final class AccountProvisioningExceptions {

    private AccountProvisioningExceptions() {}

    /**
     * Indicates that some SSHAccountProvisioner setup is missing or incorrect.
     * Message should indicate what is invalid and potentially how to fix it.
     */
    public static class InvalidSetupException extends Exception {
        private static final long serialVersionUID = 1L;

        public InvalidSetupException() {}

        public InvalidSetupException(String message) {
            super(message);
        }

        public InvalidSetupException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidSetupException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * Thrown by {@link SSHAccountProvisioner} when provided userId doesn't map to a local account.
     */
    public static class InvalidUsernameException extends Exception {
        private static final long serialVersionUID = 1L;

        public InvalidUsernameException() {}

        public InvalidUsernameException(String message) {
            super(message);
        }

        public InvalidUsernameException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidUsernameException(Throwable cause) {
            super(cause);
        }
    }
}
