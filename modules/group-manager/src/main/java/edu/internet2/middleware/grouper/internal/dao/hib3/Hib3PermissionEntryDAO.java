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
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.internal.dao.hib3;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PermissionEntryDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionEntryImpl;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import java.sql.Timestamp;
import java.util.*;

/**
 * Basic Hibernate <code>PermissionEntry</code> DAO interface.
 * @author  Chris Hyzer
 * @version $Id: Hib3PermissionEntryDAO.java,v 1.4 2009-10-26 04:52:17 mchyzer Exp $
 */
public class Hib3PermissionEntryDAO extends Hib3DAO implements PermissionEntryDAO {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(Hib3PermissionEntryDAO.class);

  /** */
  private static final String KLASS = Hib3PermissionEntryDAO.class.getName();
  
  private static final String PERMISSION_ENTRY_COLUMNS = "gr.nameDb as roleName, gm.subjectSourceIdDb as subjectSourceId, gm.subjectIdDb as subjectId, gaaa.nameDb as action, gadn.nameDb as attributeDefNameName, gadn.displayNameDb as attributeDefNameDispName, gr.displayNameDb as roleDisplayName, gaa.attributeAssignDelegatableDb as attributeAssignDelegatableDb, gaa.enabledDb as enabledDb, gaa.enabledTimeDb as enabledTimeDb, gaa.disabledTimeDb as disabledTimeDb, gr.uuid as roleId, gadn.attributeDefId as attributeDefId, gm.uuid as memberId, gadn.id as attributeDefNameId, gaaa.id as actionId, gmav.depth as membershipDepth, grs.depth as roleSetDepth, gadns.depth as attributeDefNameSetDepth, gaaas.depth as attributeAssignActionSetDepth, gmav.uuid as membershipId, gaa.id as attributeAssignId, gaa.attributeAssignTypeDb as attributeAssignTypeDb, gaa.notes as assignmentNotes, gmav.enabledTimeDb as immediateMshipEnabledTimeDb, gmav.disabledTimeDb as immediateMshipDisabledTimeDb, gaa.disallowedDb as disallowedDb";

  private static final String PERMISSION_ENTRY_TABLES = "Group gr, MembershipEntry gmav, Member gm, Field gf, RoleSet grs, AttributeDef gad, AttributeAssign gaa, AttributeDefName gadn, AttributeDefNameSet gadns, AttributeAssignAction gaaa, AttributeAssignActionSet gaaas";
  
  private static final String PERMISSION_ENTRY_WHERE_CLAUSE = "gmav.ownerGroupId = gr.uuid and gmav.fieldId = gf.uuid and gr.typeOfGroupDb = 'role' and gf.typeString = 'list' and gf.name = 'members' and gmav.enabledDb = 'T' and gmav.memberUuid = gm.id and gadn.attributeDefId = gad.id and gad.attributeDefTypeDb = 'perm' and gaa.attributeDefNameId = gadns.ifHasAttributeDefNameId and gadn.id = gadns.thenHasAttributeDefNameId and gaa.attributeAssignActionId = gaaas.ifHasAttrAssignActionId and gaaa.id = gaaas.thenHasAttrAssignActionId and ((grs.ifHasRoleId = gr.uuid and gaa.ownerGroupId = grs.thenHasRoleId  and gaa.attributeAssignTypeDb = 'group') or (grs.ifHasRoleId = gr.uuid and grs.thenHasRoleId = gr.uuid and gmav.ownerGroupId = gaa.ownerGroupId and gmav.memberUuid = gaa.ownerMemberId and gaa.attributeAssignTypeDb = 'any_mem'))";
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.PermissionEntryDAO#findByMemberId(String)
   */
  public Set<PermissionEntry> findByMemberId(String memberId) {

    String sql = 
      "select distinct " + PERMISSION_ENTRY_COLUMNS + " from " + PERMISSION_ENTRY_TABLES + " where " + PERMISSION_ENTRY_WHERE_CLAUSE + " " +
      "and gm.uuid = :theMemberId ";
    
    Set<PermissionEntryImpl> permissionData = HibernateSession.byHqlStatic().createQuery(sql)
      .setString("theMemberId", memberId)
      .assignConvertHqlColumnsToObject(true)
      .listSet(PermissionEntryImpl.class);
    
    return new LinkedHashSet<PermissionEntry>(permissionData);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.PermissionEntryDAO#hasPermissionBySubjectIdSourceIdActionAttributeDefName(String, String, String, String)
   */
  public boolean hasPermissionBySubjectIdSourceIdActionAttributeDefName(String subjectId, String sourceId, 
      String action, String attributeDefNameName) {
    Long count = HibernateSession.byHqlStatic().createQuery(
        "select count(*) from " + PERMISSION_ENTRY_TABLES + " where " + PERMISSION_ENTRY_WHERE_CLAUSE + " " +
              "and gm.subjectIdDb = :theSubjectId " +
              "and gm.subjectSourceIdDb = :theSubjectSourceId " +
              "and gaaa.nameDb = :theAction " +
              "and gadn.nameDb = :theAttributeDefNameName")
        .setString("theSubjectId", subjectId)
        .setString("theSubjectSourceId", sourceId)
        .setString("theAction", action)
        .setString("theAttributeDefNameName", attributeDefNameName)
        .uniqueResult(Long.class);

    return count > 0;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PermissionEntryDAO#findByMemberIdAndAttributeDefNameId(String, String)
   */
  public Set<PermissionEntry> findByMemberIdAndAttributeDefNameId(String memberId,
      String attributeDefNameId) {
    Set<PermissionEntryImpl> permissionEntries = HibernateSession.byHqlStatic().createQuery(
      "select " + PERMISSION_ENTRY_COLUMNS + " from " + PERMISSION_ENTRY_TABLES + " where " + PERMISSION_ENTRY_WHERE_CLAUSE +
      " and gm.uuid = :theMemberId" +
      " and gadn.id = :theAttributeDefNameId")
      .setString("theMemberId", memberId)
      .setString("theAttributeDefNameId", attributeDefNameId)
      .assignConvertHqlColumnsToObject(true)
      .listSet(PermissionEntryImpl.class);
  
    return new LinkedHashSet<PermissionEntry>(permissionEntries);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PermissionEntryDAO#findPermissions(Collection, Collection, Collection, Collection, Boolean, Collection)
   */
  public Set<PermissionEntry> findPermissions(Collection<String> attributeDefIds,
      Collection<String> attributeDefNameIds, Collection<String> roleIds,
      Collection<String> actions, Boolean enabled, Collection<String> memberIds) {
    return findPermissions(attributeDefIds, attributeDefNameIds, roleIds, actions, enabled, memberIds, false);
  }
    
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PermissionEntryDAO#findPermissions(Collection, Collection, Collection, Collection, Boolean, Collection, boolean)
   */
  public Set<PermissionEntry> findPermissions(Collection<String> attributeDefIds,
      Collection<String> attributeDefNameIds, Collection<String> roleIds,
      Collection<String> actions, Boolean enabled, Collection<String> memberIdsTotal, boolean noEndDate) {
    return findPermissions(attributeDefIds, attributeDefNameIds, roleIds, actions, enabled, memberIdsTotal, false, null, null);
  }
    
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PermissionEntryDAO#findPermissions(Collection, Collection, Collection, Collection, Boolean, Collection, boolean, Stem, Scope)
   */
  public Set<PermissionEntry> findPermissions(Collection<String> attributeDefIds,
      Collection<String> attributeDefNameIds, Collection<String> roleIds,
      Collection<String> actions, Boolean enabled, Collection<String> memberIdsTotal, 
      boolean noEndDate, Stem permissionNameInStem, Scope permissionNameInStemScope) {
    
    Set<PermissionEntry> totalResults = new LinkedHashSet<PermissionEntry>();

    int numberOfMemberBatches = GrouperUtil.batchNumberOfBatches(memberIdsTotal, 100);
    
    boolean hasMemberBatches = numberOfMemberBatches > 0;
    //there needs to be at least one batch
    numberOfMemberBatches = numberOfMemberBatches == 0 ? 1 : numberOfMemberBatches;
    
    List<String> membersIdsTotalList = memberIdsTotal instanceof List ? (List)memberIdsTotal 
        : new ArrayList<String>(GrouperUtil.nonNull(memberIdsTotal));
    
    for (int memberBatchIndex=0;memberBatchIndex<numberOfMemberBatches;memberBatchIndex++) {
      
      //if no batches, just use null
      List<String> memberIds = hasMemberBatches ? GrouperUtil.batchList(membersIdsTotalList, 100, memberBatchIndex) : null;
      
      int memberIdsSize = GrouperUtil.length(memberIds);
      int roleIdsSize = GrouperUtil.length(roleIds);
      int actionsSize = GrouperUtil.length(actions);
      int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
      int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
      
      //if (memberIdsSize == 0 && roleIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      //  throw new RuntimeException("Illegal query, you need to pass in members and/or attributeDefId(s) and/or roleId(s) and/or roleNames and/or attributeDefNameIds");
      //}
      
      //too many bind vars... note, we can batch up the memberIds
      if (memberIdsSize + roleIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 180) {
        throw new RuntimeException("Too many memberIdsSize " + memberIdsSize 
            + " roleIdsSize " + roleIdsSize + " or attributeDefIdsSize " 
            + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
      }

      
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

      String selectPrefix = "select distinct " + PERMISSION_ENTRY_COLUMNS + " ";
      
      //doesnt work due to composite key, hibernate puts parens around it and mysql fails
      //String countPrefix = "select count(distinct pea) ";
      
      StringBuilder sqlTables = new StringBuilder(" from " + PERMISSION_ENTRY_TABLES + " ");

      if (permissionNameInStem != null && permissionNameInStemScope == Scope.ONE) {
        sqlTables.append(" , AttributeDefName adn2 ");
      }
      
      StringBuilder sqlWhereClause = new StringBuilder(" " + PERMISSION_ENTRY_WHERE_CLAUSE + " ");
      
      GrouperSession grouperSession = GrouperSession.staticGrouperSession();
      
      Subject grouperSessionSubject = grouperSession.getSubject();
      
      grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "gadn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
      
      boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
          grouperSessionSubject, byHqlStatic, 
          sqlTables, "gr.uuid", AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES);

      StringBuilder sql;
      if (changedQuery) {
        if (sqlWhereClause.length() > 0) {
          sql = sqlTables.append(" and ").append(sqlWhereClause);
        } else {
          throw new RuntimeException("Unexpected.");
        }
      } else {
        sql = sqlTables.append(" where ").append(sqlWhereClause);
      }
      
      if (enabled != null && enabled) {
        sql.append(" and gaa.enabledDb = 'T' ");
      }
      if (enabled != null && !enabled) {
        sql.append(" and gaa.enabledDb = 'F' ");
      }
      
      if (permissionNameInStem != null) {
        switch (permissionNameInStemScope) {
          case ONE:
            sql.append(" and gadn.id = adn2.id and adn2.stemId = :stemId ");
            byHqlStatic.setString("stemId", permissionNameInStem.getUuid());
            break;
          case SUB:
            
            sql.append(" and gadn.nameDb like :stemSub ");
            byHqlStatic.setString("stemSub", permissionNameInStem.getName() + ":%");
            
            break;
          default:
            throw new RuntimeException("Not expecting permissionNameInStemScope: " + permissionNameInStemScope);
        }
      }

      
      if (noEndDate) {
        sql.append(" and gmav.disabledTimeDb is null ");
        sql.append(" and gaa.disabledTimeDb is null ");
      }
      
      if (actionsSize > 0) {
        sql.append(" and gaaa.nameDb in (");
        sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
        sql.append(") ");
      }
      if (roleIdsSize > 0) {
        sql.append(" and gr.uuid in (");
        sql.append(HibUtils.convertToInClause(roleIds, byHqlStatic));
        sql.append(") ");
      }
      if (attributeDefIdsSize > 0) {
        sql.append(" and gadn.attributeDefId in (");
        sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
        sql.append(") ");
      }
      if (attributeDefNameIdsSize > 0) {
        sql.append(" and gadn.id in (");
        sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
        sql.append(") ");
      }
      if (memberIdsSize > 0) {
        sql.append(" and gm.uuid in (");
        sql.append(HibUtils.convertToInClause(memberIds, byHqlStatic));
        sql.append(") ");
      }
      
      QueryOptions queryOptions = new QueryOptions();
      QuerySort querySort = new QuerySort("gm.subjectIdDb", true);
      querySort.insertSortToBeginning("gaaa.nameDb", true);
      querySort.insertSortToBeginning("gr.displayNameDb", true);
      querySort.insertSortToBeginning("gadn.displayNameDb", true);
      queryOptions.sort(querySort);
      
      System.out.println("TEST TEST TEST");
      
      byHqlStatic
        .setCacheable(false)
        .setCacheRegion(KLASS + ".findPermissions").options(queryOptions);

      int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findPermissions.maxResultSize", 30000);
      
      String sqlString = sql.toString();
      
      //if we did where and, then switch to where
      sqlString = sqlString.replaceAll("where\\s+and", "where");

      //if we end in where, strip it out
      sqlString = sqlString.trim();
      if (sqlString.endsWith("where")) {
        sqlString = sqlString.substring(0, sqlString.length()-5);
      }

      Set<PermissionEntryImpl> permissionData = byHqlStatic.createQuery(selectPrefix + sqlString)
        .assignConvertHqlColumnsToObject(true)
        .listSet(PermissionEntryImpl.class);
      
      int size = GrouperUtil.length(permissionData);
      if (maxAssignments >= 0) {

        //doesnt work on mysql i think due to hibernate and composite key
        //size = byHqlStatic.createQuery(countPrefix + sqlString).uniqueResult(long.class);    
        
        //see if too many
        if (size > maxAssignments) {
          throw new RuntimeException("Too many results: " + size);
        }
        
      }
      

      //nothing to filter
      if (size == 0) {
        continue;
      }
      
      Set<PermissionEntry> results = new LinkedHashSet<PermissionEntry>(permissionData);
      
      //if the hql didnt filter, we need to do that here
      results = grouperSession.getAttributeDefResolver().postHqlFilterPermissions(grouperSessionSubject, results);
      
      //we should be down to the secure list
      totalResults.addAll(results);
    }
      
    return totalResults;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PermissionEntryDAO#findPermissions(Collection, Collection, Collection, Collection, Boolean, Collection, boolean, Stem, Scope, QueryOptions)
   */
  public Set<PermissionEntry> findPermissions(Collection<String> attributeDefIds,
      Collection<String> attributeDefNameIds, Collection<String> roleIds,
      Collection<String> actions, Boolean enabled, Collection<String> memberIdsTotal, 
      boolean noEndDate, Stem permissionNameInStem, Scope permissionNameInStemScope, QueryOptions queryOptions) {
    
    Set<PermissionEntry> totalResults = new LinkedHashSet<PermissionEntry>();

    int numberOfMemberBatches = GrouperUtil.batchNumberOfBatches(memberIdsTotal, 100);
    
    boolean hasMemberBatches = numberOfMemberBatches > 0;
    //there needs to be at least one batch
    numberOfMemberBatches = numberOfMemberBatches == 0 ? 1 : numberOfMemberBatches;
    
    List<String> membersIdsTotalList = memberIdsTotal instanceof List ? (List)memberIdsTotal 
        : new ArrayList<String>(GrouperUtil.nonNull(memberIdsTotal));
    
    for (int memberBatchIndex=0;memberBatchIndex<numberOfMemberBatches;memberBatchIndex++) {
      
      //if no batches, just use null
      List<String> memberIds = hasMemberBatches ? GrouperUtil.batchList(membersIdsTotalList, 100, memberBatchIndex) : null;
      
      int memberIdsSize = GrouperUtil.length(memberIds);
      int roleIdsSize = GrouperUtil.length(roleIds);
      int actionsSize = GrouperUtil.length(actions);
      int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
      int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
      
      //if (memberIdsSize == 0 && roleIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      //  throw new RuntimeException("Illegal query, you need to pass in members and/or attributeDefId(s) and/or roleId(s) and/or roleNames and/or attributeDefNameIds");
      //}
      
      //too many bind vars... note, we can batch up the memberIds
      if (memberIdsSize + roleIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 180) {
        throw new RuntimeException("Too many memberIdsSize " + memberIdsSize 
            + " roleIdsSize " + roleIdsSize + " or attributeDefIdsSize " 
            + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
      }

      
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

      String selectPrefix = "select distinct " + PERMISSION_ENTRY_COLUMNS + " ";
      
      //doesnt work due to composite key, hibernate puts parens around it and mysql fails
      //String countPrefix = "select count(distinct pea) ";
      
      StringBuilder sqlTables = new StringBuilder(" from " + PERMISSION_ENTRY_TABLES + " ");

      if (permissionNameInStem != null && permissionNameInStemScope == Scope.ONE) {
        sqlTables.append(" , AttributeDefName adn2 ");
      }
      
      StringBuilder sqlWhereClause = new StringBuilder(" " + PERMISSION_ENTRY_WHERE_CLAUSE + " ");
      
      GrouperSession grouperSession = GrouperSession.staticGrouperSession();
      
      Subject grouperSessionSubject = grouperSession.getSubject();
      
      grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "gadn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
      
      boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
          grouperSessionSubject, byHqlStatic, 
          sqlTables, "gr.uuid", AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES);

      StringBuilder sql;
      if (changedQuery) {
        if (sqlWhereClause.length() > 0) {
          sql = sqlTables.append(" and ").append(sqlWhereClause);
        } else {
          throw new RuntimeException("Unexpected.");
        }
      } else {
        sql = sqlTables.append(" where ").append(sqlWhereClause);
      }
      
      if (enabled != null && enabled) {
        sql.append(" and gaa.enabledDb = 'T' ");
      }
      if (enabled != null && !enabled) {
        sql.append(" and gaa.enabledDb = 'F' ");
      }
      
      if (permissionNameInStem != null) {
        switch (permissionNameInStemScope) {
          case ONE:
            sql.append(" and gadn.id = adn2.id and adn2.stemId = :stemId ");
            byHqlStatic.setString("stemId", permissionNameInStem.getUuid());
            break;
          case SUB:
            
            sql.append(" and gadn.nameDb like :stemSub ");
            byHqlStatic.setString("stemSub", permissionNameInStem.getName() + ":%");
            
            break;
          default:
            throw new RuntimeException("Not expecting permissionNameInStemScope: " + permissionNameInStemScope);
        }
      }

      
      if (noEndDate) {
        sql.append(" and gmav.disabledTimeDb is null ");
        sql.append(" and gaa.disabledTimeDb is null ");
      }
      
      if (actionsSize > 0) {
        sql.append(" and gaaa.nameDb in (");
        sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
        sql.append(") ");
      }
      if (roleIdsSize > 0) {
        sql.append(" and gr.uuid in (");
        sql.append(HibUtils.convertToInClause(roleIds, byHqlStatic));
        sql.append(") ");
      }
      if (attributeDefIdsSize > 0) {
        sql.append(" and gadn.attributeDefId in (");
        sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
        sql.append(") ");
      }
      if (attributeDefNameIdsSize > 0) {
        sql.append(" and gadn.id in (");
        sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
        sql.append(") ");
      }
      if (memberIdsSize > 0) {
        sql.append(" and gm.uuid in (");
        sql.append(HibUtils.convertToInClause(memberIds, byHqlStatic));
        sql.append(") ");
      }
      
      if (queryOptions == null) {
        queryOptions = new QueryOptions();
      }
      // don't let the client override the sorting settings
      QuerySort querySort = new QuerySort("gm.subjectIdDb", true);
      querySort.insertSortToBeginning("gaaa.nameDb", true);
      querySort.insertSortToBeginning("gr.displayNameDb", true);
      querySort.insertSortToBeginning("gadn.displayNameDb", true);
      queryOptions.sort(querySort);
      
            
      byHqlStatic
        .setCacheable(false)
        .setCacheRegion(KLASS + ".findPermissions").options(queryOptions);

      int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findPermissions.maxResultSize", 30000);
      
      String sqlString = sql.toString();
      
      //if we did where and, then switch to where
      sqlString = sqlString.replaceAll("where\\s+and", "where");

      //if we end in where, strip it out
      sqlString = sqlString.trim();
      if (sqlString.endsWith("where")) {
        sqlString = sqlString.substring(0, sqlString.length()-5);
      }

      Set<PermissionEntryImpl> permissionData = byHqlStatic.createQuery(selectPrefix + sqlString)
        .assignConvertHqlColumnsToObject(true)
        .listSet(PermissionEntryImpl.class);
      
      int size = GrouperUtil.length(permissionData);
      if (maxAssignments >= 0) {

        //doesnt work on mysql i think due to hibernate and composite key
        //size = byHqlStatic.createQuery(countPrefix + sqlString).uniqueResult(long.class);    
        
        //see if too many
        if (size > maxAssignments) {
          throw new RuntimeException("Too many results: " + size);
        }
        
      }
      

      //nothing to filter
      if (size == 0) {
        continue;
      }
      
      Set<PermissionEntry> results = new LinkedHashSet<PermissionEntry>(permissionData);
      
      //if the hql didnt filter, we need to do that here
      results = grouperSession.getAttributeDefResolver().postHqlFilterPermissions(grouperSessionSubject, results);
      
      //we should be down to the secure list
      totalResults.addAll(results);
    }
      
    return totalResults;
  }

  /**
   * @see PermissionEntryDAO#findPermissionsByAttributeDefDisabledRange(String, Timestamp, Timestamp)
   * find permissions by attribute definition which are about to expire
   */
  public Set<PermissionEntry> findPermissionsByAttributeDefDisabledRange(
      String attributeDefId, Timestamp disabledDateFrom, Timestamp disabledDateTo) {
    
    if (disabledDateFrom == null && disabledDateTo == null) {
      throw new RuntimeException("Need to pass in disabledFrom or disabledTo");
    }
    
    //if they got it backwards, then fix it for them
    if (disabledDateFrom != null && disabledDateTo != null 
        && disabledDateFrom.getTime() > disabledDateTo.getTime()) {
      
      Timestamp temp = disabledDateFrom;
      disabledDateFrom = disabledDateTo;
      disabledDateTo = temp;
      
    }
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder sql = new StringBuilder(
        "select " + PERMISSION_ENTRY_COLUMNS + " from " + PERMISSION_ENTRY_TABLES + ", AttributeDefName theAttributeDefName where " + PERMISSION_ENTRY_WHERE_CLAUSE
        + " and gadn.id = theAttributeDefName.id "
        + " and theAttributeDefName.attributeDefId   = :theAttributeDefId "
        + " and gm.uuid is not null "
        + " and gaa.enabledDb = 'T' ");
    
    if (disabledDateFrom != null) {
      sql.append(" and gaa.disabledTimeDb >= :disabledDateFrom ");
      byHqlStatic.setLong( "disabledDateFrom" , disabledDateFrom.getTime() );
    }
    if (disabledDateTo != null) {
      sql.append(" and gaa.disabledTimeDb <= :disabledDateTo ");
      byHqlStatic.setLong( "disabledDateTo" , disabledDateTo.getTime() );
    }

    sql.append(
        " and not exists ( select gaaInner.id from " + PERMISSION_ENTRY_TABLES.replaceAll("(\\w+) (\\w+)", "$1 $2Inner") + " where " + PERMISSION_ENTRY_WHERE_CLAUSE.replace(".", "Inner.") +
        " and gadnInner.id = gadn.id " +
        " and gaaaInner.id = gaaa.id " +
        //note, who cares which role it is, if the user has the permission...  (not exactly right if not flattening permissions, but thats ok)
        //" and validPermissionEntry.roleId = thePermissionEntry.roleId " +
        " and gmInner.uuid = gm.uuid " +
        " and gaaInner.enabledDb = 'T' and ( gaaInner.disabledTimeDb is null ");

    if (disabledDateTo != null) {
      sql.append(" or gaaInner.disabledTimeDb > :disabledDateTo ");
    } else if (disabledDateFrom != null) {
      sql.append(" or gaaInner.disabledTimeDb < :disabledDateFrom ");
    }
    
    
    sql.append(") )");
    
    Set<PermissionEntryImpl> permissionEntries = byHqlStatic
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindPermissionsByAttributeDefDisabledRange")
      .setString( "theAttributeDefId" , attributeDefId )
      .assignConvertHqlColumnsToObject(true)
      .listSet(PermissionEntryImpl.class);

    return new LinkedHashSet<PermissionEntry>(permissionEntries);

    
  }

  /**
   * @see PermissionEntryDAO#findAllPermissionsNotInGroupAndType(String, String, boolean, QueryOptions, Boolean, boolean)
   */
  public Set<PermissionEntry> findAllPermissionsNotInGroupAndType(String attributeDefId,
      String groupId, boolean immediateRoleMembershipsOrRoleSubject, QueryOptions queryOptions,
      Boolean enabled, boolean hasNoEndDate) {

    StringBuilder sql = new StringBuilder(
        "select " + PERMISSION_ENTRY_COLUMNS + " from " + PERMISSION_ENTRY_TABLES + ", AttributeDefName theAttributeDefName where " + PERMISSION_ENTRY_WHERE_CLAUSE
        + " and gadn.id = theAttributeDefName.id "
        + " and theAttributeDefName.attributeDefId   = :theAttributeDefId "
        + " and gm.uuid is not null ");
    
    if (enabled != null) {
      sql.append(" and gaa.enabledDb = 'T' ");
    }

    if (immediateRoleMembershipsOrRoleSubject) {
      //either t
      sql.append(" and (gmav.depth = 0 " );
      sql.append(" or gaa.attributeAssignTypeDb != 'group' ) " );
    }

    if (hasNoEndDate) {
      sql.append(" and gaa.disabledTimeDb is null ");
      sql.append(" and gmav.disabledTimeDb is null ");
    }

    sql.append(" and  gm.uuid not in ( select notInMembershipEntry.memberUuid from MembershipEntry as notInMembershipEntry " +
        " where notInMembershipEntry.ownerGroupId = :ownerGroupId "
        + " and notInMembershipEntry.fieldId = '" + Group.getDefaultList().getUuid() + "' ");
    if (enabled != null) {
      if (enabled) {
        sql.append(" and notInMembershipEntry.enabledDb = 'T' ");
      } else {
        sql.append(" and notInMembershipEntry.enabledDb = 'F' ");
      }
    }
    sql.append(" ) ");
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    Set<PermissionEntryImpl> permissionEntries = byHqlStatic
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllPermissionsNotInGroupAndType")
      .setString( "theAttributeDefId" , attributeDefId )
      .setString( "ownerGroupId" , groupId )
      .assignConvertHqlColumnsToObject(true)
      .listSet(PermissionEntryImpl.class);

    return new LinkedHashSet<PermissionEntry>(permissionEntries);

  
  }

  /**
   * @see PermissionEntryDAO#findAllPermissionsNotInStem(String, Stem, Stem.Scope, boolean, QueryOptions, Boolean, boolean)
   */
  public Set<PermissionEntry> findAllPermissionsNotInStem(String attributeDefId,
      Stem ownerNotInStem, Stem.Scope stemScope,  boolean immediateRoleMembershipsOrRoleSubject,
      QueryOptions queryOptions, Boolean enabled, boolean hasNoEndDate) {

    StringBuilder sql = new StringBuilder(
        "select " + PERMISSION_ENTRY_COLUMNS + " from " + PERMISSION_ENTRY_TABLES + ", AttributeDefName theAttributeDefName where " + PERMISSION_ENTRY_WHERE_CLAUSE
        + " and gadn.id = theAttributeDefName.id "
        + " and theAttributeDefName.attributeDefId   = :theAttributeDefId "
        + " and gm.uuid is not null ");
    
    if (enabled != null) {
      sql.append(" and gaa.enabledDb = 'T' ");
    }

    if (immediateRoleMembershipsOrRoleSubject) {
      //either t
      sql.append(" and (gmav.depth = 0 " );
      sql.append(" or gaa.attributeAssignTypeDb != 'group' ) " );
    }

    if (hasNoEndDate) {
      sql.append(" and gaa.disabledTimeDb is null ");
      sql.append(" and gmav.disabledTimeDb is null ");
    }

    sql.append(" and  not exists ( select notInMembershipEntry.memberUuid " +
        " from MembershipEntry as notInMembershipEntry, Group as theStemGroup " +
            " where notInMembershipEntry.ownerGroupId = theStemGroup.uuid "
            + " and notInMembershipEntry.memberUuid = gm.uuid "
            + " and notInMembershipEntry.fieldId = '" + Group.getDefaultList().getUuid() + "' ");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    switch (stemScope) {
      case ONE:
        
        sql.append(" and theStemGroup.parentUuid = :stemId ");
        byHqlStatic.setString("stemId", ownerNotInStem.getUuid());
        break;

      case SUB:
        
        sql.append(" and theStemGroup.nameDb like :stemSub ");
        byHqlStatic.setString("stemSub", ownerNotInStem.getName() + ":%");
        
        break;
      default:
        throw new RuntimeException("Not expecting scope: " + stemScope);
    }
    
    sql.append(" ) ");
            
    
    Set<PermissionEntryImpl> permissionEntries = byHqlStatic
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllPermissionsNotInStem")
      .setString( "theAttributeDefId" , attributeDefId )
      .assignConvertHqlColumnsToObject(true)
      .listSet(PermissionEntryImpl.class);

    return new LinkedHashSet<PermissionEntry>(permissionEntries);

  }
  
  /**
   * find permissions based on filter criteria
   */
  public Set<PermissionEntry> findPermissions(String attributeDefId,
      String attributeDefNameId, String ownerRoleId, String ownerMemberId,
      String action, Boolean enabled) {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    String selectPrefix = "select distinct " + PERMISSION_ENTRY_COLUMNS + " ";
    
    //doesnt work due to composite key, hibernate puts parens around it and mysql fails
    //String countPrefix = "select count(distinct pea) ";
    
    StringBuilder sqlTables = new StringBuilder(" from " + PERMISSION_ENTRY_TABLES + " ");
    
    StringBuilder sqlWhereClause = new StringBuilder(" " + PERMISSION_ENTRY_WHERE_CLAUSE + " ");
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "gadn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
    
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "gr.uuid", AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES);

    StringBuilder sql;
    if (changedQuery) {
      if (sqlWhereClause.length() > 0) {
        sql = sqlTables.append(" and ").append(sqlWhereClause);
      } else {
        throw new RuntimeException("Unexpected.");
      }
    } else {
      sql = sqlTables.append(" where ").append(sqlWhereClause);
    }
    
    if (enabled != null && enabled) {
      sql.append(" and gaa.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and gaa.enabledDb = 'F' ");
    }
    
    if (!StringUtils.isBlank(ownerRoleId)) {
      sql.append(" and gr.uuid = :theOwnerRoleId ");
      byHqlStatic.setString("theOwnerRoleId", ownerRoleId);
    }
    
    if (!StringUtils.isBlank(action)) {
      sql.append(" and gaaa.nameDb = :theAction ");
      byHqlStatic.setString("theAction", action);
    }

    if (!StringUtils.isBlank(attributeDefId)) {
      sql.append(" and gadn.attributeDefId = :theAttributeDefId ");
      byHqlStatic.setString("theAttributeDefId", attributeDefId);
    }
    if (!StringUtils.isBlank(attributeDefNameId)) {
      sql.append(" and gadn.id = :theAttributeDefNameId ");
      byHqlStatic.setString("theAttributeDefNameId", attributeDefNameId);
    }
    if (!StringUtils.isBlank(ownerMemberId)) {
      sql.append(" and gm.uuid = :theOwnerMemberId ");
      byHqlStatic.setString("theOwnerMemberId", ownerMemberId);
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".findPermissions");

    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findPermissions.maxResultSize", 30000);
    
    String sqlString = sql.toString();
    
    //if we did where and, then switch to where
    sqlString = sqlString.replaceAll("where\\s+and", "where");
    sqlString = sqlString.replaceAll("where\\s*$", "");
    
    Set<PermissionEntryImpl> resultsTemp = byHqlStatic.createQuery(selectPrefix + sqlString)
      .assignConvertHqlColumnsToObject(true)
      .listSet(PermissionEntryImpl.class);

    int size = GrouperUtil.length(resultsTemp);
    if (maxAssignments >= 0) {

      //doesnt work on mysql i think due to hibernate and composite key
      //size = byHqlStatic.createQuery(countPrefix + sqlString).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    
    Set<PermissionEntry> results = new LinkedHashSet<PermissionEntry>(resultsTemp);
    

    //nothing to filter
    if (size == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = grouperSession.getAttributeDefResolver().postHqlFilterPermissions(grouperSessionSubject, results);
    
    //we should be down to the secure list
    return results;
  }

  /**
   * @see PermissionEntry#findRolePermissions(String attributeDefId, String attributeDefNameId, String ownerRoleId, String action, Boolean enabled)
   */
  public Set<PermissionEntry> findRolePermissions(String attributeDefId,
      String attributeDefNameId, String ownerRoleId, String action, Boolean enabled) {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    String selectPrefix = "select distinct pea ";
    
    //doesnt work due to composite key, hibernate puts parens around it and mysql fails
    //String countPrefix = "select count(distinct pea) ";
    
    StringBuilder sqlTables = new StringBuilder(" from PermissionEntryRoleAssigned pea ");
    
    StringBuilder sqlWhereClause = new StringBuilder("");
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "pea.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
    
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "pea.roleId", AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES);

    StringBuilder sql;
    if (changedQuery) {
      if (sqlWhereClause.length() > 0) {
        sql = sqlTables.append(" and ").append(sqlWhereClause);
      } else {
        sql = sqlTables;
      }
    } else {
      sql = sqlTables.append(" where ").append(sqlWhereClause);
    }
    
    if (enabled != null && enabled) {
      sql.append(" and pea.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and pea.enabledDb = 'F' ");
    }
    
    if (!StringUtils.isBlank(ownerRoleId)) {
      sql.append(" and pea.roleId = :theOwnerRoleId ");
      byHqlStatic.setString("theOwnerRoleId", ownerRoleId);
    }
    
    if (!StringUtils.isBlank(action)) {
      sql.append(" and pea.action = :theAction ");
      byHqlStatic.setString("theAction", action);
    }

    if (!StringUtils.isBlank(attributeDefId)) {
      sql.append(" and pea.attributeDefId = :theAttributeDefId ");
      byHqlStatic.setString("theAttributeDefId", attributeDefId);
    }
    if (!StringUtils.isBlank(attributeDefNameId)) {
      sql.append(" and pea.attributeDefNameId = :theAttributeDefNameId ");
      byHqlStatic.setString("theAttributeDefNameId", attributeDefNameId);
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".findRolePermissions");

    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findPermissions.maxResultSize", 30000);
    
    String sqlString = sql.toString();
    
    //if we did where and, then switch to where
    sqlString = sqlString.replaceAll("where\\s+and", "where");
    sqlString = sqlString.replaceAll("where\\s*$", "");
    
    Set<PermissionEntry> results = byHqlStatic.createQuery(selectPrefix + sqlString).listSet(PermissionEntry.class);

    int size = GrouperUtil.length(results);
    if (maxAssignments >= 0) {

      //doesnt work on mysql i think due to hibernate and composite key
      //size = byHqlStatic.createQuery(countPrefix + sqlString).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    

    //nothing to filter
    if (size == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = grouperSession.getAttributeDefResolver().postHqlFilterPermissions(grouperSessionSubject, results);
    
    //we should be down to the secure list
    return results;

  }

  /**
   * @see PermissionEntryDAO#findRolePermissions(Collection, Collection, Collection, Collection, Boolean, boolean)
   */
  public Set<PermissionEntry> findRolePermissions(Collection<String> attributeDefIds,
      Collection<String> attributeDefNameIds, Collection<String> roleIds,
      Collection<String> actions, Boolean enabled, boolean noEndDate) {
    return findRolePermissions(attributeDefIds, attributeDefNameIds, roleIds, actions, enabled, noEndDate, null, null);
  }

  /**
   * @see PermissionEntryDAO#findRolePermissions(Collection, Collection, Collection, Collection, Boolean, boolean, Stem, Scope)
   */
  public Set<PermissionEntry> findRolePermissions(Collection<String> attributeDefIds,
      Collection<String> attributeDefNameIds, Collection<String> roleIds,
      Collection<String> actions, Boolean enabled, boolean noEndDate, 
      Stem permissionNameInStem, Scope permissionNameInStemScope) {
    int roleIdsSize = GrouperUtil.length(roleIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);

    //too many bind vars
    if (roleIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 100) {
      throw new RuntimeException("Too many roleIdsSize " + roleIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
    }

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    String selectPrefix = "select distinct pea ";

    //doesnt work due to composite key, hibernate puts parens around it and mysql fails
    //String countPrefix = "select count(distinct pea) ";

    StringBuilder sqlTables = new StringBuilder(" from PermissionEntryRoleAssigned pea ");

    if (permissionNameInStem != null && permissionNameInStemScope == Scope.ONE) {
      sqlTables.append(" , AttributeDefName adn ");
    }

    StringBuilder sqlWhereClause = new StringBuilder("");

    GrouperSession grouperSession = GrouperSession.staticGrouperSession();

    Subject grouperSessionSubject = grouperSession.getSubject();

    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "pea.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
    
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "pea.roleId", AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES);

    StringBuilder sql;
    if (changedQuery) {
      if (sqlWhereClause.length() > 0) {
        sql = sqlTables.append(" and ").append(sqlWhereClause);
      } else {
        sql = sqlTables;
      }
    } else {
      sql = sqlTables.append(" where ").append(sqlWhereClause);
    }
    
    if (enabled != null && enabled) {
      sql.append(" and pea.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and pea.enabledDb = 'F' ");
    }
    
    if (noEndDate) {
      sql.append(" and pea.disabledTimeDb is null ");
    }

    if (permissionNameInStem != null) {
      switch (permissionNameInStemScope) {
        case ONE:
          sql.append(" and pea.attributeDefNameId = adn.id and adn.stemId = :stemId ");
          byHqlStatic.setString("stemId", permissionNameInStem.getUuid());
          break;
        case SUB:
          
          sql.append(" and pea.attributeDefNameName like :stemSub ");
          byHqlStatic.setString("stemSub", permissionNameInStem.getName() + ":%");
          
          break;
        default:
          throw new RuntimeException("Not expecting permissionNameInStemScope: " + permissionNameInStemScope);
      }
    }

    
    if (actionsSize > 0) {
      sql.append(" and pea.action in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (roleIdsSize > 0) {
      sql.append(" and pea.roleId in (");
      sql.append(HibUtils.convertToInClause(roleIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and pea.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and pea.attributeDefNameId in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
    
    QueryOptions queryOptions = new QueryOptions();
    QuerySort querySort = new QuerySort("pea.action", true);
    querySort.insertSortToBeginning("pea.roleDisplayName", true);
    querySort.insertSortToBeginning("pea.attributeDefNameDispName", true);
    queryOptions.sort(querySort);
    
    byHqlStatic
      .setCacheable(false).options(queryOptions)
      .setCacheRegion(KLASS + ".findRolePermissions");

    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findPermissions.maxResultSize", 30000);
    
    String sqlString = sql.toString();
    
    //if we did where and, then switch to where
    sqlString = sqlString.replaceAll("where\\s+and", "where");
    
    Set<PermissionEntry> results = byHqlStatic.createQuery(selectPrefix + sqlString).listSet(PermissionEntry.class);

    int size = GrouperUtil.length(results);
    if (maxAssignments >= 0) {

      //doesnt work on mysql i think due to hibernate and composite key
      //size = byHqlStatic.createQuery(countPrefix + sqlString).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    

    //nothing to filter
    if (size == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = grouperSession.getAttributeDefResolver().postHqlFilterPermissions(grouperSessionSubject, results);
    
    //we should be down to the secure list
    return results;
      
  }
}
