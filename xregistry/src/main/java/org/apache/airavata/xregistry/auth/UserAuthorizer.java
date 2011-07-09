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

package org.apache.airavata.xregistry.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.airavata.xregistry.XregistryConstants;
import org.apache.airavata.xregistry.group.Group;
import org.apache.airavata.xregistry.group.GroupManager;
import org.apache.airavata.xregistry.group.User;


/**
 * This class maintains a list of authorized resources accessible to a give user.
 * Usually other componets may create a Object of this type per user and keep it around. However this 
 * does not reflect changes done to autorizations after object is created.   
 * @author Srinath Perera(hperera@cs.indiana.edu)
 */

public class UserAuthorizer {
    private GroupManager groupManager;
    private Map<String,String> accessibleResource;
    
    public UserAuthorizer(GroupManager groupManager,String userName){
        this.groupManager = groupManager;
        accessibleResource = new HashMap<String,String>();
        
        List<Group> allGroupsForUser = findAllGroupsUserIn(userName);
        for(Group group:allGroupsForUser){
            Map<String,String> authorizedResource = group.getAuthorizedResources();
            for(String resourceID:authorizedResource.keySet()){
                addAccessibleResource(resourceID, authorizedResource.get(resourceID));
            }
        }
        
        User user = groupManager.getUser(userName );
        Map<String,String> authorizedResource =user.getAuthorizedResources();
        for(String resourceID:authorizedResource.keySet()){
            addAccessibleResource(resourceID, authorizedResource.get(resourceID));
        }
    }
    
    
    public void addAccessibleResource(String resourceID,String action){
        String oldAction = accessibleResource.get(resourceID);
        if(oldAction == null){
            accessibleResource.put(resourceID, action);
        }else if(oldAction.equals(XregistryConstants.Action.Write.toString()) && action.equals(XregistryConstants.Action.All.toString()) ){
            accessibleResource.put(resourceID, action);
        }else if(oldAction.equals(XregistryConstants.Action.Read.toString()) && action.equals(XregistryConstants.Action.Write.toString()) ){
            accessibleResource.put(resourceID, action);
        }
    }
    
    
    
    public String isAuthorized(String resourceID){
        return accessibleResource.get(resourceID);
    }
    
    /**
     * Given a user, find all groups this user is part of. e.g. if foo is the user and foo is in G1 and G1 is in G2,
     * this method will return G1 and G2 give foo. 
     * @param user
     * @return
     */
    private List<Group> findAllGroupsUserIn(String user){
        Collection<Group> groups = groupManager.getGroups();
        Set<Group> groupsUserIsIn =  new HashSet<Group>();
        
        Map<String,Group> groupsAlredySeen = new HashMap<String, Group>();
        for(Group group:groups){
            if(group.hasUser(user)){
                groupsUserIsIn.add(group);
                groupsAlredySeen.put(group.getName(), group);
            }
        }
        
        List<Group> inheritedList = new ArrayList<Group>();
        inheritedList.addAll(groupsUserIsIn);
        //Groups in Inherited list will be added to  groupsAlredySeen with in the findAllGroupsGroupIn() method.
        //So we do not add them first hand
        for(Group group:groupsUserIsIn){
            findAllGroupsGroupIn(group, inheritedList, groupsAlredySeen);
        }
        return inheritedList;
    }
    
    /**
     * Given a group, find all parent groups of this group. Code does it by recursively finding parent groups
     * @param group
     * @param collectedGroups
     * @param groupsAlredySeen
     */
    public static void findAllGroupsGroupIn(Group group,List<Group> collectedGroups,Map<String,Group> groupsAlredySeen){
        Collection<Group> parents = group.getParentGroups();
        for(Group parentGroup:parents){
            if(!groupsAlredySeen.containsKey(parentGroup)){
                groupsAlredySeen.put(parentGroup.getName(), group);
                collectedGroups.add(parentGroup);
                findAllGroupsGroupIn(parentGroup, collectedGroups, groupsAlredySeen);    
            }
            
        }
    }
}

