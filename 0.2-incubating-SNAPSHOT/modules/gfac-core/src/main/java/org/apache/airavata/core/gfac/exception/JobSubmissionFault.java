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

package org.apache.airavata.core.gfac.exception;

import org.apache.airavata.core.gfac.provider.Provider;

/**
 * JobSubmissionFault represents an error from Provider which uses submission.
 */
public class JobSubmissionFault extends ProviderException {

    public static final String JOB_CANCEL = "JOB_CANCEL";

    public static final String JOB_FAILED = "JOB_FAILED";

    private String reason;

    public JobSubmissionFault(Provider provider, Throwable cause, String submitHost, String contact, String rsl) {
        super(cause.getMessage(), cause);
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
