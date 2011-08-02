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
package org.apache.airavata.xregistry;

import java.sql.Timestamp;

import xregistry.generated.CapabilityToken;


public interface Xregistry {
    public String registerConcreteWsdl(String user,String wsdlAsStr,int lifetimeAsSeconds)throws XregistryException;
    public String getConcreateWsdl(String user,String wsdlQName)throws XregistryException;
    public void removeConcreteWsdl(String user,String wsdlQName)throws XregistryException;
    public String getAbstractWsdl(String user,String wsdlQName) throws XregistryException;
    
    public String registerServiceDesc(String user,String serviceMapAsStr,String abstractWsdlAsString)throws XregistryException;
    public void removeServiceDesc(String user,String serviceQName)throws XregistryException;
    public String getServiceDesc(String user,String serviceQName)throws XregistryException;
    
    public String registerHostDesc(String user,String hostDescAsStr)throws XregistryException;
    public String getHostDesc(String user,String hostName)throws XregistryException;
    public void removeHostDesc(String user,String hostName)throws XregistryException;
    
    public String registerAppDesc(String user,String appDescAsStr)throws XregistryException;
    public String getAppDesc(String user,String appQName,String hostName)throws XregistryException;
    public void removeAppDesc(String user,String appQName,String hostName)throws XregistryException;
    
    public String[] findServiceInstance(String user,String serviceName)throws XregistryException;
    public String[] findServiceDesc(String user,String serviceName)throws XregistryException;
    public String[][] findAppDesc(String user,String query) throws XregistryException;
    public String[] findHosts(String user,String hostName)throws XregistryException;
    
    public String[] app2Hosts(String user,String appName)throws XregistryException;
    
    public void createUser(String user,String newUser,String description)throws XregistryException;
    public void createGroup(String user,String newGroup,String description)throws XregistryException;
    public void addUsertoGroup(String user,String group,String usertoAdded)throws XregistryException;
    public void removeUserFromGroup(String user,String group,String usertoRemoved)throws XregistryException;
    public void removeGroupFromGroup(String user,String group,String grouptoRemoved)throws XregistryException;
    public void deleteUser(String user,String userID)throws XregistryException;
    public void deleteGroup(String user,String groupID)throws XregistryException;
    public String[] listUsers(String user)throws XregistryException;
    public String[] listGroups(String user)throws XregistryException;
    public String[] listGroupsGivenAUser(String user,String targetUser)throws XregistryException;
    public String[] listSubActorsGivenAGroup(String user,String group)throws XregistryException;
    
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
    public void addCapability(String user,String resourceID,String actor,boolean actorType,String action,String assertions,Timestamp notbefore,Timestamp notafter)throws XregistryException;
    public CapabilityToken[] getCapability(String user,String resourceID,String actor,boolean actorType,String action)throws XregistryException;
    public void removeCapability(String user,String resourceID,String actor) throws XregistryException;
    public boolean isAuthorizedToAcsses(String user,String resourceID,String actor,String action) throws XregistryException;
    

}

