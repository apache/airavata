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

package org.apache.airavata.registry.api;

import java.util.Date;

public class ResourceMetadata {
	
	/**
	 * Note: not all the following properties will be available for
	 * a resource
	 */
	
	private AiravataUser createdUser;
	private AiravataUser lastUpdatedUser;
	
	private Date createdDate;
	private Date lastUpdatedDate;
	private String revision;
	
	public AiravataUser getCreatedUser() {
		return createdUser;
	}
	public void setCreatedUser(AiravataUser createdUser) {
		this.createdUser = createdUser;
	}
	public AiravataUser getLastUpdatedUser() {
		return lastUpdatedUser;
	}
	public void setLastUpdatedUser(AiravataUser lastUpdatedUser) {
		this.lastUpdatedUser = lastUpdatedUser;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Date getLastUpdatedDate() {
		return lastUpdatedDate;
	}
	public void setLastUpdatedDate(Date lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}
	public String getRevision() {
		return revision;
	}
	public void setRevision(String revision) {
		this.revision = revision;
	}
}
