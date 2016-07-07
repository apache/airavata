/**
 * 
 */
package org.apache.airavata.grouper.group;

import org.apache.airavata.grouper.SubjectType;

/**
 * @author vsachdeva
 *
 */
public class GroupMembership {
  
  private String groupId;
  
  private String memberId;
  
  private SubjectType memberType;
  
  private GroupMembershipType groupMembershipType;
  
  /**
   * @return the groupId
   */
  public String getGroupId() {
    return groupId;
  }
  
  /**
   * @param groupId the groupId to set
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }
  
  /**
   * @return the memberId
   */
  public String getMemberId() {
    return memberId;
  }
  
  /**
   * @param memberId the memberId to set
   */
  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }
  
  /**
   * @return the memberType
   */
  public SubjectType getMemberType() {
    return memberType;
  }
  
  /**
   * @param memberType the memberType to set
   */
  public void setMemberType(SubjectType memberType) {
    this.memberType = memberType;
  }
  
  /**
   * @return the groupMembershipType
   */
  public GroupMembershipType getGroupMembershipType() {
    return groupMembershipType;
  }
  
  /**
   * @param groupMembershipType the groupMembershipType to set
   */
  public void setGroupMembershipType(GroupMembershipType groupMembershipType) {
    this.groupMembershipType = groupMembershipType;
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "GroupMembership [groupId=" + groupId + ", memberId=" + memberId
        + ", memberType=" + memberType + ", groupMembershipType=" + groupMembershipType
        + "]";
  }
    
}
