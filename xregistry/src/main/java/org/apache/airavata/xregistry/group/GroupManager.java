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
package org.apache.airavata.xregistry.group;

import java.util.Collection;

import org.apache.airavata.xregistry.XregistryException;

import xregistry.generated.ListSubActorsGivenAGroupResponseDocument.ListSubActorsGivenAGroupResponse.Actor;

public interface GroupManager {

    public boolean isAdminUser(String user);

    public boolean hasUser(String userName);

    public Group getGroup(String name);

    public void createGroup(String newGroup, String description) throws XregistryException;

    public void createUser(String newUser, String description, boolean isAdmin)
            throws XregistryException;

    public void addGrouptoGroup(String groupName, String grouptoAddedName)
            throws XregistryException;

    public void addUsertoGroup(String groupName, String usertoAdded) throws XregistryException;

    public void deleteGroup(String groupID) throws XregistryException;

    public void deleteUser(String userID) throws XregistryException;

    public void removeUserFromGroup(String group, String usertoRemoved) throws XregistryException;

    public void removeGroupFromGroup(String group, String grouptoRemoved) throws XregistryException;

    public String[] listUsers() throws XregistryException;

    public String[] listGroups() throws XregistryException;

    public String[] listGroupsGivenAUser(String user) throws XregistryException;

    public Actor[]  listSubActorsGivenAGroup(String group)throws XregistryException;
    
    public Collection<Group> getGroups();
    
    public User getUser(String user);

}
