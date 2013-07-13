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
package org.apache.airavata.gfac;

import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.provider.GFacProviderException;

// TODO review this class - Not sure some of the attributes are actually
// needed
public class JobSubmissionFault extends GFacProviderException{

    // TODO why we need following ?
    //public static final String JOB_CANCEL = "JOB_CANCEL";

    //public static final String JOB_FAILED = "JOB_FAILED";

    private String reason;
    private String contact;
    private int gramErrorCode;
    private String rsl;
    private String host;

    public JobSubmissionFault(Throwable cause, String submitHost, String contact, String rsl,
                              JobExecutionContext jobExecutionContext, String reason, int errorCode) {
        super(cause.getMessage(), cause,jobExecutionContext);

        this.host = submitHost;
        this.contact = contact;
        this.rsl = rsl;
        this.reason = reason;
        this.gramErrorCode = errorCode;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    // TODO why we need this ?
    public void sendFaultNotification(String message,
			JobExecutionContext jobExecutionContext, Exception e,
			String... additionalExceptiondata) {
		
	}

    public String getReason() {
        return reason;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public int getGramErrorCode() {
        return gramErrorCode;
    }

    public void setGramErrorCode(int gramErrorCode) {
        this.gramErrorCode = gramErrorCode;
    }

    public String getRsl() {
        return rsl;
    }

    public void setRsl(String rsl) {
        this.rsl = rsl;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
