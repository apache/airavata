/**
 *
 */
package org.apache.airavata.grouper.group;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vsachdeva
 *
 */
public class Group {

  private String id;

  private String name;

  private String ownerId;

  private String description;

  private List<String> members = new ArrayList<String>();

  public Group(String id, String ownerId) {
    if (id == null || ownerId == null) {
      throw new IllegalArgumentException("id or ownerId is null");
    }
    this.id = id;
    this.ownerId = ownerId;
  }

  public Group() {

  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }


  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }


  /**
   * @return the ownerId
   */
  public String getOwnerId() {
    return ownerId;
  }


  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }


  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the members
   */
  public List<String> getMembers() {
    return members;
  }

  /**
   * @param members the members to set
   */
  public void setMembers(List<String> members) {
    this.members = members;
  }


  @Override
  public String toString() {
    return "Group [id=" + id + ", name=" + name + ", ownerId=" + ownerId
        + ", description=" + description + "]";
  }

}
