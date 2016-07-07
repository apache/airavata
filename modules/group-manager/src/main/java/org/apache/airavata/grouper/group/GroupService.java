/**
 * 
 */
package org.apache.airavata.grouper.group;

import edu.internet2.middleware.grouper.exception.GroupNotFoundException;

/**
 * @author vsachdeva
 *
 */
public interface GroupService {
  
  public void createOrUpdateGroup(Group group);
  
  public void deleteGroup(String groupId) throws GroupNotFoundException;
  
  public Group getGroup(String groupId) throws GroupNotFoundException;
  
  public void addGroupToGroup(String parentGroupId, String childGroupId) throws GroupNotFoundException;
  
  public void removeGroupFromGroup(String parentGroupId, String childGroupId) throws GroupNotFoundException;

}
