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

package org.apache.airavata.registry.api.workflow;

import java.util.Date;

import org.apache.airavata.registry.api.ExecutionErrors;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class ExecutionError {
	public ExecutionErrors.Source source;
	public Date errorTime;
	public String errorCode;
	public String errorMessage;
	public String errorDescription;
	public String errorLocation;
	public String actionTaken;
	private String errorReported;
	private int errorReference;
	
	public ExecutionErrors.Source getSource() {
		return source;
	}
	public void setSource(ExecutionErrors.Source source) {
		this.source = source;
	}
	public Date getErrorTime() {
		return errorTime;
	}
	public void setErrorTime(Date errorTime) {
		this.errorTime = errorTime;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getErrorDescription() {
		return errorDescription;
	}
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
	public String getErrorLocation() {
		return errorLocation;
	}
	public void setErrorLocation(String errorLocation) {
		this.errorLocation = errorLocation;
	}
	public String getActionTaken() {
		return actionTaken;
	}
	public void setActionTaken(String actionTaken) {
		this.actionTaken = actionTaken;
	}
	public String getErrorReported() {
		return errorReported;
	}
	public void setErrorReported(String errorReported) {
		this.errorReported = errorReported;
	}
	public int getErrorReference() {
		return errorReference;
	}
	public void setErrorReference(int errorReference) {
		this.errorReference = errorReference;
	}
	
}
