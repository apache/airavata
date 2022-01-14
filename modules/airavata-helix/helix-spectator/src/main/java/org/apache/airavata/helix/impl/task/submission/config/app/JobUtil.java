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
 */
package org.apache.airavata.helix.impl.task.submission.config.app;

import org.apache.airavata.model.status.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobUtil {
	private static final Logger log = LoggerFactory.getLogger(JobUtil.class);

	public static JobState getJobState(String status) {
		log.info("parsing the job status returned : " + status);
		if (status != null) {
			if ("C".equals(status) || "CD".equals(status) || "E".equals(status) || "CG".equals(status) || "DONE".equals(status)) {
				return JobState.COMPLETE;
//			} else if ("H".equals(status) || "h".equals(status)) {
//				return JobState.HELD;
			} else if ("Q".equals(status) || "qw".equals(status) || "PEND".equals(status)) {
				return JobState.QUEUED;
			} else if ("R".equals(status) || "CF".equals(status) || "r".equals(status) || "RUN".equals(status)) {
				return JobState.ACTIVE;
//			} else if ("T".equals(status)) {
//				return JobState.HELD;
			} else if ("W".equals(status) || "PD".equals(status) || "I".equals(status)) {
				return JobState.QUEUED;
			} else if ("S".equals(status) || "PSUSP".equals(status) || "USUSP".equals(status) || "SSUSP".equals(status)) {
				return JobState.SUSPENDED;
			} else if ("CA".equals(status) || "X".equals(status)) {
				return JobState.CANCELED;
			} else if ("F".equals(status) || "NF".equals(status) || "TO".equals(status) || "EXIT".equals(status)) {
				return JobState.FAILED;
			} else if ("PR".equals(status) || "Er".equals(status)) {
				return JobState.FAILED;
			} else if ("U".equals(status) || ("UNKWN".equals(status))) {
				return JobState.UNKNOWN;
			}
		}
		return JobState.UNKNOWN;
	}
}
