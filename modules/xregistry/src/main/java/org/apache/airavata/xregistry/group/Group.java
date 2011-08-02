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
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.airavata.xregistry.utils.Utils;


/**
 * Represent a Group, group may have Users and child groups. The symantics of child groups are resolved by 
 * assuming of foo is in G1 and G1 is in G2, it is same as foo is in G1 (G1,G2 are groups and foo is a user).
 */

public class Group {
    private final String name;
    private Map<String, String> users = new ConcurrentHashMap<String, String>();
    private Vector<Group> childGroups = new Vector<Group>();
    private Vector<Group> parentGroups = new Vector<Group>();
    private Map<String,String> authorizedResources = new ConcurrentHashMap<String, String>();
    
    
    public Group(final String name) {
        this.name = name;
    }

    public void addUser(String user){
        users.put(user, user);
    }
    
    public void addGroup(Group group){
        childGroups.add(group);
        group.addParentGroup(this);
    }

    public void removeGroup(Group group){
        childGroups.remove(group);
        group.removeParentGroup(this);
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Group){
            return name.equals(((Group)obj).name);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Check does this user authorized to access resources assigned to this group. It is
     * decided by checking this group has give user cantained at some level. 
     * @param user
     * @param traversal
     * @return
     */
    public boolean isAuthorized(String user,Traversal traversal){
        if(hasUser(user)){
          return true;
        }else{
            for(Group group:childGroups){
                if(!traversal.hasVisited(group)){
                    traversal.addVisited(group);
                    if(group.isAuthorized(user, traversal)){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    
    public boolean hasGroup(String groupName){
        for(Group group:childGroups){
            if(group.getName().equals(groupName)){
                return true;
            }
        }
        return false;
    }
    
    public boolean hasUser(String user){
        for(String auser:users.values()){
            if(Utils.isSameDN(auser,user)){
                return true;
            }
        }
        return false;
    }
    
    public void removeUser(String userID){
        users.remove(userID);
    }
    

    public String getName() {
        return name;
    }
    
    public void addParentGroup(Group parentGroup){
        parentGroups.add(parentGroup);
    }
    
    public void removeParentGroup(Group parentGroup){
        parentGroups.remove(parentGroup);
    }
    
    public Collection<Group> getParentGroups(){
        return parentGroups;
    }
    
    public void addAuthorizedResource(String resourceID,String action){
        authorizedResources.put(resourceID,action);
    }
    
    
    public boolean removeAuthorizedResource(String resourceID){
        return authorizedResources.remove(resourceID) != null;
    }
    
    
    public Map<String,String> getAuthorizedResources(){
        return authorizedResources;
    }
}

