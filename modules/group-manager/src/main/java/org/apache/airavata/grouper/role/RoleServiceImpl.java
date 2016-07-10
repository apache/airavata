/**
 *
 */
package org.apache.airavata.grouper.role;

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

import static org.apache.airavata.grouper.AiravataGrouperUtil.*;

/**
 * @author vsachdeva
 *
 */
public class RoleServiceImpl {


  public Group createRole(String roleId, GrouperSession session) {

    GrouperSession grouperSession = null;
    Group role = null;
    try {
      grouperSession = session != null? session : GrouperSession.startRootSession();
      GroupSave groupSave = new GroupSave(grouperSession);
      groupSave.assignTypeOfGroup(TypeOfGroup.role);
      groupSave.assignGroupNameToEdit(ROLES_STEM_NAME+COLON+roleId);
      groupSave.assignName(ROLES_STEM_NAME+COLON+roleId);
      groupSave.assignDisplayExtension(roleId);
      groupSave.assignDescription(roleId);
      groupSave.assignSaveMode(SaveMode.INSERT_OR_UPDATE);
      groupSave.assignCreateParentStemsIfNotExist(true);
      role = groupSave.save();
    } finally {
      if (session == null) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
    return role;
  }

  public void deleteRole(String roleId, GrouperSession session) {
    GrouperSession grouperSession = null;
    try {
      grouperSession = session != null? session : GrouperSession.startRootSession();
      edu.internet2.middleware.grouper.Group role = GroupFinder.findByName(grouperSession, ROLES_STEM_NAME+COLON+roleId, false);
      if (role != null) {
        role.delete();
      }
    } finally {
      if (session == null) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  public void assignRoleToUser(String userId, String roleId, GrouperSession session) throws GroupNotFoundException, SubjectNotFoundException {

    GrouperSession grouperSession = null;
    try {
      grouperSession = session != null? session : GrouperSession.startRootSession();
      edu.internet2.middleware.grouper.Group role = GroupFinder.findByName(grouperSession, ROLES_STEM_NAME+COLON+roleId, false);
      if (role == null) {
        throw new GroupNotFoundException("Role "+roleId+" was not found.");
      }
      Subject subject = SubjectFinder.findById(userId, false);
      if (subject == null) {
        throw new SubjectNotFoundException("userId "+userId+" was not found.");
      }
      role.addMember(subject, false);
    } finally {
      if (session == null) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }

  }

  public void removeRoleFromUser(String userId, String roleId, GrouperSession session) throws GroupNotFoundException, SubjectNotFoundException {
    GrouperSession grouperSession = null;
    try {
      grouperSession = session != null? session : GrouperSession.startRootSession();
      edu.internet2.middleware.grouper.Group role = GroupFinder.findByName(grouperSession, ROLES_STEM_NAME+COLON+roleId, false);
      if (role == null) {
        throw new GroupNotFoundException("Role "+roleId+" was not found.");
      }
      Subject subject = SubjectFinder.findByIdAndSource(userId, SUBJECT_SOURCE, false);
      if (subject == null) {
        throw new SubjectNotFoundException("userId "+userId+" was not found.");
      }
      role.deleteMember(subject, false);
    } finally {
      if (session == null) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  public static void main(String[] args) {
    RoleServiceImpl roleServiceImpl = new RoleServiceImpl();

    roleServiceImpl.createRole("test_role", null);

    roleServiceImpl.assignRoleToUser("test.subject.3", "test_role", null);

    //roleServiceImpl.deleteRole("test_role", null);
  }

}
