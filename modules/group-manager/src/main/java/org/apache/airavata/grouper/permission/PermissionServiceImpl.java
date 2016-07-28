/**
 * 
 */
package org.apache.airavata.grouper.permission;

import static org.apache.airavata.grouper.AiravataGrouperUtil.COLON;
import static org.apache.airavata.grouper.AiravataGrouperUtil.GROUPS_STEM_NAME;
import static org.apache.airavata.grouper.AiravataGrouperUtil.SUBJECT_SOURCE;
import static org.apache.airavata.grouper.SubjectType.PERSON;

import org.apache.airavata.grouper.SubjectType;
import org.apache.airavata.grouper.resource.ResourceType;
import org.apache.airavata.grouper.role.RoleServiceImpl;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.subject.Subject;

/**
 * @author vsachdeva
 *
 */
public class PermissionServiceImpl {
  
  
  public void grantPermission(String userIdOrGroupId, SubjectType subjectType, String resourceId, ResourceType resourceType, PermissionAction action) {
    
    if (userIdOrGroupId == null || subjectType == null || resourceId == null || resourceType == null || action == null) {
      throw new IllegalArgumentException("Invalid input");
    }
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      Subject subject = null;
      if (PERSON == subjectType) {
         subject = SubjectFinder.findByIdAndSource(userIdOrGroupId, SUBJECT_SOURCE, false);
      } else {
        edu.internet2.middleware.grouper.Group grouperGroup = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+userIdOrGroupId, false);
        if (grouperGroup == null) {
          throw new IllegalArgumentException("group with id/name "+userIdOrGroupId+" could not be found.");
        }
        subject = SubjectFinder.findById(grouperGroup.getId(), false);
      }
      
      if (subject == null) {
        throw new IllegalArgumentException("userIdOrGroupId "+userIdOrGroupId+" could not be found.");
      }
      RoleServiceImpl roleService = new RoleServiceImpl();
      roleService.assignRoleToUser(subject.getId(), resourceId+"_"+action, grouperSession);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  public void revokePermission(String userIdOrGroupId, SubjectType subjectType, String resourceId, ResourceType resourceType, PermissionAction action) {
    if (userIdOrGroupId == null || subjectType == null || resourceId == null || resourceType == null || action == null) {
      throw new IllegalArgumentException("Invalid input");
    }
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      Subject subject = null;
      if (PERSON == subjectType) {
        subject = SubjectFinder.findByIdAndSource(userIdOrGroupId, SUBJECT_SOURCE, false);
      } else {
        edu.internet2.middleware.grouper.Group grouperGroup = GroupFinder.findByName(grouperSession, GROUPS_STEM_NAME+COLON+userIdOrGroupId, false);
        if (grouperGroup == null) {
          throw new IllegalArgumentException("group with id/name "+userIdOrGroupId+" could not be found.");
        }
        subject = SubjectFinder.findById(grouperGroup.getId(), false);
      }
      
      if (subject == null) {
        throw new IllegalArgumentException("userIdOrGroupId "+userIdOrGroupId+" could not be found.");
      }
      RoleServiceImpl roleService = new RoleServiceImpl();
      roleService.removeRoleFromUser(subject.getId(), resourceId+"_"+action, grouperSession);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
}
