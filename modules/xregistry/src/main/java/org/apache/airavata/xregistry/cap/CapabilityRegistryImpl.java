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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.airavata.xregistry.SQLConstants;
import org.apache.airavata.xregistry.XregistryConstants;
import org.apache.airavata.xregistry.XregistryConstants.Action;
import org.apache.airavata.xregistry.XregistryException;
import org.apache.airavata.xregistry.context.GlobalContext;
import org.apache.airavata.xregistry.group.Group;
import org.apache.airavata.xregistry.group.GroupManager;
import org.apache.airavata.xregistry.group.Traversal;
import org.apache.airavata.xregistry.group.User;
import org.apache.airavata.xregistry.utils.Utils;

import xregistry.generated.CapabilityToken;
import xsul.MLogger;

public class CapabilityRegistryImpl implements CapabilityRegistry,SQLConstants{
    protected static MLogger log = MLogger.getLogger(XregistryConstants.LOGGER_NAME);
    private GroupManager groupManager;
    private GlobalContext globalContext;
    
    
    
    public CapabilityRegistryImpl(GlobalContext context,GroupManager groupManager) {
        this.groupManager = groupManager;
        this.globalContext = context;
    }
    

    public void addCapability(String user, String resourceID, String actor, boolean isUser, String action,String assertions, 
            Timestamp notbefore, Timestamp notafter) throws XregistryException {
        Connection connection = globalContext.createConnection();
        if(isUser){
            actor = Utils.canonicalizeDN(actor);    
        }
        
        try {
            //owner ,resourceid,allowed_actor,isUser,assertions,notbefore,notafter
            PreparedStatement statement = connection.prepareStatement(ADD_FULL_CAPABILITY_SQL);
            statement.setString(1,user);
            statement.setString(2, resourceID);
            statement.setString(3, actor);
            statement.setBoolean(4,isUser);
            statement.setString(5, assertions);
            statement.setTimestamp(6, notbefore);
            statement.setTimestamp(7, notafter);
            statement.executeUpdate();
            log.info("Execuate SQL "+ statement);

            if(isUser){
                User userObj = groupManager.getUser(actor);
                if(userObj != null){
                    userObj.addAuthorizedResource(resourceID, action);
                }else{
                    log.info("User "+ action + " realted to capability is not found");
                }
            }else{
                Group groupObj = groupManager.getGroup(actor);
                if(groupObj != null){
                    groupObj.addAuthorizedResource(resourceID, action);
                }else{
                    log.info("Group "+ action + " realted to capability is not found");
                }
            }
        } catch (SQLException e) {
            throw new XregistryException(e);
        }finally{
            globalContext.closeConnection(connection);
        }
        
    }

    public void addCapability(String user, String resourceID, String actor, boolean isUser, String action) throws XregistryException {
        Connection connection = globalContext.createConnection();
        if(isUser){
            actor = Utils.canonicalizeDN(actor);    
        }
        try {
            PreparedStatement statement = connection.prepareStatement(ADD_CAPABILITY_SQL);
            statement.setString(1,user);
            statement.setString(2, resourceID);
            statement.setString(3, actor);
            statement.setBoolean(4,isUser);
            statement.setString(5, action);
            log.info("Execuate SQL "+ statement);
            statement.executeUpdate();
            
            if(isUser){
                User userObj = groupManager.getUser(actor);
                if(userObj != null){
                    userObj.addAuthorizedResource(resourceID, action);
                }else{
                    log.info("User "+ action + " realted to capability is not found");
                }
            }else{
                Group groupObj = groupManager.getGroup(actor);
                if(groupObj != null){
                    groupObj.addAuthorizedResource(resourceID, action);
                }else{
                    log.info("Group "+ action + " realted to capability is not found");
                }
            }
        } catch (SQLException e) {
            throw new XregistryException(e);
        }finally{
            globalContext.closeConnection(connection);
        }
        
    }


    public CapabilityToken[] getCapability(String user, String resourceID, String actor, boolean actorType, String searchAction) throws XregistryException {
        Connection connection = globalContext.createConnection();
        
        //if actor is a user, canonicalize the name
        if(actor != null && groupManager.hasUser(actor)){
            actor = Utils.canonicalizeDN(actor);    
        }
        
        try {
            String sql; 
            PreparedStatement statement;
            if(resourceID != null && actor != null){
                sql = GET_CAPABILITIES_BY_ACTOR_AND_RESOURCE;
                statement = connection.prepareStatement(sql);
                statement.setString(1, resourceID);
                statement.setString(2, actor);
            }else if(resourceID != null){
                sql = GET_CAPABILITIES_BY_RESOURCE;
                statement = connection.prepareStatement(sql);
                statement.setString(1, resourceID);
            }else if(actor != null){
                sql = GET_CAPABILITIES_BY_ACTOR;
                statement = connection.prepareStatement(sql);
                statement.setString(1, actor);
            }else{
                throw new XregistryException("At least one of the Actor or resource ID must not be null");
            }
            log.info("Execuate SQL "+ statement);
            ResultSet results = statement.executeQuery();
            
            ArrayList<CapabilityToken> tokenList = new ArrayList<CapabilityToken>();
            while(results.next()){
                CapabilityToken captoken = CapabilityToken.Factory.newInstance();
                captoken.setActor(results.getString(ALLOWED_ACTOR));
                captoken.setResourceID(results.getString(RESOURCE_ID));
                captoken.setActorType(results.getBoolean(IS_USER));
                
                String capActionStr = results.getString(ACTION_TYPE);
                Action capAction = Action.All;
                if(capActionStr != null){
                    capAction = Action.valueOf(capActionStr);
                }
                
                //If use has give a search action, skip everything does not match
                if(searchAction != null){
                    if(!capAction.equals(Action.All) && !capAction.equals(searchAction)){
                        break;
                    }
                }
                captoken.setAction(capAction.toString());
                String assertion = results.getString(ASSERTION);
                if(assertion != null){
                    captoken.setAssertions(assertion);
                }
                
                
                //TODO it throws a exception when I try to get null time stamp
//                Timestamp notbefore = results.getTimestamp(NOT_BEFORE);
//                if(notbefore != null){
//                    XmlDateTime notbeforeXmlbeansVal = XmlDateTime.Factory.newInstance(); 
//                    notbeforeXmlbeansVal.setDateValue(notbefore);
//                    captoken.xsetNotbefore(notbeforeXmlbeansVal);
//                }
//                
//                Timestamp notAfter = results.getTimestamp(NOT_AFTER);
//                if(notAfter != null){
//                    XmlDateTime notAfterXmlbeansVal = XmlDateTime.Factory.newInstance(); 
//                    notAfterXmlbeansVal.setDateValue(notAfter);
//                    captoken.xsetNotbefore(notAfterXmlbeansVal);
//                }
                
                tokenList.add(captoken);
            }
            
            return tokenList.toArray(new CapabilityToken[0]);
        } catch (SQLException e) {
            throw new XregistryException(e);
        }finally{
            globalContext.closeConnection(connection);
        }
    }

    public void removeCapability(String user, String resourceID, String actor) throws XregistryException {
        Connection connection = globalContext.createConnection();
        //if actor is a user, canonicalize the name
        if(groupManager.hasUser(actor)){
            actor = Utils.canonicalizeDN(actor);    
        }
        try {
            PreparedStatement statement = connection.prepareStatement(REMOVE_CAPABILITY_SQL);
            statement.setString(1,actor);
            statement.setString(2, resourceID);
            statement.executeUpdate();
            log.info("Execuate SQL "+ statement);
            
            User userObj = groupManager.getUser(actor);
            if(userObj != null){
                userObj.removeAuthorizedResource(resourceID);
            }else{
                Group groupObj = groupManager.getGroup(actor);
                if(groupObj != null){
                    groupObj.removeAuthorizedResource(resourceID);
                }
            }
        } catch (SQLException e) {
            throw new XregistryException(e);
        }finally{
            globalContext.closeConnection(connection);
        }
        
    }


    public boolean isAuthorizedToAcsses(String resourceID, String actor, String action) throws XregistryException {
        Connection connection = globalContext.createConnection();
        //if actor is a user, canonicalize the name
        try{
            PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_CAPABILITIES_FOR_ARESOURCE_SQL);
            statement.setString(1, resourceID);
            statement.setString(2, action);
            ResultSet result = statement.executeQuery();
            
            boolean authorized = false;
            ArrayList<Group> allowedGroups =  new ArrayList<Group>();
            while(result.next()){
                String allowedActor = result.getString(ALLOWED_ACTOR);
                boolean isUser = result.getBoolean(IS_USER);
                
                //If we found user's name in a capability token we are done
                if(isUser){
                    if(Utils.isSameDN(actor, allowedActor)){
                        authorized = true;
                        break;
                    }
                }else{
                    allowedGroups.add(groupManager.getGroup(allowedActor));
                }
            }
            result.close();
            
            if(authorized){
                return true;
            }else{
                //We need to check does this user is in any 
                Traversal traversal = new Traversal();
                for(Group group:allowedGroups){
                    authorized = group.isAuthorized(actor, traversal);
                    if(authorized){
                        return true;
                    }
                }
                return false;
            }
         } catch (SQLException e) {
            throw new XregistryException(e);
        }finally{
            globalContext.closeConnection(connection);
        }
    }
}

