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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.permissions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class PermissionFinder {

  /**
   * limitEnvVars if processing limits, pass in a map of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   */
  private Map<String, Object> limitEnvVars = null;
  
  /**
   * limitEnvVars if processing limits with PermissionProcessor, pass in a map of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @param key
   * @param value
   * @return this for chaining
   */
  public PermissionFinder addLimitEnvVar(String key, Object value) {
    if (this.limitEnvVars == null) {
      this.limitEnvVars = new LinkedHashMap<String, Object>();
    }
    this.limitEnvVars.put(key, value);
    return this;
  }

  /**
   * limitEnvVars if processing limits, pass in a map of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @param theEnvVars the map to replace
   * @return this for chaining
   */
  public PermissionFinder assignLimitEnvVars(Map<String, Object> theEnvVars) {
    this.limitEnvVars = theEnvVars;
    return this;
  }
  
  /**
   * 
   */
  private Collection<String> memberIds = null;
  
  /**
   * add a member id to the search criteria
   * @param memberId
   * @return this for chaining
   */
  public PermissionFinder addMemberId(String memberId) {
    if (this.memberIds == null) {
      this.memberIds = new ArrayList<String>();
    }
    //no need to look for dupes
    if (!this.memberIds.contains(memberId)) {
      this.memberIds.add(memberId);
    }
    return this;
  }

  /**
   * add a collection of member ids to look for
   * @param theMemberIds
   * @return this for chaining
   */
  public PermissionFinder assignMemberIds(Collection<String> theMemberIds) {
    this.memberIds = theMemberIds;
    return this;
  }
  
  /**
   * add a subject to look for.
   * @param subject
   * @return this for chaining
   */
  public PermissionFinder addSubject(Subject subject) {
    
    //note, since we are chaining, we need to add if not found, since if we dont, it will find for
    //all subjects if no more are added
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
    return this.addMemberId(member.getUuid());
  }
  
  /**
   * 
   */
  private Collection<String> roleIds = null;
  
  /**
   * add a role id to the search criteria
   * @param roleId
   * @return this for chaining
   */
  public PermissionFinder addRoleId(String roleId) {
    if (!StringUtils.isBlank(roleId)) {
      if (this.roleIds == null) {
        this.roleIds = new ArrayList<String>();
      }
      //no need to look for dupes
      if (!this.roleIds.contains(roleId)) {
        this.roleIds.add(roleId);
      }
    }
    return this;
  }

  /**
   * assign a collection of role ids to look for
   * @param theRoleIds
   * @return this for chaining
   */
  public PermissionFinder assignRoleIds(Collection<String> theRoleIds) {
    this.roleIds = theRoleIds;
    return this;
  }
  
  /**
   * add a role to look for.
   * @param role
   * @return this for chaining
   */
  public PermissionFinder addRole(Role role) {
    
    return this.addRoleId(role.getId());
  }
  
  /**
   * add a role to look for by name.
   * @param name
   * @return this for chaining
   */
  public PermissionFinder addRole(String name) {
    
    Role role = GroupFinder.findByName(GrouperSession.staticGrouperSession(), name, true);
    
    return this.addRoleId(role.getId());
  }
  
  
  /**
   * 
   */
  private Collection<String> permissionDefIds = null;
  
  /**
   * add a attribute def id to the search criteria
   * @param attributeDefId
   * @return this for chaining
   */
  public PermissionFinder addPermissionDefId(String attributeDefId) {
    if (!StringUtils.isBlank(attributeDefId)) {
      if (this.permissionDefIds == null) {
        this.permissionDefIds = new ArrayList<String>();
      }
      //no need to look for dupes
      if (!this.permissionDefIds.contains(attributeDefId)) {
        this.permissionDefIds.add(attributeDefId);
      }
    }
    return this;
  }

  /**
   * assign a collection of attribute def ids to look for
   * @param theAttributeDefIds
   * @return this for chaining
   */
  public PermissionFinder assignPermissionDefIds(Collection<String> theAttributeDefIds) {
    this.permissionDefIds = theAttributeDefIds;
    return this;
  }
  
  /**
   * if narrowing search for permissions in a certain folder only
   * @param permissionNameFolder1
   * @return this for chaining
   */
  public PermissionFinder assignPermissionNameFolder(Stem permissionNameFolder1) {
    this.permissionNameFolder = permissionNameFolder1;
    return this;
  }
  
  /**
   * if searching in a folder, this is the scope: only in this folder, or also in subfolders
   * @param scope
   * @return this for chaining
   */
  public PermissionFinder assignPermissionNameFolderScope(Scope scope) {
    this.permissionNameFolderScope = scope;
    return this;
  }
  
  /**
   * add a attribute def to look for.
   * @param attributeDef
   * @return this for chaining
   */
  public PermissionFinder addPermissionDef(AttributeDef attributeDef) {
    
    return this.addPermissionDefId(attributeDef.getId());
  }
  
  /**
   * add a attribute def to look for by name.
   * @param attributeDefName
   * @return this for chaining
   */
  public PermissionFinder addPermissionDef(String attributeDefName) {
    
    AttributeDef attributeDef = AttributeDefFinder.findByName(attributeDefName, true);
    
    return this.addPermissionDefId(attributeDef.getId());
  }
  
  
  /**
   * 
   */
  private Collection<String> permissionNameIds = null;
  
  /**
   * if looking for permissions in a certain folder
   */
  private Stem permissionNameFolder = null;

  /**
   * if looking for permissions in any subfolder, or just in this folder directly
   */
  private Scope permissionNameFolderScope = null;
  
  /**
   * add an attribute def name id to the search criteria
   * @param attributeDefNameId
   * @return this for chaining
   */
  public PermissionFinder addPermissionNameId(String attributeDefNameId) {
    if (!StringUtils.isBlank(attributeDefNameId)) {
      if (this.permissionNameIds == null) {
        this.permissionNameIds = new ArrayList<String>();
      }
      //no need to look for dupes
      if (!this.permissionNameIds.contains(attributeDefNameId)) {
        this.permissionNameIds.add(attributeDefNameId);
      }
    }
    return this;
  }

  /**
   * assign a collection of attribute def name ids to look for
   * @param theAttributeDefNameIds
   * @return this for chaining
   */
  public PermissionFinder assignPermissionNameIds(Collection<String> theAttributeDefNameIds) {
    this.permissionNameIds = theAttributeDefNameIds;
    return this;
  }
  
  /**
   * add a attribute def name to look for.
   * @param attributeDefName
   * @return this for chaining
   */
  public PermissionFinder addPermissionName(AttributeDefName attributeDefName) {
    
    return this.addPermissionNameId(attributeDefName.getId());
  }
  
  /**
   * add a attribute def name to look for by name.
   * @param name
   * @return this for chaining
   */
  public PermissionFinder addPermissionName(String name) {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(name, true);
    
    return this.addPermissionNameId(attributeDefName.getId());
  }
  

  /**
   * 
   */
  private Collection<String> actions = null;
  
  /**
   * add a action to the search criteria
   * @param action
   * @return this for chaining
   */
  public PermissionFinder addAction(String action) {
    if (!StringUtils.isBlank(action)) {
      if (this.actions == null) {
        this.actions = new ArrayList<String>();
      }
      //no need to look for dupes
      if (!this.actions.contains(action)) {
        this.actions.add(action);
      }
    }
    return this;
  }

  /**
   * if sorting or paging
   */
  private QueryOptions queryOptions;
  
  /**
   * if sorting, paging, caching, etc
   * @param theQueryOptions
   * @return this for chaining
   */
  public PermissionFinder assignQueryOptions(QueryOptions theQueryOptions) {
    this.queryOptions = theQueryOptions;
    return this;
  }
  
  /**
   * assign actions to search for, return this for chaining
   * @param theActions
   * @return this for chaining
   */
  public PermissionFinder assignActions(Collection<String> theActions) {
    this.actions = theActions;
    return this;
  }
  
  /** if we should look for all, or enabled only.  default is all */
  private Boolean enabled;
  
  /**
   * true means enabled only, false, means disabled only, and null means all
   * @param theEnabled
   * @return this for chaining
   */
  public PermissionFinder assignEnabled(Boolean theEnabled) {
    this.enabled = theEnabled;
    return this;
  }
  
  /**
   * if we should find the best answer, or process limits, etc
   */
  private PermissionProcessor permissionProcessor;

  /**
   * if we should find the best answer, or process limits, etc
   * @param thePermissionProcessor
   * @return this for chaining
   */
  public PermissionFinder assignPermissionProcessor(PermissionProcessor thePermissionProcessor) {
    this.permissionProcessor = thePermissionProcessor;
    return this;
  }
  
  /** if we should filter out non immediate permissions */
  private boolean immediateOnly = false;

  /**
   * if we should filter out non immediate permissions
   * @param theImmediate
   * @return this for chaining
   */
  public PermissionFinder assignImmediateOnly(boolean theImmediate) {
    this.immediateOnly = theImmediate;
    return this;
  }
  
  /** are we looking for role permissions or subject permissions?  cant be null */
  private PermissionType permissionType = PermissionType.role_subject;
  
  /**
   * are we looking for role permissions or subject permissions?  cant be null
   * @param thePermissionType 
   * @return this for chaining
   */
  public PermissionFinder assignPermissionType(PermissionType thePermissionType) {
    this.permissionType = thePermissionType;
    return this;
  }
  
  
  /**
   * based on what you are querying for, see if has permission.
   * Note, you should be looking for one subject, 
   * one action, one resource, one role or multiple roles, etc
   * If you are looking for multiple, it will see if anyone has that permission
   * @return true if has permission, false if not
   */
  public boolean hasPermission() {
    
    //there needs to be a subject if looking by subject
    if (this.permissionType == PermissionType.role_subject) {
      if (GrouperUtil.length(this.memberIds) != 1) {
        throw new RuntimeException("You need to search for 1 and only 1 subject when using hasPermission for subject permissions: " + this);
      }
    } else if (this.permissionType == PermissionType.role) {
      if (GrouperUtil.length(this.roleIds) != 1) {
        throw new RuntimeException("You need to search for 1 and only 1 role when using hasPermission for role permissions: " + this);
      }
    }
    
    if (this.permissionProcessor == null) {
      //get all the permissions for this user in these roles
      this.permissionProcessor = limitEnvVars == null ? PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES
          : PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS ;
    } else if (this.permissionProcessor != PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES 
        && this.permissionProcessor != PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS){
      throw new RuntimeException("permissionProcessor must be FILTER_REDUNDANT_PERMISSIONS_AND_ROLES " +
          "or FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS");
    }
    
    Set<PermissionEntry> permissionEntriesSet = findPermissions();

    if (GrouperUtil.length(permissionEntriesSet) > 1) {
      throw new RuntimeException("Why is there more than one permission entry? " + GrouperUtil.stringValue(permissionEntriesSet));
    }
    
    if (GrouperUtil.length(permissionEntriesSet) == 0) {
      return false;
    }
    
    if (pointInTimeTo != null || pointInTimeFrom != null) {
      // we're not taking into consideration limits here...
      return !permissionEntriesSet.iterator().next().isDisallowed();
    }

    return permissionEntriesSet.iterator().next().isAllowedOverall();
  }
  
  /**
   * get the permissions, and the limits, so the caller (e.g. the UI/WS) doesnt have to get them again
   * @return the map of entry to the limits and values
   */
  public Map<PermissionEntry, Set<PermissionLimitBean>> findPermissionsAndLimits() {
    
    PermissionProcessor originalProcessor = this.permissionProcessor;
    
    this.validateProcessor();
    
    PermissionProcessor nonLimitProcessor = originalProcessor;
    boolean getLimits = false;

    if (originalProcessor != null && this.permissionProcessor.isLimitProcessor()) {
      nonLimitProcessor = this.permissionProcessor.nonLimitPermissionProcesssor();
      getLimits = true;
    }
    
    //do this without limits
    this.assignPermissionProcessor(nonLimitProcessor);
    
    Set<PermissionEntry> permissionEntrySet = this.findPermissions();
    
    //List<PermissionEntry> permissionEntryList = new ArrayList<PermissionEntry>(permissionEntrySet);
    //for (PermissionEntry permissionEntry : permissionEntryList) {
    //  System.out.println(permissionEntry.getRole().getDisplayExtension() + " - " 
    //      + permissionEntry.getSubjectId() + " - " + permissionEntry.getAction() + " - " 
    //      + permissionEntry.getAttributeDefName().getDisplayExtension() + " - " 
    //      + permissionEntry.getAttributeAssignId());
    //}
    //System.out.println("\n");
      
    //assign back original
    this.assignPermissionProcessor(originalProcessor);
    
    //get limits from permissions
    //CH 20111005: pass PIT to this method, to get limit attribute assignments at a certain point in time
    Map<PermissionEntry, Set<PermissionLimitBean>> permissionLimitBeanMap = GrouperUtil.nonNull(PermissionLimitBean.findPermissionLimits(permissionEntrySet));
    
    //if (GrouperUtil.length(permissionLimitBeanMap) > 0) {
    //  for (PermissionEntry permissionEntry : permissionLimitBeanMap.keySet()) {
    //    System.out.println(permissionEntry.getRole().getDisplayExtension() + " - " 
    //        + permissionEntry.getSubjectId() + " - " + permissionEntry.getAction() + " - " 
    //        + permissionEntry.getAttributeDefName().getDisplayExtension() + " - " 
    //        + permissionEntry.getAttributeAssignId() + ":");
    //    Set<PermissionLimitBean> permissionLimitBeans = permissionLimitBeanMap.get(permissionEntry);
    //    for (PermissionLimitBean permissionLimitBean : GrouperUtil.nonNull(permissionLimitBeans)) {
    //      System.out.println("  -> " + permissionLimitBean.getLimitAssign().getId() + " - " 
    //          + permissionLimitBean.getLimitAssign().getAttributeDefName().getDisplayExtension());
    //    }
    //  }
    //}
    
    if (getLimits) {
      PermissionProcessor.processLimits(permissionEntrySet, this.limitEnvVars, permissionLimitBeanMap);
    }
    
    return permissionLimitBeanMap;
  }

  /**
   * permission result gives helper methods in processing the results
   * @return the permission result
   */
  public PermissionResult findPermissionResult() {
    
    Set<PermissionEntry> permissionEntries = this.findPermissions();
    
    return new PermissionResult(permissionEntries);
  }
  
  /**
   * find a list of permissions
   * @return the set of permissions never null
   */
  public Set<PermissionEntry> findPermissions() {

    validateProcessor();

    Set<PermissionEntry> permissionEntries = null;
    
    if (pointInTimeFrom == null && pointInTimeTo == null) {
      if (this.permissionType == PermissionType.role_subject) {
        permissionEntries = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
            this.permissionDefIds, this.permissionNameIds, this.roleIds, this.actions, this.enabled, 
            this.memberIds, false, this.permissionNameFolder, this.permissionNameFolderScope, this.queryOptions);
      } else if (this.permissionType == PermissionType.role) {
        permissionEntries = GrouperDAOFactory.getFactory().getPermissionEntry().findRolePermissions(
            this.permissionDefIds, this.permissionNameIds, this.roleIds, this.actions, 
            this.enabled, false, this.permissionNameFolder, this.permissionNameFolderScope);
      } else {
        throw new RuntimeException("Not expecting permission type: " + this.permissionType);
      }
    } else {
      if (this.permissionType == PermissionType.role_subject) {
        if (this.permissionNameFolder != null) {
          throw new RuntimeException("Not implemented looking for permissions by folder and point in time");
        }
        permissionEntries = GrouperDAOFactory.getFactory().getPITPermissionAllView().findPermissions(
            permissionDefIds, permissionNameIds, roleIds, actions, memberIds, pointInTimeFrom, pointInTimeTo);
      } else {
        throw new RuntimeException("Not expecting permission type: " + this.permissionType);
      }
    }
    
    //if size is one, there arent redundancies to process
    if (this.permissionProcessor != null) {
      this.permissionProcessor.processPermissions(permissionEntries, this.limitEnvVars);
    }
    
    //if immediate only, do this after processing since it might affect the best decision
    if (this.immediateOnly) {
      //see if we are doing immediate only
      Iterator<PermissionEntry> iterator = GrouperUtil.nonNull(permissionEntries).iterator();
      while (iterator.hasNext()) {
        PermissionEntry permissionEntry = iterator.next();
        if (!permissionEntry.isImmediate(this.permissionType)) {
          iterator.remove();
        }
      }
    }
    
    return permissionEntries;
    
  }

  /**
   * validate that the processor dosent conflict with anything...
   */
  private void validateProcessor() {
    if (this.permissionProcessor != null && (this.enabled != null && !this.enabled)) {      
      throw new RuntimeException("You cannot process the permissions " +
          "(FILTER_REDUNDANT_PERMISSIONS || FILTER_REUNDANT_PERMISSIONS_AND_ROLES) " +
          "without looking for enabled permissions only");
    }
    
    //if processing permissions, just look at enabled
    if (this.permissionProcessor != null && this.enabled == null) {
      this.enabled = true;
    }
    
    // verify options for point in time queries
    if (pointInTimeFrom != null || pointInTimeTo != null) {
      if (limitEnvVars != null && limitEnvVars.size() > 0) {
        throw new RuntimeException("Cannot use limits for point in time queries.");
      }
      
      if (immediateOnly) {
        throw new RuntimeException("immediateOnly is not supported for point in time queries.");
      }
      
      if (enabled == null || !enabled) {
        throw new RuntimeException("Cannot search for disabled permissions for point in time queries.");
      }
      
      if (permissionType == PermissionType.role) {
        throw new RuntimeException("Permission type " + PermissionType.role.getName() + " is not supported for point in time queries.");
      }
      
      if (permissionProcessor != null) {
        if (permissionProcessor.isLimitProcessor()) {
          throw new RuntimeException("limit processors are not supported for point in time queries.");
        }
        
        if (pointInTimeFrom == null || pointInTimeTo == null || pointInTimeFrom.getTime() != pointInTimeTo.getTime()) {
          throw new RuntimeException("When using permission processors with point in time queries, queries have to be at a single point in time.");
        }
      }
    }
  }

  /**
   * find a permission
   * @param exceptionIfNotFound true if exception should be thrown if permission not found
   * @return the permission or null
   */
  public PermissionEntry findPermission(boolean exceptionIfNotFound) {

    Set<PermissionEntry> permissions = findPermissions();
    
    //this should find one if it is there...
    PermissionEntry permissionEntry = null;
    
    if (GrouperUtil.length(permissions) > 1) {
      throw new RuntimeException("Why is there more than one permission found? " + this);
    }
    
    if (GrouperUtil.length(permissions) == 1) {
      permissionEntry = permissions.iterator().next();
    }
    
    if (permissionEntry == null && exceptionIfNotFound) {
      throw new RuntimeException("could not find permission: " 
          + this);
    }
    return permissionEntry;
    
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (GrouperUtil.length(this.actions) > 0) {
      result.append("actions: ").append(GrouperUtil.toStringForLog(this.actions, 100));
    }
    if (GrouperUtil.length(this.permissionDefIds) > 0) {
      result.append("attributeDefIds: ").append(GrouperUtil.toStringForLog(this.permissionDefIds, 100));
    }
    if (GrouperUtil.length(this.permissionNameIds) > 0) {
      result.append("attributeDefNameIds: ").append(GrouperUtil.toStringForLog(this.permissionNameIds, 100));
    }
    if (enabled != null) {
      result.append("enabled: ").append(this.enabled);
    }
    if (this.immediateOnly) {
      result.append("immediateOnly: ").append(this.immediateOnly);
    }
    if (GrouperUtil.length(this.limitEnvVars) > 0) {
      result.append("limitEnvVars: ").append(GrouperUtil.toStringForLog(this.limitEnvVars, 100));
    }
    if (GrouperUtil.length(this.memberIds) > 0) {
      result.append("memberIds: ").append(GrouperUtil.toStringForLog(this.memberIds, 100));
    }
    if (this.permissionProcessor != null) {
      result.append("permissionProcessor: ").append(this.permissionProcessor);
    }
    if (this.permissionType != null) {
      result.append("permissionType: ").append(this.permissionType);
    }
    if (GrouperUtil.length(this.roleIds) > 0) {
      result.append("roleIds: ").append(GrouperUtil.toStringForLog(this.roleIds, 100));
    }
    return result.toString();
  }

  /**
   * To query permissions at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   * of the point in time query.  If this is specified but pointInTimeTo is not specified,
   * then the point in time query range will be from the time specified to now.
   */
  private Timestamp pointInTimeFrom = null;
  
  /**
   * To query permissions at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeFrom.  This parameter specifies the end of the range
   * of the point in time query.  If this is the same as pointInTimeFrom, then the query
   * will be done at a single point in time rather than a range.  If this is specified but
   * pointInTimeFrom is not specified, then the point in time query range will be from the
   * minimum point in time to the time specified.
   */
  private Timestamp pointInTimeTo = null;
  
  /**
   * To query permissions at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   * of the point in time query.  If this is specified but pointInTimeTo is not specified,
   * then the point in time query range will be from the time specified to now.
   * @param pointInTimeFrom 
   * @return this for changing
   */
  public PermissionFinder assignPointInTimeFrom(Timestamp pointInTimeFrom) {
    this.pointInTimeFrom = pointInTimeFrom;
    return this;
  }
  
  /**
   * To query permissions at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeFrom.  This parameter specifies the end of the range
   * of the point in time query.  If this is the same as pointInTimeFrom, then the query
   * will be done at a single point in time rather than a range.  If this is specified but
   * pointInTimeFrom is not specified, then the point in time query range will be from the
   * minimum point in time to the time specified.
   * @param pointInTimeTo 
   * @return this for changing
   */
  public PermissionFinder assignPointInTimeTo(Timestamp pointInTimeTo) {
    this.pointInTimeTo = pointInTimeTo;
    return this;
  }
}
