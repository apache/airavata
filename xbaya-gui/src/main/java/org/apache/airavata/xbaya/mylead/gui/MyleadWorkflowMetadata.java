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

package org.apache.airavata.xbaya.mylead.gui;

public class MyleadWorkflowMetadata {

    private String id;

    private String name;

    private String description;

    private String userDN;

    private String projectID;

    public MyleadWorkflowMetadata() {
    }

    /**
     * Constructs a MyleadWorkflowMetadata.
     * 
     * @param id
     * @param name
     * @param description
     * @param userDN
     * @param projectID
     */
    public MyleadWorkflowMetadata(String id, String name, String description, String userDN, String projectID) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.userDN = userDN;
        this.projectID = projectID;
    }

    /**
     * Returns the id.
     * 
     * @return The id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets id.
     * 
     * @param id
     *            The id to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name.
     * 
     * @return The name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets name.
     * 
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description.
     * 
     * @return The description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets description.
     * 
     * @param description
     *            The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the userDN.
     * 
     * @return The userDN
     */
    public String getUserDN() {
        return this.userDN;
    }

    /**
     * Sets userDN.
     * 
     * @param userDN
     *            The userDN to set.
     */
    public void setUserDN(String userDN) {
        this.userDN = userDN;
    }

    /**
     * Returns the projectID.
     * 
     * @return The projectID
     */
    public String getProjectID() {
        return this.projectID;
    }

    /**
     * Sets projectID.
     * 
     * @param projectID
     *            The projectID to set.
     */
    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

}