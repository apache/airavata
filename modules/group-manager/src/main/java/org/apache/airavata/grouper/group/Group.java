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
  
  private String description;
  
  private List<String> users = new ArrayList<String>();
  
  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  
  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
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
   * @return the users
   */
  public List<String> getUsers() {
    return users;
  }

  /**
   * @param users the users to set
   */
  public void setUsers(List<String> users) {
    this.users = users;
  }


  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "Group [id=" + id + ", name=" + name + ", description=" + description + "]";
  }
  
}
