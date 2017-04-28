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
package org.apache.airavata.credential.store.credential;

import java.io.Serializable;
import java.util.Date;

/**
 * Any audit information related to a credential.
 */
public interface AuditInfo extends Serializable {

    /**
     * Gets the community user associated with the credential.
     * 
     * @return The community user associated with the credential.
     */
    public CommunityUser getCommunityUser();

    /**
     * The portal user associated with the credential.
     * 
     * @return The portal user name.
     */
    public String getPortalUserId();

    /**
     * Get the time which credentials are persisted.
     * 
     * @return Time credentials are persisted.
     */
    public Date getTimePersisted();

}
