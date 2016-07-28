/**
 *
 */
package org.apache.airavata.grouper.group;

import static edu.internet2.middleware.subject.provider.SubjectTypeEnum.PERSON;
import static org.apache.airavata.grouper.AiravataGrouperUtil.COLON;
import static org.apache.airavata.grouper.AiravataGrouperUtil.GROUPS_STEM_NAME;
import static org.apache.airavata.grouper.AiravataGrouperUtil.SUBJECT_SOURCE;
import static org.apache.airavata.grouper.group.GroupMembershipType.DIRECT;
import static org.apache.airavata.grouper.group.GroupMembershipType.INDIRECT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.airavata.grouper.SubjectType;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * @author vsachdeva
 *
 */
public class GroupServiceImpl implements GroupService {


  public void createGroup(Group group) throws SubjectNotFoundException, GroupAddAlreadyExistsException {

    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      Subject subject = SubjectFinder.findByIdAndSource(group.getOwnerId(), SUBJECT_SOURCE, true);
      GroupSave groupSave = new GroupSave(grouperSession);
      groupSave.assignTypeOfGroup(TypeOfGroup.group);
      groupSave.assignGroupNameToEdit(GROUPS_STEM_NAME+COLON+group.getId());
      groupSave.assignName(GROUPS_STEM_NAME+COLON+group.getId());
      groupSave.assignDisplayExtension(group.getName());
      groupSave.assignDescription(group.getDescription());
      groupSave.assignSaveMode(SaveMode.INSERT);
      groupSave.assignCreateParentStemsIfNotExist(true);
      edu.internet2.middleware.grouper.Group grp = groupSave.save();
      grp.grantPriv(subject, AccessPrivilege.ADMIN, false);
      for (String userId: group.getMembers()) {
        Subject sub = SubjectFinder.findByIdAndSource(userId, SUBJECT_SOURCE, true);
        grp.addMember(sub, false);
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  public void updateGroup(Group group) throws GroupNotFoundException, SubjectNotFoundException, InsufficientPrivilegeException {

    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      Subject subject = SubjectFinder.findByIdAndSource(group.getOwnerId(), SUBJECT_SOURCE, true);

      edu.internet2.middleware.grouper.Group grouperGroup = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+group.getId(),
          true, new QueryOptions().secondLevelCache(false));

      Subject admin = null;
      // there will be one admin only.
      if (grouperGroup.getAdmins().size() > 0) {
        admin = grouperGroup.getAdmins().iterator().next();
      }
      if (admin == null) {
        throw new RuntimeException("There is no admin for the group "+group.getId()+". It should have never happened.");
      }
      if (!admin.getId().equals(subject.getId())) {
        throw new InsufficientPrivilegeException("Only the owner of the group can update.");
      }
      GroupSave groupSave = new GroupSave(grouperSession);
      groupSave.assignTypeOfGroup(TypeOfGroup.group);
      groupSave.assignGroupNameToEdit(GROUPS_STEM_NAME+COLON+group.getId());
      groupSave.assignName(GROUPS_STEM_NAME+COLON+group.getId());
      groupSave.assignDisplayExtension(group.getName());
      groupSave.assignDescription(group.getDescription());
      groupSave.assignSaveMode(SaveMode.UPDATE);
      groupSave.assignCreateParentStemsIfNotExist(true);
      edu.internet2.middleware.grouper.Group grp = groupSave.save();
      for (Member member: grp.getMembers()) {
        grp.deleteMember(member);
      }
      for (String userId: group.getMembers()) {
        Subject sub = SubjectFinder.findByIdAndSource(userId, SUBJECT_SOURCE, true);
        grp.addMember(sub, false);
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }

  public void deleteGroup(String groupId, String ownerId) throws GroupNotFoundException, SubjectNotFoundException, InsufficientPrivilegeException {

    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      edu.internet2.middleware.grouper.Group group = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+groupId,
          true, new QueryOptions().secondLevelCache(false));
      Subject subject = SubjectFinder.findByIdAndSource(ownerId, SUBJECT_SOURCE, true);
      Subject admin = null;
      // there will be one admin only.
      if (group.getAdmins().size() > 0) {
        admin = group.getAdmins().iterator().next();
      }
      if (admin == null) {
        throw new RuntimeException("There is no admin for the group "+groupId+". It should have never happened.");
      }
      if (!admin.getId().equals(subject.getId())) {
        throw new InsufficientPrivilegeException("Only the owner of the group can update.");
      }
      group.delete();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  public Group getGroup(String groupId) throws GroupNotFoundException {

    GrouperSession grouperSession = null;
    Group group = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      edu.internet2.middleware.grouper.Group grouperGroup = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+groupId, true);
      Subject admin = null;
      // there will be one admin only.
      if (grouperGroup.getAdmins().size() > 0) {
        admin = grouperGroup.getAdmins().iterator().next();
      }
      if (admin == null) {
        throw new RuntimeException("There is no admin for the group "+groupId+". It should have never happened.");
      }
      group = new Group(grouperGroup.getExtension(), admin.getId());
      group.setName(grouperGroup.getDisplayExtension());
      group.setDescription(grouperGroup.getDescription());
      List<String> users = new ArrayList<String>();
      for(Member member: grouperGroup.getMembers()) {
        if (member.getSubjectType().equals(PERSON)) {
          users.add(member.getSubjectId());
        }
      }
      group.setMembers(users);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    return group;
  }

  public void addGroupToGroup(String parentGroupId, String childGroupId, String ownerId) throws GroupNotFoundException, SubjectNotFoundException, InsufficientPrivilegeException {

    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      edu.internet2.middleware.grouper.Group grouperParentGroup = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+parentGroupId, true);
      edu.internet2.middleware.grouper.Group grouperChildGroup = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+childGroupId, true);
      Subject subject = SubjectFinder.findById(grouperChildGroup.getId(), false);
      if (subject == null) {
        throw new GroupNotFoundException(childGroupId+" was not found.");
      }
      Subject maybeAdmin = SubjectFinder.findByIdAndSource(ownerId, SUBJECT_SOURCE, true);
      Subject admin = null;
      // there will be one admin only.
      if (grouperParentGroup.getAdmins().size() > 0) {
        admin = grouperParentGroup.getAdmins().iterator().next();
      }
      if (admin == null) {
        throw new RuntimeException("There is no admin for the group "+parentGroupId+". It should have never happened.");
      }
      if (!admin.getId().equals(maybeAdmin.getId())) {
        throw new InsufficientPrivilegeException("Only the owner of the group "+parentGroupId+" can update.");
      }

      if (grouperChildGroup.getAdmins().size() > 0) {
        admin = grouperChildGroup.getAdmins().iterator().next();
      }
      if (admin == null) {
        throw new RuntimeException("There is no admin for the group "+childGroupId+". It should have never happened.");
      }
      if (!admin.getId().equals(maybeAdmin.getId())) {
        throw new InsufficientPrivilegeException("Only the owner of the group "+childGroupId+" can update.");
      }
      grouperParentGroup.addMember(subject, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  public void removeGroupFromGroup(String parentGroupId, String childGroupId, String ownerId) throws GroupNotFoundException, SubjectNotFoundException, InsufficientPrivilegeException {

    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      edu.internet2.middleware.grouper.Group grouperParentGroup = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+parentGroupId, true);
      edu.internet2.middleware.grouper.Group grouperChildGroup = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+childGroupId, true);
      Subject subject = SubjectFinder.findById(grouperChildGroup.getId(), false);
      if (subject == null) {
        throw new SubjectNotFoundException(childGroupId+" was not found.");
      }

      Subject maybeAdmin = SubjectFinder.findByIdAndSource(ownerId, SUBJECT_SOURCE, true);
      Subject admin = null;
      // there will be one admin only.
      if (grouperParentGroup.getAdmins().size() > 0) {
        admin = grouperParentGroup.getAdmins().iterator().next();
      }
      if (admin == null) {
        throw new RuntimeException("There is no admin for the group "+parentGroupId+". It should have never happened.");
      }
      if (!admin.getId().equals(maybeAdmin.getId())) {
        throw new InsufficientPrivilegeException("Only the owner of the group "+parentGroupId+" can update.");
      }

      if (grouperChildGroup.getAdmins().size() > 0) {
        admin = grouperChildGroup.getAdmins().iterator().next();
      }
      if (admin == null) {
        throw new RuntimeException("There is no admin for the group "+childGroupId+". It should have never happened.");
      }
      if (!admin.getId().equals(maybeAdmin.getId())) {
        throw new InsufficientPrivilegeException("Only the owner of the group "+childGroupId+" can update.");
      }
      grouperParentGroup.deleteMember(subject, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  public void addUserToGroup(String userId, String groupId, String ownerId) throws SubjectNotFoundException, GroupNotFoundException, InsufficientPrivilegeException {
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      edu.internet2.middleware.grouper.Group group = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+groupId, true);
      Subject subject = SubjectFinder.findByIdAndSource(userId, SUBJECT_SOURCE, true);

      Subject maybeAdmin = SubjectFinder.findByIdAndSource(ownerId, SUBJECT_SOURCE, true);
      Subject admin = null;
      // there will be one admin only.
      if (group.getAdmins().size() > 0) {
        admin = group.getAdmins().iterator().next();
      }
      if (admin == null) {
        throw new RuntimeException("There is no admin for the group "+groupId+". It should have never happened.");
      }
      if (!admin.getId().equals(maybeAdmin.getId())) {
        throw new InsufficientPrivilegeException("Only the owner of the group can update.");
      }
      group.addMember(subject, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  public void removeUserFromGroup(String userId, String groupId, String ownerId) throws SubjectNotFoundException, GroupNotFoundException, InsufficientPrivilegeException {
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      edu.internet2.middleware.grouper.Group group = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+groupId, true);
      Subject subject = SubjectFinder.findByIdAndSource(userId, SUBJECT_SOURCE, true);

      Subject maybeAdmin = SubjectFinder.findByIdAndSource(ownerId, SUBJECT_SOURCE, true);
      Subject admin = null;
      // there will be one admin only.
      if (group.getAdmins().size() > 0) {
        admin = group.getAdmins().iterator().next();
      }
      if (admin == null) {
        throw new RuntimeException("There is no admin for the group "+groupId+". It should have never happened.");
      }
      if (!admin.getId().equals(maybeAdmin.getId())) {
        throw new InsufficientPrivilegeException("Only the owner of the group can update.");
      }

      group.deleteMember(subject, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  public List<GroupMembership> getAllMembersForGroup(String groupId) throws GroupNotFoundException {
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

  public List<GroupMembership> getAllMembershipsForUser(String userId) throws SubjectNotFoundException {
    List<GroupMembership> groupMemberships = new ArrayList<GroupMembership>();
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      Subject subject = SubjectFinder.findByIdAndSource(userId, SUBJECT_SOURCE, true);
      Member member = MemberFinder.findBySubject(grouperSession, subject, false);
      if (member != null) {
        for (edu.internet2.middleware.grouper.Group group : member.getImmediateGroups()) {
          GroupMembership groupMembership = new GroupMembership();
          groupMembership.setGroupId(group.getExtension());
          groupMembership.setGroupMembershipType(DIRECT);
          groupMembership.setMemberId(userId);
          groupMembership.setMemberType(SubjectType.PERSON);
          groupMemberships.add(groupMembership);
        }
        for (edu.internet2.middleware.grouper.Group group : member.getNonImmediateGroups()) {
          GroupMembership groupMembership = new GroupMembership();
          groupMembership.setGroupId(group.getExtension());
          groupMembership.setGroupMembershipType(INDIRECT);
          groupMembership.setMemberId(userId);
          groupMembership.setMemberType(SubjectType.PERSON);
          groupMemberships.add(groupMembership);
        }
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    return groupMemberships;
  }

  public static void main(String[] args) {

    GroupService groupService = new GroupServiceImpl();

    // create a test group
    Group parentGroup = new Group("airavata parent group id", "airavata_id_1");
    parentGroup.setName("airavata parent group name");
    parentGroup.setDescription("airavata parent group description");
    parentGroup.setMembers(Arrays.asList("airavata_id_2", "airavata_id_3"));
    groupService.createGroup(parentGroup);

    // update the same group
    Group updateGroup = new Group("airavata parent group id", "airavata_id_1");
    updateGroup.setName("airavata parent group name updated");
    updateGroup.setDescription("airavata parent group description updated");
    updateGroup.setMembers(Arrays.asList("airavata_id_4", "airavata_id_5"));
    groupService.updateGroup(updateGroup);

    // create another group
    Group childGroup = new Group("airavata child group id", "airavata_id_1");
    childGroup.setName("airavata child group name");
    childGroup.setDescription("airavata child group description");
    childGroup.setMembers(Arrays.asList("airavata_id_6", "airavata_id_7"));
    groupService.createGroup(childGroup);

    // add child group to parent group
    groupService.addGroupToGroup("airavata parent group id", "airavata child group id", "airavata_id_1");

    // add two more direct persons to the group
    groupService.addUserToGroup("airavata_id_2", "airavata parent group id", "airavata_id_1");
    groupService.addUserToGroup("airavata_id_3", "airavata parent group id", "airavata_id_1");

    // add a person to the child group which will be basically an indirect member of parent group
    groupService.addUserToGroup("airavata_id_8", "airavata child group id", "airavata_id_1");

    // get the parent group
    Group group = groupService.getGroup("airavata parent group id");
    System.out.println(group);

    //get all the members of the group
    List<GroupMembership> allMembersForGroup = groupService.getAllMembersForGroup("airavata parent group id");
    System.out.println(allMembersForGroup);

    //get all the groups for user airavata_id_2
    List<GroupMembership> membershipsForUser = groupService.getAllMembershipsForUser("airavata_id_2");
    System.out.println(membershipsForUser);

    // remove child from parent
    groupService.removeGroupFromGroup("airavata parent group id", "airavata child group id", "airavata_id_1");

    // delete the same group
    groupService.deleteGroup("airavata child group id", "airavata_id_1");
    groupService.deleteGroup("airavata parent group id", "airavata_id_1");

  }

}
