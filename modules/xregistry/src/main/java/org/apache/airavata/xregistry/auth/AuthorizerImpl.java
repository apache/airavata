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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.airavata.xregistry.SQLConstants;
import org.apache.airavata.xregistry.XregistryConstants;
import org.apache.airavata.xregistry.XregistryException;
import org.apache.airavata.xregistry.context.GlobalContext;
import org.apache.airavata.xregistry.group.Group;
import org.apache.airavata.xregistry.group.GroupManager;
import org.apache.airavata.xregistry.group.Traversal;
import org.apache.airavata.xregistry.utils.Utils;

import xsul.MLogger;

public class AuthorizerImpl implements Authorizer, SQLConstants {
    protected static MLogger log = MLogger.getLogger(XregistryConstants.LOGGER_NAME);

    private final GlobalContext context;

    private final GroupManager groupManager;

    public AuthorizerImpl(final GlobalContext context, GroupManager groupManager) {
        this.context = context;
        this.groupManager = groupManager;
    }

    public boolean isAuthorized(String user, String resourceID, Action action)
            throws XregistryException {
        Connection connection = context.createConnection();
        try {
            if(user != null){
                user = Utils.canonicalizeDN(user);
            }
            
            //We create a account for use on remand
            if (!groupManager.hasUser(user)) {
                groupManager.createUser(user, "New User created on request", false);
                groupManager.addUsertoGroup(XregistryConstants.PUBLIC_GROUP, user);
                log.info("New user "+user+"created on request");
            }
            String owner;
            switch (action) {
                case AddNew:
                    if (groupManager.hasUser(user)) {
                        // registered users may add new documents
                        return true;
                    } else {
                        groupManager.createUser(user, "New User created on request", false);
                        log.info("New user "+user+"created on request");
                        return true;
                    }
                case Read:
                    //We allow all reads
                    return true;
                case ResourceAdmin:
                    owner = getOwner(resourceID, connection);
                    if (Utils.isSameDN(owner, user) || groupManager.isAdminUser(user)) {
                        return true;
                    } else {
                        throw new XregistryException(action
                                + " is a Admin Operation, and only owner of " + resourceID
                                + " is allowed ");
                    }
                case SysAdmin:
                    if (groupManager.isAdminUser(user)) {
                        return true;
                    } else {
                        throw new XregistryException(action + " is a Admin operation but user " + user
                                + " is not an adminsitrator");
                    }
                case Write:
                    return verifyWriteAccesses(user, resourceID, action, connection);
                default:
                    throw new XregistryException("User " + user
                            + " is not authorized to a perform this action " + action);
            }
        } finally {
            context.closeConnection(connection);
        }

    }

    private boolean verifyWriteAccesses(String user, String resourceID, Action action,
            Connection connection) throws XregistryException {

        try {
            if (resourceID != null) {
                String owner = getOwner(resourceID, connection);
                //If he is the owner hs has all privileges
                if (Utils.isSameDN(owner,user)) {
                    return true;
                }

                PreparedStatement statement = connection
                        .prepareStatement(SQLConstants.GET_CAPABILITIES_FOR_ARESOURCE_SQL);
                statement.setString(1, resourceID);
                statement.setString(2, action.toString());
                ResultSet result = statement.executeQuery();

                boolean authorized = false;
                ArrayList<Group> allowedGroups = new ArrayList<Group>();
                while (result.next()) {
                    String allowedActor = result.getString(ALLOWED_ACTOR);
                    boolean isUser = result.getBoolean(IS_USER);

                    if (isUser) {
                        //If capabilities specified as user, we can veirfy that starigh away
                        if (Utils.isSameDN(user, allowedActor)) {
                            authorized = true;
                            break;
                        }
                    } else {
                        allowedGroups.add(groupManager.getGroup(allowedActor));
                    }

                }
                result.close();

                if (authorized) {
                    return true;
                } else {
                    //Do graph traversal and authorize if there is a match
                    Traversal traversal = new Traversal();
                    for (Group group : allowedGroups) {
                        authorized = group.isAuthorized(user, traversal);
                        if (authorized) {
                            return true;
                        }
                    }
                    throw new XregistryException("User " + user
                            + " is not authorized to accsess resource " + resourceID);
                }

            } else {
                throw new XregistryException("For write access resource ID must not be Null");
            }
        } catch (SQLException e) {
            throw new XregistryException(e);
        }
    }

    private String getOwner(String resourceID, Connection connection) throws XregistryException {
        try {
            ResultSet results = null;
            try {
                PreparedStatement statement = connection.prepareStatement(GET_RESOURCE_OWNER_SQL);
                statement.setString(1, resourceID);
                results = statement.executeQuery();
                if (results.next()) {
                    return results.getString(OWNER);
                } else {
                    throw new XregistryException("No such resource " + resourceID);
                }
            } catch (SQLException e) {
                throw new XregistryException(e);
            } catch (XregistryException e) {
                throw new XregistryException(e);
            } finally {
                if (results != null) {
                    results.close();
                }
            }
        } catch (SQLException e) {
            throw new XregistryException(e);
        }
    }
    
    public UserAuthorizer getAuthorizerForUser(String userName){
        return new UserAuthorizer(groupManager,userName);
    }
    
    
 
}
