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

import org.apache.airavata.core.gfac.utils.GFacOptions.CurrentProviders;
import org.ogce.schemas.gfac.inca.faults.Job;

public class JobSubmissionFault extends GfacException {
    public JobSubmissionFault(Throwable cause, String submitHost, String contact, String rsl, CurrentProviders api) {
        super(cause, FaultCode.ErrorAtDependentService);
        Job job = Job.Factory.newInstance();
        job.setContact(contact);
        job.setRsl(rsl);
        job.setSubmitHost(submitHost);
        errorActionDocument = createFaultData(job, api.toString(), cause);
    }
}
