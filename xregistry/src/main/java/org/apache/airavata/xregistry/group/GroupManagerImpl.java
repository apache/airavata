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

import static org.apache.airavata.xregistry.utils.Utils.canonicalizeDN;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.apache.airavata.xregistry.SQLConstants;
import org.apache.airavata.xregistry.XregistryConstants;
import org.apache.airavata.xregistry.XregistryException;
import org.apache.airavata.xregistry.context.GlobalContext;
import org.apache.airavata.xregistry.utils.Utils;

import xregistry.generated.ListSubActorsGivenAGroupResponseDocument.ListSubActorsGivenAGroupResponse.Actor;
import xsul.MLogger;

public class GroupManagerImpl implements SQLConstants, GroupManager {
	protected static MLogger log = MLogger
			.getLogger(XregistryConstants.LOGGER_NAME);
	private final Hashtable<String, Group> groups = new Hashtable<String, Group>();
	private final GlobalContext context;
	private boolean cascadingDeletes = true;
	private Hashtable<String, User> users = new Hashtable<String, User>();
	private Hashtable<String, String> adminUsers = new Hashtable<String, String>();

	public GroupManagerImpl(GlobalContext context) throws XregistryException {
		Connection connection = context.createConnection();
		this.context = context;
		/**
		 * We create a Memory model of the Group and user tree at startup and it
		 * is kept in sync whenever a change happen to the Groups. However
		 * database can not be shared by two Xregistry instances
		 */
		Statement statement = null;
		ResultSet results = null;
		try {
			statement = connection.createStatement();
			results = statement.executeQuery(SQLConstants.GET_ALL_GROUPS_SQL);
			while (results.next()) {
				String groupId = results.getString(GROUPID);
				log.info("@@@@@@@@@@@@@@@@@@@@@groupId" + groupId);
				addGroup(new Group(groupId));
			}
			results.close();

			results = statement
					.executeQuery(SQLConstants.GET_ALL_GROUP2GROUP_SQL);
			while (results.next()) {
				String masterGroupId = results.getString(GROUPID);
				String containedGroupID = results.getString(CONTANTED_GROUP_ID);
				getGroup(masterGroupId).addGroup(getGroup(containedGroupID));
			}
			results.close();

			results = statement
					.executeQuery(SQLConstants.GET_ALL_USER2GROUP_SQL);
			while (results.next()) {
				String userID = results.getString(USERID);
				String groupId = results.getString(GROUPID);
				Group group = getGroup(groupId);
				if (group != null) {
					group.addUser(userID);
				} else {
					log.warning("Group "
							+ groupId
							+ " find in user to group table, but not found in Group table. Database may be inconsistant");
				}

			}
			results.close();
			PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.GET_ADMIN_USERS_SQL);
			preparedStatement.setBoolean(1, true);
			results = preparedStatement.executeQuery();
			while (results.next()) {
				String adminUSer = results.getString(USERID);
				adminUsers.put(adminUSer, adminUSer);
			}
			results.close();

			String[] userList = listUsers();

			if (userList != null) {
				for (String user : userList) {
					users.put(user, new User(user));
				}
			}

			results = statement.executeQuery(SQLConstants.GET_CAPABILITIES);
			while (results.next()) {
				boolean isUser = results.getBoolean(IS_USER);
				String resourceID = results.getString(RESOURCE_ID);
				String action = results.getString(ACTION_TYPE);
				String actorName = results.getString(ALLOWED_ACTOR);
				if (!isUser) {
					Group group = getGroup(actorName);
					if (group != null) {
						group.addAuthorizedResource(resourceID, action);
					}
				} else {
					User user = getUser(actorName);
					if (user != null) {
						user.addAuthorizedResource(resourceID, action);
					}
				}
			}
			// Create a public group if it is not there and add all users to
			// that group
			Group publicGroup = getGroup(XregistryConstants.PUBLIC_GROUP);
			if (publicGroup == null) {
				createGroup(XregistryConstants.PUBLIC_GROUP, "Public Group");
				publicGroup = getGroup(XregistryConstants.PUBLIC_GROUP);
			}
			for (String user : users.keySet()) {
				if (!publicGroup.hasUser(user)) {
					addUsertoGroup(publicGroup.getName(), user);
				}
			}

			String anonymousUser = Utils
					.canonicalizeDN(XregistryConstants.ANONYMOUS_USER);
			if (!hasUser(anonymousUser)) {
				createUser(anonymousUser, anonymousUser, false);
			}
			if (!publicGroup.hasUser(anonymousUser)) {
				addUsertoGroup(publicGroup.getName(), anonymousUser);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			log.severe(e.getLocalizedMessage(), e);
			// throw new XregistryException(e);
		} finally {
			try {
				results.close();
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// throw new XregistryException(e);
			}
			context.closeConnection(connection);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xregistry.auth.GroupManager#isAdminUser(java.lang.String)
	 */
	public boolean isAdminUser(String user) {
		return adminUsers.containsKey(user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xregistry.auth.GroupManager#hasUser(java.lang.String)
	 */
	public boolean hasUser(String userName) {
		return users.containsKey(userName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xregistry.auth.GroupManager#getGroup(java.lang.String)
	 */
	public Group getGroup(String name) {
		return groups.get(name);
	}

	protected void addGroup(Group group) {
		groups.put(group.getName(), group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xregistry.auth.GroupManager#createGroup(java.lang.String,
	 * java.lang.String)
	 */
	public void createGroup(String newGroup, String description)
			throws XregistryException {
		Connection connection = context.createConnection();
		try {
			PreparedStatement statement = connection
					.prepareStatement(ADD_GROUP_SQL);
			statement.setString(1, newGroup);
			statement.setString(2, description);
			statement.executeUpdate();
			// Add the group to memory model
			addGroup(new Group(newGroup));
			log.info("Group " + newGroup + " Created");
		} catch (SQLException e) {
			throw new XregistryException(e);
		} finally {
			context.closeConnection(connection);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xregistry.auth.GroupManager#createUser(java.lang.String,
	 * java.lang.String, boolean)
	 */
	public void createUser(String newUser, String description, boolean isAdmin)
			throws XregistryException {
		Connection connection = context.createConnection();
		try {
			PreparedStatement statement = connection
					.prepareStatement(ADD_USER_SQL);
			statement.setString(1, Utils.canonicalizeDN(newUser));
			statement.setString(2, description);
			statement.setBoolean(3, isAdmin);
			statement.executeUpdate();
			log.info("User " + newUser + " created");
			users.put(newUser, new User(newUser));
		} catch (SQLException e) {
			throw new XregistryException(e);
		} finally {
			context.closeConnection(connection);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xregistry.auth.GroupManager#addGrouptoGroup(java.lang.String,
	 * java.lang.String)
	 */
	public void addGrouptoGroup(String groupName, String grouptoAddedName)
			throws XregistryException {
		Group group = getGroup(groupName);
		if (group == null) {
			throw new XregistryException("No such Group " + groupName);
		}

		Group grouptoAdd = getGroup(groupName);
		if (grouptoAdd == null) {
			throw new XregistryException("No such Group " + groupName);
		}

		if (group.hasGroup(grouptoAddedName)) {
			throw new XregistryException("Group" + grouptoAddedName
					+ " already exisits in group " + groupName);
		}
		Connection connection = context.createConnection();
		try {
			PreparedStatement statement = connection
					.prepareStatement(ADD_GROUP_TO_GROUP_SQL);
			statement.setString(1, groupName);
			statement.setString(2, grouptoAddedName);
			statement.executeUpdate();
			group.addGroup(grouptoAdd);
			log.info("Add Group " + groupName + " to " + grouptoAddedName);
		} catch (SQLException e) {
			throw new XregistryException(e);
		} finally {
			context.closeConnection(connection);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xregistry.auth.GroupManager#addUsertoGroup(java.lang.String,
	 * java.lang.String)
	 */
	public void addUsertoGroup(String groupName, String usertoAdded)
			throws XregistryException {
		usertoAdded = Utils.canonicalizeDN(usertoAdded);
		Group group = getGroup(groupName);
		if (group == null) {
			throw new XregistryException("No such Group " + groupName);
		}
		if (group.hasUser(usertoAdded)) {
			throw new XregistryException("user " + usertoAdded
					+ " already exisits in group " + groupName);
		}
		Connection connection = context.createConnection();
		try {
			PreparedStatement statement = connection
					.prepareStatement(ADD_USER_TO_GROUP);
			statement.setString(1, usertoAdded);
			statement.setString(2, groupName);
			statement.executeUpdate();
			group.addUser(usertoAdded);
			log.info("Add User " + usertoAdded + " to " + groupName);
		} catch (SQLException e) {
			throw new XregistryException(e);
		} finally {
			context.closeConnection(connection);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xregistry.auth.GroupManager#deleteGroup(java.lang.String)
	 */
	public void deleteGroup(String groupID) throws XregistryException {
		try {
			Connection connection = context.createConnection();
			connection.setAutoCommit(false);
			try {
				PreparedStatement statement1 = connection
						.prepareStatement(DELETE_GROUP_SQL_MAIN);
				statement1.setString(1, groupID);
				int updateCount = statement1.executeUpdate();

				if (updateCount == 0) {
					throw new XregistryException(
							"Database is not updated, Can not find such Group "
									+ groupID);
				}

				if (cascadingDeletes) {
					PreparedStatement statement2 = connection
							.prepareStatement(DELETE_GROUP_SQL_DEPEND);
					statement2.setString(1, groupID);
					statement2.setString(2, groupID);
					statement2.executeUpdate();
				}

				connection.commit();
				groups.remove(groupID);
				log.info("Delete Group " + groupID
						+ (cascadingDeletes ? " with cascading deletes " : ""));
			} catch (SQLException e) {
				connection.rollback();
				throw new XregistryException(e);
			} finally {
				context.closeConnection(connection);
			}
		} catch (SQLException e) {
			throw new XregistryException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xregistry.auth.GroupManager#deleteUser(java.lang.String)
	 */
	public void deleteUser(String userID) throws XregistryException {
		try {
			userID = Utils.canonicalizeDN(userID);
			Connection connection = context.createConnection();
			connection.setAutoCommit(false);
			try {
				PreparedStatement statement1 = connection
						.prepareStatement(DELETE_USER_SQL_MAIN);
				statement1.setString(1, userID);
				statement1.executeUpdate();

				PreparedStatement statement2 = connection
						.prepareStatement(DELETE_USER_SQL_DEPEND);
				statement2.setString(1, userID);
				statement2.executeUpdate();

				connection.commit();
				Collection<Group> groupList = groups.values();
				for (Group group : groupList) {
					group.removeUser(userID);
				}
				log.info("Delete User " + userID);
			} catch (SQLException e) {
				connection.rollback();
				throw new XregistryException(e);
			} finally {
				context.closeConnection(connection);
			}
		} catch (SQLException e) {
			throw new XregistryException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xregistry.auth.GroupManager#genericUpdate(java.lang.String,
	 * java.lang.String[])
	 */
	public void genericUpdate(String sql, String[] keys)
			throws XregistryException {
		Connection connection = context.createConnection();
		try {
			PreparedStatement statement1 = connection.prepareStatement(sql);
			for (int i = 0; i < keys.length; i++) {
				statement1.setString(i + 1, keys[i]);
			}
			statement1.executeUpdate();
		} catch (SQLException e) {
			throw new XregistryException(e);
		} finally {
			context.closeConnection(connection);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xregistry.auth.GroupManager#removeUserFromGroup(java.lang.String,
	 * java.lang.String)
	 */
	public void removeUserFromGroup(String groupName, String usertoRemoved)
			throws XregistryException {
		// DELETE FROM group_group_table WHERE AND userid = ? AND groupid = ?
		usertoRemoved = canonicalizeDN(usertoRemoved);
		genericUpdate(REMOVE_USER_FROM_GROUP, new String[] { usertoRemoved,
				groupName });
		Group group = getGroup(groupName);
		if (group == null) {
			throw new XregistryException("No such group " + groupName);
		}
		if (group.hasUser(usertoRemoved)) {
			group.removeUser(usertoRemoved);
		} else {
			throw new XregistryException("No such User " + usertoRemoved);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xregistry.auth.GroupManager#removeGroupFromGroup(java.lang.String,
	 * java.lang.String)
	 */
	public void removeGroupFromGroup(String groupName, String grouptoRemovedName)
			throws XregistryException {
		// DELETE FROM group_group_table WHERE AND contained_groupid = ? AND
		// groupid = ?
		Group group = getGroup(groupName);
		Group groupToRemove = getGroup(groupName);
		if (group == null) {
			throw new XregistryException("No such group " + groupName);
		}
		if (grouptoRemovedName == null) {
			throw new XregistryException("No such group " + grouptoRemovedName);
		}
		group.removeGroup(groupToRemove);
		genericUpdate(REMOVE_GROUP_FROM_GROUP, new String[] {
				grouptoRemovedName, groupName });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xregistry.auth.GroupManager#listUsers()
	 */
	public String[] listUsers() throws XregistryException {
		Connection connection = context.createConnection();
		ArrayList<String> users = new ArrayList<String>();
		try {
			Statement statement = connection.createStatement();
			ResultSet results = statement
					.executeQuery(SQLConstants.GET_ALL_USERS_SQL);
			while (results.next()) {
				String userID = results.getString(USERID);
				users.add(userID);
			}
		} catch (SQLException e) {
			throw new XregistryException(e);
		} finally {
			context.closeConnection(connection);
		}
		return Utils.toStrListToArray(users);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xregistry.auth.GroupManager#listGroups()
	 */
	public String[] listGroups() throws XregistryException {
		Connection connection = context.createConnection();
		ArrayList<String> groups = new ArrayList<String>();
		try {
			Statement statement = connection.createStatement();
			ResultSet results = statement
					.executeQuery(SQLConstants.GET_ALL_GROUPS_SQL);
			while (results.next()) {
				String groupId = results.getString(GROUPID);
				groups.add(groupId);
			}
		} catch (SQLException e) {
			throw new XregistryException(e);
		} finally {
			context.closeConnection(connection);
		}
		return Utils.toStrListToArray(groups);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xregistry.auth.GroupManager#listGroupsGivenAUser(java.lang.String)
	 */
	public String[] listGroupsGivenAUser(String user) throws XregistryException {
		user = canonicalizeDN(user);
		Connection connection = context.createConnection();
		ArrayList<String> groups = new ArrayList<String>();
		try {
			PreparedStatement statement = connection
					.prepareStatement(GET_GROUPS_GIVEN_USER);
			statement.setString(1, user);

			ResultSet results = statement.executeQuery();
			while (results.next()) {
				String groupId = results.getString(GROUPID);
				groups.add(groupId);
			}
		} catch (SQLException e) {
			throw new XregistryException(e);
		} finally {
			context.closeConnection(connection);
		}
		return Utils.toStrListToArray(groups);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xregistry.auth.GroupManager#listUsersGivenAGroup(java.lang.String)
	 */
	public Actor[] listSubActorsGivenAGroup(String group)
			throws XregistryException {
		List<Actor> actorList = new ArrayList<Actor>();
		Connection connection = context.createConnection();

		try {
			PreparedStatement statement = connection
					.prepareStatement(GET_USERS_GIVEN_GROUP);
			statement.setString(1, group);

			ResultSet results = statement.executeQuery();
			while (results.next()) {
				String groupId = results.getString(USERID);
				Actor actor = Actor.Factory.newInstance();
				actor.setActor(groupId);
				actor.setIsUser(true);
				actorList.add(actor);
			}
			results.close();
			statement.close();

			statement = connection.prepareStatement(GET_SUBGROUPS_GIVEN_GROUP);
			statement.setString(1, group);
			results = statement.executeQuery();
			while (results.next()) {
				String groupId = results.getString(CONTANTED_GROUP_ID);
				Actor actor = Actor.Factory.newInstance();
				actor.setActor(groupId);
				actor.setIsUser(false);
				actorList.add(actor);
			}

		} catch (SQLException e) {
			throw new XregistryException(e);
		} finally {
			context.closeConnection(connection);
		}
		return actorList.toArray(new Actor[0]);
	}

	public Collection<Group> getGroups() {
		return groups.values();
	}

	public User getUser(String user) {
		return users.get(user);
	}

}
