/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.airavata.xregistry.doc;

import java.util.Calendar;

import javax.xml.namespace.QName;

import org.apache.airavata.xregistry.XregistryConstants;


public class DocData {
	public QName resourceID;
	public QName name;
    public String resourcename;
    public String owner;
    public String resourcetype;
    public String resourcedesc;
    public Calendar created;
    public String allowedAction = XregistryConstants.Action.All.toString();
    
    public DocData(QName name, String owner) {
        this.name = name;
        this.owner = owner;
    }
    public DocData(QName name, String owner, String resourceType) {
        this.name = name;
        this.owner = owner;
        this.resourcetype = resourceType;
    }
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(resourceID);
        return super.toString();
    }
	/**
	 * @return the resourceID
	 */
	public QName getResourceID() {
		return resourceID;
	}
	/**
	 * @param resourceID the resourceID to set
	 */
	public void setResourceID(QName resourceID) {
		this.resourceID = resourceID;
	}
	/**
	 * @return the resourcename
	 */
	public String getResourcename() {
		return resourcename;
	}
	/**
	 * @param resourcename the resourcename to set
	 */
	public void setResourcename(String resourcename) {
		this.resourcename = resourcename;
	}
	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}
	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}
	/**
	 * @return the resourcetype
	 */
	public String getResourcetype() {
		return resourcetype;
	}
	/**
	 * @param resourcetype the resourcetype to set
	 */
	public void setResourcetype(String resourcetype) {
		this.resourcetype = resourcetype;
	}
	/**
	 * @return the resourcedesc
	 */
	public String getResourcedesc() {
		return resourcedesc;
	}
	/**
	 * @param resourcedesc the resourcedesc to set
	 */
	public void setResourcedesc(String resourcedesc) {
		this.resourcedesc = resourcedesc;
	}
	/**
	 * @return the created
	 */
	public Calendar getCreated() {
		return created;
	}
	/**
	 * @param created the created to set
	 */
	public void setCreated(Calendar created) {
		this.created = created;
	}
	/**
	 * @return the allowedAction
	 */
	public String getAllowedAction() {
		return allowedAction;
	}
	/**
	 * @param allowedAction the allowedAction to set
	 */
	public void setAllowedAction(String allowedAction) {
		this.allowedAction = allowedAction;
	}
	/**
	 * @return the name
	 */
	public QName getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(QName name) {
		this.name = name;
	}
}

