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
package org.apache.airavata.gfac.impl.task.utils.bes;

import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStatusType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import java.io.Serializable;

public class ActivityInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private EndpointReferenceType activityEPR;
	
	private ActivityStatusType activityStatusDoc;
	

	public EndpointReferenceType getActivityEPR() {
		return activityEPR;
	}
	public void setActivityEPR(EndpointReferenceType activityEPR) {
		this.activityEPR = activityEPR;
	}
	public ActivityStatusType getActivityStatus() {
		return activityStatusDoc;
	}
	public void setActivityStatusDoc(ActivityStatusType activityStatusDoc) {
		this.activityStatusDoc = activityStatusDoc;
	}
	
}
