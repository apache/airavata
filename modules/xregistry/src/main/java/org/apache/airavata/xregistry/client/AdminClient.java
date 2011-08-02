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

package org.apache.airavata.xregistry.client;

import org.apache.airavata.xregistry.XregistryException;
import org.apache.airavata.xregistry.context.GlobalContext;
import org.apache.airavata.xregistry.impl.XregistryPortType;
import org.apache.airavata.xregistry.utils.Utils;

import xregistry.generated.AddGrouptoGroupDocument;
import xregistry.generated.AddUsertoGroupDocument;
import xregistry.generated.CreateGroupDocument;
import xregistry.generated.CreateUserDocument;
import xregistry.generated.DeleteGroupDocument;
import xregistry.generated.DeleteUserDocument;
import xregistry.generated.ListGroupsDocument;
import xregistry.generated.ListGroupsGivenAUserDocument;
import xregistry.generated.ListGroupsGivenAUserResponseDocument;
import xregistry.generated.ListGroupsResponseDocument;
import xregistry.generated.ListSubActorsGivenAGroupDocument;
import xregistry.generated.ListSubActorsGivenAGroupResponseDocument;
import xregistry.generated.ListUsersDocument;
import xregistry.generated.ListUsersResponseDocument;
import xregistry.generated.RemoveGroupFromGroupDocument;
import xregistry.generated.RemoveUserFromGroupDocument;
import xregistry.generated.AddGrouptoGroupDocument.AddGrouptoGroup;
import xregistry.generated.AddUsertoGroupDocument.AddUsertoGroup;
import xregistry.generated.CreateGroupDocument.CreateGroup;
import xregistry.generated.CreateUserDocument.CreateUser;
import xregistry.generated.ListSubActorsGivenAGroupResponseDocument.ListSubActorsGivenAGroupResponse.Actor;
import xregistry.generated.RemoveGroupFromGroupDocument.RemoveGroupFromGroup;
import xregistry.generated.RemoveUserFromGroupDocument.RemoveUserFromGroup;
import xsul.xwsif_runtime.WSIFClient;

public class AdminClient {
    private XregistryPortType proxy;
    public AdminClient(GlobalContext context,String registryServiceWsdlUrl) throws XregistryException{
        WSIFClient client = Utils.createWSIFClient(context, registryServiceWsdlUrl);
        proxy = (XregistryPortType)client.generateDynamicStub(XregistryPortType.class);
    }
    
    
    public void addGrouptoGroup(String groupName, String grouptoAddedName) throws XregistryException {
        AddGrouptoGroupDocument input = AddGrouptoGroupDocument.Factory.newInstance();
        AddGrouptoGroup grouptoGroup = input.addNewAddGrouptoGroup();
        grouptoGroup.setGroup(groupName);
        grouptoGroup.setGroupToAdd(grouptoAddedName);
        proxy.addGrouptoGroup(input);
    }
    public void addUsertoGroup(String groupName, String usertoAdded) throws XregistryException {
        AddUsertoGroupDocument input = AddUsertoGroupDocument.Factory.newInstance();
        AddUsertoGroup UsertoGroup = input.addNewAddUsertoGroup();
        UsertoGroup.setGroup(groupName);
        UsertoGroup.setUserToAdd(usertoAdded);
        proxy.addUsertoGroup(input);
        
    }
    public void createGroup(String newGroupName, String description) throws XregistryException {
        CreateGroupDocument input = CreateGroupDocument.Factory.newInstance();
        CreateGroup UsertoGroup = input.addNewCreateGroup();
        UsertoGroup.setGroupName(newGroupName);
        UsertoGroup.setDescription(description);
        proxy.createGroup(input);
        
    }
    public void createUser(String newUser, String description, boolean isAdmin) throws XregistryException {
        CreateUserDocument input = CreateUserDocument.Factory.newInstance();
        CreateUser usertoGroup = input.addNewCreateUser();
        usertoGroup.setUserName(newUser);
        usertoGroup.setDescription(description);
        proxy.createUser(input);
        
    }
    public void deleteGroup(String groupID) throws XregistryException {
        DeleteGroupDocument input = DeleteGroupDocument.Factory.newInstance();
        input.addNewDeleteGroup().setGroupName(groupID);
        proxy.deleteGroup(input);
        
    }
    public void deleteUser(String userID) throws XregistryException {
        DeleteUserDocument input = DeleteUserDocument.Factory.newInstance();
        input.addNewDeleteUser().setUserName(userID);
        proxy.deleteUser(input);
        
    }
 
    public String[] listGroups() throws XregistryException {
        ListGroupsDocument input =  ListGroupsDocument.Factory.newInstance();
        input.addNewListGroups();
        ListGroupsResponseDocument responseDocument = proxy.listGroups(input);
        
        return responseDocument.getListGroupsResponse().getGroupArray(); 
    }
    public String[] listGroupsGivenAUser(String user) throws XregistryException {
        ListGroupsGivenAUserDocument input =  ListGroupsGivenAUserDocument.Factory.newInstance();
        input.addNewListGroupsGivenAUser().setUserName(user);
        ListGroupsGivenAUserResponseDocument responseDocument = proxy.listGroupsGivenAUser(input);
        
        return responseDocument.getListGroupsGivenAUserResponse().getGroupArray(); 
    }
    public String[] listUsers() throws XregistryException {
        ListUsersDocument input =  ListUsersDocument.Factory.newInstance();
        input.addNewListUsers();
        ListUsersResponseDocument responseDocument = proxy.listUsers(input);
        
        return responseDocument.getListUsersResponse().getUserArray(); 
    }
    public Actor[] listSubActorsGivenAGroup(String group) throws XregistryException {
        ListSubActorsGivenAGroupDocument input =  ListSubActorsGivenAGroupDocument.Factory.newInstance();
        input.addNewListSubActorsGivenAGroup().setGroup(group);
        ListSubActorsGivenAGroupResponseDocument responseDocument = proxy.listSubActorsGivenAGroup(input);
        return responseDocument.getListSubActorsGivenAGroupResponse().getActorArray(); 
    }
    public void removeGroupFromGroup(String group, String grouptoRemoved) throws XregistryException {
        RemoveGroupFromGroupDocument input = RemoveGroupFromGroupDocument.Factory.newInstance();
        RemoveGroupFromGroup removeGroupFromGroup = input.addNewRemoveGroupFromGroup();
        removeGroupFromGroup.setMasterGroup(group);
        removeGroupFromGroup.setGroupToremove(grouptoRemoved);
        proxy.removeGroupFromGroup(input);
    }
    public void removeUserFromGroup(String group, String usertoRemoved) throws XregistryException {
        RemoveUserFromGroupDocument input = RemoveUserFromGroupDocument.Factory.newInstance();
        RemoveUserFromGroup removeUserFromGroup = input.addNewRemoveUserFromGroup();
        removeUserFromGroup.setGroupName(group);
        removeUserFromGroup.setUserName(usertoRemoved);
        proxy.removeUserFromGroup(input);
    }
  }

