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
package org.apache.airavata.core.gfac.provider.utils;

import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BESJob {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private String factoryUrl;

	private JobDefinitionDocument jobDoc;

	public BESJob() {

	}

	public String getFactoryUrl() {
		return factoryUrl;
	}

	public void setFactory(String factoryUrl) {
		this.factoryUrl = factoryUrl;
	}

	public JobDefinitionDocument getJobDoc() {
		return jobDoc;
	}

	public void setJobDoc(JobDefinitionDocument jobDoc) {
		this.jobDoc = jobDoc;
	}

}

