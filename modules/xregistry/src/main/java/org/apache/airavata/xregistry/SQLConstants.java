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

public interface SQLConstants {
    
    public static final String GROUPID = "groupid";
    public static final String USERID = "userid";
    public static final String CONTANTED_GROUP_ID = "contained_groupid";
    public static final String HOST_DESC_STR = "hostdesc_str";
    public static final String OWNER = "owner";
    public static final String IS_USER = "isUser";
    public static final String ALLOWED_ACTOR = "allowed_actor";
    
    public static final String QNAME = "qname";
    public static final String HOST_NAME = "host_name";
    public static final String APP_DESC_STR = "appdesc_str";
    public static final String SERVICE_DESC_STR = "servicemap_str";
    public static final String AWSDL_STR = "awsdl_str";
    public static final String CWSDL_STR = "wsdl_str";
    public static final String PORT_TYPE_STR = "port_type_name";
    public static final String DOC_STR = "doc_str";
    
    public static final String OGCE_RESOURCE = "resourceDocument";
    public static final String OGCE_RESOURCE_CREATED = "created";
    public static final String OGCE_RESOURCE_DESC = "resourceDesc";
    public static final String OGCE_RESOURCE_NAME = "resourceName";
    
    
    public static final String RESOURCE_ID = "resourceid";
    public static final String ACTION_TYPE = "action_type";
    public static final String ASSERTION = "assertions";
    public static final String NOT_BEFORE = "notbefore";
    public static final String NOT_AFTER = "notafter";

    public static final String ADD_USER_SQL = "INSERT INTO user_table (userid,description,isAdmin) VALUES (?, ?, ?)";
    public static final String FIND_USER_SQL = "SELECT userid,description from user_table where userid like ? ";
    public static final String DELETE_USER_SQL_MAIN = "DELETE FROM user_table WHERE userid = ? ";
    public static final String DELETE_USER_SQL_DEPEND = "DELETE FROM user_groups_table WHERE userid = ? ";
    
    public static final String ADD_GROUP_SQL = "INSERT INTO group_table (groupid,description) VALUES (?, ?)";
    public static final String FIND_GROUP_SQL = "SELECT groupid,description from group_table where groupid like ? ";
    public static final String DELETE_GROUP_SQL_MAIN = "DELETE FROM group_table WHERE groupid = ? ";
    public static final String DELETE_GROUP_SQL_DEPEND = "DELETE FROM group_group_table WHERE groupid = ? OR contained_groupid = ?";
    
    public static final String ADD_USER_TO_GROUP = "INSERT INTO user_groups_table (userid,groupid) VALUES (?, ?)";
    public static final String ADD_GROUP_TO_GROUP_SQL = "INSERT INTO group_group_table (groupid,contained_groupid) VALUES (?, ?)";
    
    public static final String REMOVE_GROUP_FROM_GROUP = "DELETE FROM group_group_table WHERE contained_groupid = ?  AND groupid = ?";
    
    
    
    public static final String GET_ALL_GROUPS_SQL = "SELECT groupid from group_table";
    public static final String GET_ALL_GROUP2GROUP_SQL = "SELECT groupid,contained_groupid from group_group_table";
    public static final String GET_ALL_USER2GROUP_SQL = "SELECT userid,groupid from user_groups_table";
    
    public static final String GET_ALL_USERS_SQL = "SELECT userid from user_table";
    public static final String REMOVE_USER_FROM_GROUP = "DELETE FROM user_groups_table WHERE userid = ?  AND groupid = ? ";
    public static final String GET_USERS_GIVEN_GROUP = "SELECT userid from user_groups_table WHERE groupid =?";
    public static final String GET_GROUPS_GIVEN_USER = "SELECT groupid from user_groups_table WHERE  userid=?";
    public static final String GET_SUBGROUPS_GIVEN_GROUP = "SELECT contained_groupid from group_group_table WHERE  groupid=?";
    public static final String GET_ADMIN_USERS_SQL = "SELECT userid from user_table WHERE isAdmin=?";
    
   
    public static final String ADD_RESOURCE_SQL = "INSERT INTO resource_table (resourceid ,owner) VALUES (?, ?)";
    public static final String DELETE_RESOURCE_SQL = "DELETE FROM resource_table WHERE resourceid = ? ";
    public static final String GET_RESOURCE_OWNER_SQL = "SELECT owner from resource_table where resourceid = ?";
    
    public static final String ADD_SERVICE_DESC_SQL = "INSERT INTO service_map_table(resourceid,qname,servicemap_str,awsdl_str) VALUES (?, ?,?,?)";
    public static final String DELETE_SERVICE_DESC_SQL = "DELETE FROM service_map_table WHERE qname = ? ";
    public static final String GET_SERVICE_DESC_SQL = "SELECT servicemap_str,resourceid from service_map_table where qname = ?";
    public static final String GET_AWSDL_SQL = "SELECT awsdl_str from service_map_table where qname = ?";
    public static final String FIND_SERVICE_DESC_SQL = "SELECT qname,owner,resource_table.resourceid from service_map_table,resource_table where service_map_table.resourceid=resource_table.resourceid AND qname like ? ";
    
    public static final String ADD_APP_DESC_SQL = "INSERT INTO appdesc_table(resourceid,qname,host_name,appdesc_str) VALUES (?, ?, ?, ?)";
    public static final String DELETE_APP_DESC_SQL = "DELETE FROM appdesc_table WHERE qname= ? AND host_name = ? ";
    public static final String GET_APP_DESC_SQL = "SELECT appdesc_str from appdesc_table WHERE qname= ? AND host_name = ?";
    public static final String FIND_APP_DESC_SQL = "SELECT qname,host_name,owner,resource_table.resourceid from appdesc_table,resource_table where appdesc_table.resourceid=resource_table.resourceid AND qname like ? ";
    
    public static final String ADD_CWSDL_SQL = "INSERT INTO cwsdl_table(resourceid,qname,wsdl_str,time_stamp,life_time,port_type_name) VALUES (?, ?, ?, ?, ?, ?)";
    public static final String DELETE_CWSDL_SQL = "DELETE FROM cwsdl_table WHERE qname = ? ";
    public static final String GET_CWSDL_SQL = "SELECT wsdl_str from cwsdl_table where qname like ? OR port_type_name = ?";
    public static final String FIND_CWSDL_SQL = "SELECT qname,owner,resource_table.resourceid from cwsdl_table,resource_table where cwsdl_table.resourceid=resource_table.resourceid AND qname like ? ";
    public static final String UPDATE_CWSDL_SQL = "DELETE FROM cwsdl_table WHERE (time_stamp + life_time) < ? ";

    public static final String ADD_HOST_DESC_SQL = "INSERT INTO hostdesc_table (resourceid,host_name,hostdesc_str) VALUES (?, ?,?)";
    public static final String DELETE_HOST_DESC_SQL = "DELETE FROM hostdesc_table WHERE host_name = ? ";
    public static final String GET_HOST_DESC_SQL = "SELECT hostdesc_str,resourceid from hostdesc_table where host_name = ?";
    public static final String FIND_HOST_DESC_SQL = "SELECT distinct host_name,owner,resource_table.resourceid from hostdesc_table,resource_table where hostdesc_table.resourceid=resource_table.resourceid AND host_name like ? ";
    
    
    public static final String ADD_DOC_SQL = "INSERT INTO doc_table (resourceid,doc_str) VALUES (?, ?)";
    public static final String DELETE_DOC_SQL = "DELETE FROM doc_table WHERE resourceid = ? ";
    public static final String GET_DOC_SQL = "SELECT doc_str,resourceid from doc_table where resourceid = ?";
    public static final String FIND_DOC_SQL = "SELECT owner,resource_table.resourceid from doc_table,resource_table where doc_table.resourceid=resource_table.resourceid AND resource_table.resourceid like ? ";

    
    
    
    public static final String GET_CAPABILITIES_FOR_ARESOURCE_SQL = "SELECT allowed_actor,isUser from capability_table WHERE resourceid =? AND (action_type = ? OR action_type='ALL')";
    
    public static final String GET_CAPABILITIES = "SELECT resourceid,allowed_actor,isUser,action_type from capability_table";
    
    public static final String GET_CAPABILITIES_BY_ACTOR = "SELECT owner,resourceid,allowed_actor,isUser,action_type,assertions,notbefore,notafter FROM capability_table " +
                "WHERE allowed_actor = ?";
    
    public static final String GET_CAPABILITIES_BY_RESOURCE = "SELECT owner,resourceid,allowed_actor,isUser,action_type,assertions,notbefore,notafter FROM capability_table " +
    "WHERE resourceid = ? ";
    
    public static final String GET_CAPABILITIES_BY_ACTOR_AND_RESOURCE = "SELECT owner,resourceid,allowed_actor,isUser,action_type,assertions,notbefore,notafter FROM capability_table " +
    "WHERE resourceid = ? AND allowed_actor = ?";
    
    public static final String ADD_FULL_CAPABILITY_SQL= "INSERT INTO capability_table (owner ,resourceid,allowed_actor,isUser,assertions,notbefore,notafter) VALUES (?, ?, ?, ?, ?, ?, ?)";
    public static final String ADD_CAPABILITY_SQL= "INSERT INTO capability_table (owner ,resourceid,allowed_actor,isUser,action_type) VALUES (?, ?, ?, ?, ?)";
    public static final String REMOVE_CAPABILITY_SQL= "DELETE FROM capability_table where allowed_actor =? AND resourceid =? ";
    
    public static final String GIVEN_APP_FIND_HOSTS_SQL = "SELECT host_name from appdesc_table where qname = ? ";
    
    
    // Added for OGCE
    public static final String ADD_OGCE_RESOURCE_SQL = "INSERT INTO ogce_resource_table (resourceid , resourcename, resourcetype, resourceDesc, resourceDocument, parentTypedID) VALUES (?, ?, ?, ?, ? ,? )";
    public static final String DELETE_OGCE_RESOURCE_SQL = "DELETE FROM ogce_resource_table WHERE resourceid = ? and resourcetype = ? ";
    public static final String GET_OGCE_RESOURCE_DESC_SQL = "SELECT resourceDesc,resourceid, resourcetype, created, resourceDocument  from ogce_resource_table WHERE resourceid = ? and resourcetype = ? and parentTypedID = ?";
    public static final String FIND_OGCE_RESOURCE_DESC_SQL = "SELECT owner, ogce_resource_table.created, resource_table.resourceid, resourceDesc, resourceName from ogce_resource_table,resource_table where ogce_resource_table.resourceid=resource_table.resourceid AND (resource_table.resourceid like ? OR resourcename like ?) AND resourceType = ? and parentTypedID = ?";
    public static final String GET_OGCE_RESOURCE_DESC_SQL_WITHOUTTYPE = "SELECT resourceDesc,resourceid, resourcetype, created, resourceDocument  from ogce_resource_table WHERE resourceid like ?";
    public static final String FIND_OGCE_RESOURCE_DESC_SQL_WITHOUTTYPE = "SELECT owner, ogce_resource_table.created ,resource_table.resourceid, resourceDesc, resourceName from ogce_resource_table,resource_table where ogce_resource_table.resourceid=resource_table.resourceid AND (resource_table.resourceid like ? OR resourcename like ?)";
    public static final String ADD_HOST_SQL = "INSERT INTO hostdesc_table (resourceid,host_name,hostdesc_str) VALUES (?, ?,?)";
}

