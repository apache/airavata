/**
 * 
 */
package org.apache.airavata.grouper.group;

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import org.apache.airavata.grouper.SubjectType;

import java.util.ArrayList;
import java.util.List;

import static edu.internet2.middleware.grouper.misc.SaveMode.INSERT_OR_UPDATE;
import static edu.internet2.middleware.subject.provider.SubjectTypeEnum.PERSON;
import static org.apache.airavata.grouper.AiravataGrouperUtil.*;
import static org.apache.airavata.grouper.group.GroupMembershipType.DIRECT;
import static org.apache.airavata.grouper.group.GroupMembershipType.INDIRECT;

/**
 * @author vsachdeva
 *
 */
public class GroupServiceImpl implements GroupService {
  
  
  public void createOrUpdateGroup(Group group) {
    
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      GroupSave groupSave = new GroupSave(grouperSession);
      groupSave.assignTypeOfGroup(TypeOfGroup.group);
      groupSave.assignGroupNameToEdit(GROUPS_STEM_NAME+COLON+group.getId());
      groupSave.assignName(GROUPS_STEM_NAME+COLON+group.getId());
      groupSave.assignDisplayExtension(group.getName());
      groupSave.assignDescription(group.getDescription());
      groupSave.assignSaveMode(INSERT_OR_UPDATE);
      groupSave.assignCreateParentStemsIfNotExist(true);
      edu.internet2.middleware.grouper.Group grp = groupSave.save();
      for (String userId: group.getUsers()) {
        Subject subject = SubjectFinder.findByIdAndSource(userId, SUBJECT_SOURCE, false);
        if (subject == null) {
          throw new SubjectNotFoundException(userId+" airavata internal user id was not found.");
        }
        grp.addMember(subject, false);
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public void deleteGroup(String groupId) throws GroupNotFoundException {
    
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      edu.internet2.middleware.grouper.Group group = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+groupId, 
          true, new QueryOptions().secondLevelCache(false));
      group.delete();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public Group getGroup(String groupId) throws GroupNotFoundException {
    
    GrouperSession grouperSession = null;
    Group group = new Group();
    try {
      grouperSession = GrouperSession.startRootSession();
      edu.internet2.middleware.grouper.Group grouperGroup = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+groupId, true);
      group.setId(grouperGroup.getExtension());
      group.setName(grouperGroup.getDisplayExtension());
      group.setDescription(grouperGroup.getDescription());
      List<String> users = new ArrayList<String>();
      for(Member member: grouperGroup.getMembers()) {
        if (member.getSubjectType().equals(PERSON)) {
          users.add(member.getSubjectId());
        }
      }
      group.setUsers(users);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    return group;
  }
  
  public void addGroupToGroup(String parentGroupId, String childGroupId) throws GroupNotFoundException {
    
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      edu.internet2.middleware.grouper.Group grouperParentGroup = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+parentGroupId, true);
      edu.internet2.middleware.grouper.Group grouperChildGroup = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+childGroupId, true);
      Subject subject = SubjectFinder.findById(grouperChildGroup.getId(), false);
      if (subject == null) {
        throw new GroupNotFoundException(childGroupId+" was not found.");
      }
      grouperParentGroup.addMember(subject, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public void removeGroupFromGroup(String parentGroupId, String childGroupId) throws GroupNotFoundException {
    
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      edu.internet2.middleware.grouper.Group grouperParentGroup = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+parentGroupId, true);
      edu.internet2.middleware.grouper.Group grouperChildGroup = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+childGroupId, true);
      Subject subject = SubjectFinder.findById(grouperChildGroup.getId(), false);
      if (subject == null) {
        throw new SubjectNotFoundException(childGroupId+" was not found.");
      }
      grouperParentGroup.deleteMember(subject, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public void addUserToGroup(String userId, String groupId) throws SubjectNotFoundException, GroupNotFoundException {
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      edu.internet2.middleware.grouper.Group group = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+groupId, true);
      Subject subject = SubjectFinder.findByIdAndSource(userId, SUBJECT_SOURCE, true);
      group.addMember(subject, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public void removeUserFromGroup(String userId, String groupId) throws SubjectNotFoundException, GroupNotFoundException {
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      edu.internet2.middleware.grouper.Group group = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+groupId, true);
      Subject subject = SubjectFinder.findByIdAndSource(userId, SUBJECT_SOURCE, true);
      group.deleteMember(subject, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public List<GroupMembership> getAllMembersForTheGroup(String groupId) throws GroupNotFoundException {
    List<GroupMembership> groupMemberships = new ArrayList<GroupMembership>();
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      edu.internet2.middleware.grouper.Group grouperGroup = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+groupId, true);
      for(Member member: grouperGroup.getImmediateMembers()) {
        GroupMembership groupMembership = new GroupMembership();
        groupMembership.setGroupId(groupId);
        groupMembership.setGroupMembershipType(DIRECT);
        groupMembership.setMemberId(member.getSubjectType().equals(PERSON) ? member.getSubjectId() : GrouperUtil.substringAfterLast(member.getName(), ":"));
        groupMembership.setMemberType(member.getSubjectType().equals(PERSON) ? SubjectType.PERSON: SubjectType.GROUP);
        groupMemberships.add(groupMembership);
      }
      for(Member member: grouperGroup.getNonImmediateMembers()) {
        GroupMembership groupMembership = new GroupMembership();
        groupMembership.setGroupId(groupId);
        groupMembership.setGroupMembershipType(INDIRECT);
        groupMembership.setMemberId(member.getSubjectType().equals(PERSON) ? member.getSubjectId() : GrouperUtil.substringAfterLast(member.getName(), ":"));
        groupMembership.setMemberType(member.getSubjectType().equals(PERSON) ? SubjectType.PERSON: SubjectType.GROUP);
        groupMemberships.add(groupMembership);
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    return groupMemberships;
  }
  
  public static void main(String[] args) {
    
    GroupServiceImpl groupServiceImpl = new GroupServiceImpl();
    
    // create a test group
    Group parentGroup = new Group();
    parentGroup.setId("airavata parent group id");
    parentGroup.setName("airavata parent group name");
    parentGroup.setDescription("airavata parent group description");
    groupServiceImpl.createOrUpdateGroup(parentGroup);
    
    // update the same group
    Group updateGroup = new Group();
    updateGroup.setId("airavata parent group id");
    updateGroup.setName("airavata parent group name updated");
    updateGroup.setDescription("airavata parent group description updated");
    groupServiceImpl.createOrUpdateGroup(updateGroup);
    
    // create another group
    Group childGroup = new Group();
    childGroup.setId("airavata child group id");
    childGroup.setName("airavata child group name");
    childGroup.setDescription("airavata child group description");
    groupServiceImpl.createOrUpdateGroup(childGroup);
    
    // add child group to parent group
    groupServiceImpl.addGroupToGroup("airavata parent group id", "airavata child group id");
      
    // add a direct person to the group
    groupServiceImpl.addUserToGroup("admin@seagrid", "airavata parent group id");
    
    // add a person to the child group which will be basically an indirect member of parent group
    groupServiceImpl.addUserToGroup("scnakandala@seagrid", "airavata child group id");
    
    // get the parent group
    groupServiceImpl.getGroup("airavata parent group id");
    
    //get all the members of the group
    groupServiceImpl.getAllMembersForTheGroup("airavata parent group id");
    
    // remove child from parent
    groupServiceImpl.removeGroupFromGroup("airavata parent group id", "airavata child group id");
    
    // delete the same group 
    groupServiceImpl.deleteGroup("airavata child group id");
    groupServiceImpl.deleteGroup("airavata parent group id");
    
  }
 
}
