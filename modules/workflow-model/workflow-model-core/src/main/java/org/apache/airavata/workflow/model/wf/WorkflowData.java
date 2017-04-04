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
package org.apache.airavata.workflow.model.wf;

import org.apache.airavata.workflow.model.exceptions.LazyLoadedDataException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class WorkflowData {
	private String graphXML;
	private boolean published;
	private String name;
	private boolean lazyLoaded;
	
	public WorkflowData() {
	}
	
	public WorkflowData(String name, String graphXml, boolean published) {
		setName(name);
		setGraphXML(graphXml);
		setPublished(published);
		setLazyLoaded(graphXml==null);
	}
	
	public String getGraphXML() throws Exception {
		if (isLazyLoaded()){
			throw new LazyLoadedDataException("This workflow data is lazy loaded. Please use the API to retrieve the workflow graph!!!");
		}
		return graphXML;
	}
	public void setGraphXML(String graphXML) {
		this.graphXML = graphXML;
	}
	public boolean isPublished() {
		return published;
	}
	public void setPublished(boolean published) {
		this.published = published;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public boolean isLazyLoaded() {
		return lazyLoaded;
	}

	private void setLazyLoaded(boolean lazyLoaded) {
		this.lazyLoaded = lazyLoaded;
	}
}
