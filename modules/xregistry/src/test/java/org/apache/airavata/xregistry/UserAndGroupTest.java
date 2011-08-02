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

import org.apache.airavata.xregistry.context.GlobalContext;
import org.apache.airavata.xregistry.impl.XregistryImpl;



public class UserAndGroupTest extends AbstractXregistryTestCase {

    
    public void testAddUser() throws Exception{
        GlobalContext globalContext = new GlobalContext(true);
        XregistryImpl registry = new XregistryImpl(globalContext);
        
        String user = "/C=US/O=National Center for Supercomputing Applications/CN=Hemapani Srinath Perera";
        registry.createGroup(user, "group1", "group1");
        registry.createGroup(user, "group2", "group1");
        registry.createUser(user, "user1", "User1");
        registry.createUser(user, "user2", "User1");
        registry.createUser(user, "user3", "User1");
        
        registry.addUsertoGroup(user, "group1", "user1");
        registry.addUsertoGroup(user, "group1", "user2");
        registry.addUsertoGroup(user, "group2", "user3");
//        
        //registry.addAGroupToGroup(user, "group1", "group2");
        
        String[] data = registry.listGroups(user);
        TestUtils.testCantainment(data, "group1");
        TestUtils.testCantainment(data, "group2");
        
        data = registry.listGroupsGivenAUser(user, "user1");
        TestUtils.testCantainment(data, "group1");
        
//        data = registry.listUsersGivenAGroup(user, "group1");
//        TestUtils.testCantainment(data, "user1");
//        TestUtils.testCantainment(data, "user2");
//        
//        
//        registry.removeUserFromGroup(user, "group1", "user1");
//        data = registry.listUsersGivenAGroup(user, "group1");
//        TestUtils.testCantainment(data, "user2");
        
        
        registry.deleteGroup(user, "group1");
        data = registry.listGroups(user);
        TestUtils.printList(data);
        
        registry.deleteGroup(user, "group2");
        registry.deleteUser(user, "user1");
        registry.deleteUser(user, "user2");
        registry.deleteUser(user, "user3");
        
        
        
//        String groupName = "extreme";
//        
//        String user1 = "/C=US/O=National Center for Supercomputing Applications/CN=Suresh Marru";
//        GroupManager manager = new GroupManager(globalContext);
//        //manager.createGroup(groupName, "Group for extreme Users");
//        manager.createUser(user1, user1,false);
//        //manager.addUsertoGroup(groupName, user1);
//        //
//        
//        String[] users;
//        String[] groups = manager.listGroups();
//        for(String group:groups){
//            System.out.print(group+":");
//            users = manager.listUsersGivenAGroup(group);
//            for(String usert:users){
//                System.out.print(usert+ " ");
//            }
//            System.out.println();
//        }
//        
//        users = manager.listUsers();
//        for(String usert:users){
//            System.out.print(usert+":");
//            groups = manager.listGroupsGivenAUser(usert);
//            for(String group:groups){
//                System.out.print(group+" ");
//            }
//            System.out.println();
//        }
    }
    
}

