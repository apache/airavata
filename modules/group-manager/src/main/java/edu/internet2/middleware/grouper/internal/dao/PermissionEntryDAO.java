/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

/** 
 * Basic <code>PermissionEntry</code> DAO interface.
 * @author  mchyzer
 * @version $Id: PermissionEntryDAO.java,v 1.3 2009-10-26 04:52:17 mchyzer Exp $
 */
public interface PermissionEntryDAO extends GrouperDAO {

  /**
   * find all permissions that a subject has
   * @param memberId
   * @return the permissions
   */
  public Set<PermissionEntry> findByMemberId(String memberId);
  
  /**
   * get attribute assigns by member and attribute def name id
   * @param memberId
   * @param attributeDefNameId
   * @return set of assigns or empty if none there
   */
  public Set<PermissionEntry> findByMemberIdAndAttributeDefNameId(String memberId, String attributeDefNameId);

  /**
   * see if the permission exists and is enabled
   * @param subjectId
   * @param sourceId
   * @param action
   * @param attributeDefNameName
   * @return true if has permissions and is enabled
   */
  public boolean hasPermissionBySubjectIdSourceIdActionAttributeDefName(String subjectId, String sourceId,
                                                                        String action, String attributeDefNameName);
  
  
  /**
   * securely search for assignments.  need to pass in either the assign ids, def ids, def name ids, or group ids
   * cannot have more than 100 bind variables.  well, you can pass more than 100 members... it will batch
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param roleIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @param memberIds
   * @param noEndDate true if no end date on memberships
   * @return the permissions
   */
  public Set<PermissionEntry> findPermissions(
          Collection<String> attributeDefIds,
          Collection<String> attributeDefNameIds,
          Collection<String> roleIds,
          Collection<String> actions,
          Boolean enabled,
          Collection<String> memberIds,
          boolean noEndDate);

  /**
   * securely search for assignments.  need to pass in either the assign ids, def ids, def name ids, or group ids
   * cannot have more than 100 bind variables.  well, you can pass more than 100 members... it will batch
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param roleIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @param memberIds
   * @param noEndDate true if no end date on memberships
   * @param permissionNameInStem if looking for permission names in a certain stem, put it here
   * @param permissionNameInStemScope if looking for permission names in a certain stem, put scope here
   * @return the permissions
   */
  public Set<PermissionEntry> findPermissions(
          Collection<String> attributeDefIds,
          Collection<String> attributeDefNameIds,
          Collection<String> roleIds,
          Collection<String> actions,
          Boolean enabled,
          Collection<String> memberIds,
          boolean noEndDate, Stem permissionNameInStem, Scope permissionNameInStemScope);
  
  /**
   * securely search for assignments.  need to pass in either the assign ids, def ids, def name ids, or group ids
   * cannot have more than 100 bind variables.  well, you can pass more than 100 members... it will batch
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param roleIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @param memberIds
   * @param noEndDate true if no end date on memberships
   * @param permissionNameInStem if looking for permission names in a certain stem, put it here
   * @param permissionNameInStemScope if looking for permission names in a certain stem, put scope here
   * @param queryOptions queryOptions for sorting and paging
   * @return the permissions
   */
  public Set<PermissionEntry> findPermissions(
          Collection<String> attributeDefIds,
          Collection<String> attributeDefNameIds,
          Collection<String> roleIds,
          Collection<String> actions,
          Boolean enabled,
          Collection<String> memberIds,
          boolean noEndDate, Stem permissionNameInStem, Scope permissionNameInStemScope, QueryOptions queryOptions);


  /**
   * securely search for assignments
   * @param attributeAssignType
   * @param attributeDefId optional
   * @param attributeDefNameId mutually exclusive with attributeDefIds
   * @param ownerRoleId optional
   * @param ownerStemId optional
   * @param ownerMemberId optional
   * @param ownerAttributeDefId optional
   * @param ownerMembershipId optional
   * @param action optional
   * @param enabled (null means all, true means enabled, false means disabled)
   * @param includeAssignmentsOnAssignments if assignments on assignments should also be included
   * @return the assignments
   */
  public Set<PermissionEntry> findPermissions(
          String attributeDefId, String attributeDefNameId,
          String ownerRoleId, String ownerMemberId, String action,
          Boolean enabled);

  /**
   * securely search for assignments
   * @param attributeAssignType
   * @param attributeDefId optional
   * @param attributeDefNameId mutually exclusive with attributeDefIds
   * @param ownerRoleId optional
   * @param ownerStemId optional
   * @param ownerAttributeDefId optional
   * @param ownerMembershipId optional
   * @param action optional
   * @param enabled (null means all, true means enabled, false means disabled)
   * @param includeAssignmentsOnAssignments if assignments on assignments should also be included
   * @return the assignments
   */
  public Set<PermissionEntry> findRolePermissions(
          String attributeDefId, String attributeDefNameId,
          String ownerRoleId, String action,
          Boolean enabled);

  
  /**
   * securely search for assignments.  need to pass in either the assign ids, def ids, def name ids, or group ids
   * cannot have more than 100 bind variables
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param roleIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @param memberIds
   * @return the permissions
   */
  public Set<PermissionEntry> findPermissions(
          Collection<String> attributeDefIds,
          Collection<String> attributeDefNameIds,
          Collection<String> roleIds,
          Collection<String> actions,
          Boolean enabled,
          Collection<String> memberIds);

  /**
   * Find all permissions based on attributeDefinition, and a range of disabled dates
   * @param attributeDefId 
   * @param disabledDateFrom null if dont consider
   * @param disabledDateTo null if dont consider
   * @return the permission records
   */
  public Set<PermissionEntry> findPermissionsByAttributeDefDisabledRange(String attributeDefId,
                                                                         Timestamp disabledDateFrom, Timestamp disabledDateTo);


  /**
   * find subjects who are not in a group but who have permissions
   * @param attributeDefId
   * @param groupId
   * @param immediateRoleMembershipsOrRoleSubject
   * @param queryOptions
   * @param enabled
   * @param hasNoEndDate
   * @return the set of members
   */
  public Set<PermissionEntry> findAllPermissionsNotInGroupAndType(String attributeDefId, String groupId,
                                                                  boolean immediateRoleMembershipsOrRoleSubject, QueryOptions queryOptions, Boolean enabled, boolean hasNoEndDate);

  /**
   * find subjects who are not in a group but who have permissions
   * @param attributeDefId
   * @param stem
   * @param stemScope
   * @param immediateRoleMembershipsOrRoleSubject
   * @param queryOptions
   * @param enabled
   * @param hasNoEndDate
   * @return the set of members
   */
  public Set<PermissionEntry> findAllPermissionsNotInStem(String attributeDefId, Stem stem, Stem.Scope stemScope,
                                                          boolean immediateRoleMembershipsOrRoleSubject, QueryOptions queryOptions, Boolean enabled, boolean hasNoEndDate);

  /**
   * securely search for role assignments.  need to pass in either the assign ids, def ids, def name ids, or group ids
   * cannot have more than 100 bind variables
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param roleIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @param memberIds
   * @param noEndDate true if no end date on memberships
   * @return the permissions
   */
  public Set<PermissionEntry> findRolePermissions(
          Collection<String> attributeDefIds,
          Collection<String> attributeDefNameIds,
          Collection<String> roleIds,
          Collection<String> actions,
          Boolean enabled,
          boolean noEndDate);

  /**
   * securely search for role assignments.  need to pass in either the assign ids, def ids, def name ids, or group ids
   * cannot have more than 100 bind variables
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param roleIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @param memberIds
   * @param noEndDate true if no end date on memberships
   * @param permissionNameInStem if looking for permission names in a certain stem, put it here
   * @param permissionNameInStemScope if looking for permission names in a certain stem, put scope here
   * @return the permissions
   */
  public Set<PermissionEntry> findRolePermissions(
          Collection<String> attributeDefIds,
          Collection<String> attributeDefNameIds,
          Collection<String> roleIds,
          Collection<String> actions,
          Boolean enabled,
          boolean noEndDate, Stem permissionNameInStem, Scope permissionNameInStemScope);

} 

