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

package org.apache.airavata.xregistry.cap;

import java.sql.Timestamp;

import org.apache.airavata.xregistry.XregistryException;

import xregistry.generated.CapabilityToken;

public interface CapabilityRegistry {
    /**
     * This is Capabilities Registry can understand, this and the next method both goes to same table. 
     * @param user - User who invoked this service,this is taken from SSL connection or massage signature. If there is none, asynchrous user will be passed here
     * @param resourceID - resource capability pointed to
     * @param actor - Actor who was authorized with this capability
     * @param actorType - Does actor is a user or a group
     * @param action - action actor is authorized to do - READ/WRITE/user defined
     */
    public void addCapability(String user,String resourceID,String actor,boolean actorType,String action)throws XregistryException;
    /**
     * Register user created assertions 
     * @param user - User who invoked this service
     * @param resourceID - resource capability pointed to
     * @param actor - Actor who was authorized with this capability
     * @param actorType - Does actor is a user or a group
     * @param action - action actor is authorized to do - READ/WRITE/user defined
     */
    public void addCapability(String user,String resourceID,String actor,boolean actorType,String action,String assertions,
            Timestamp notbefore, Timestamp notafter)throws XregistryException;
    
    /**
     * This is the search method for capabilities, the null values can be defined for most values to allow a search. But one of the 
     * resourceID or actor must present
     * @param user
     * @param resourceID
     * @param actor and actorType - Both or non must precent .. can be Null
     * @param action 
     * @return
     * @throws XregistryException
     */
    public CapabilityToken[] getCapability(String user,String resourceID,String actor,boolean actorType,String action)throws XregistryException;
    
    public void removeCapability(String user,String resourceID,String actor) throws XregistryException;
    
    
    /**
     * Check does given user can perform given action on given resource.
     * @param resourceID
     * @param actor
     * @param action
     * @return
     * @throws XregistryException
     */
    public boolean isAuthorizedToAcsses(String resourceID, String actor, String action) throws XregistryException;

}

