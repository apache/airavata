/**
 * 
 */
package org.apache.airavata.grouper.resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author vsachdeva
 *
 */
public class Resource {
  
  private String id;
  
  private String name;
  
  private String description;
  
  private String parentResourceId;
  
  private ResourceType type;
  
  private String ownerId;
  
  public Resource(String resourceId, ResourceType resourceType) {
    if (resourceId == null || resourceType == null) {
      throw new IllegalArgumentException("Either resourceId or resourceType is null");
    }
    this.id = resourceId;
    this.type = resourceType;
  }
  
  
  /**
   * @return the resourceId
   */
  public String getId() {
    return id;
  }

  
  /**
   * @return the resourceName
   */
  public String getName() {
    return name;
  }

  
  /**
   * @param resourceName the resourceName to set
   */
  public void setName(String resourceName) {
    this.name = resourceName;
  }

  /**
   * @return the resourceDescription
   */
  public String getDescription() {
    return description;
  }

  
  /**
   * @param resourceDescription the resourceDescription to set
   */
  public void setDescription(String resourceDescription) {
    this.description = resourceDescription;
  }


  /**
   * @return the parentResourceId
   */
  public String getParentResourceId() {
    return parentResourceId;
  }


  /**
   * @param parentResourceId the parentResourceId to set
   */
  public void setParentResourceId(String parentResourceId) {
    this.parentResourceId = parentResourceId;
  }
  
  
  /**
   * @return the resourceType
   */
  public ResourceType getResourceType() {
    return type;
  }
  
  
  /**
   * @return the ownerId
   */
  public String getOwnerId() {
    return ownerId;
  }

  
  /**
   * @param ownerId the ownerId to set
   */
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }
  
  
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Resource)) {
      return false;
    }
    return StringUtils.equals(this.getId(), ( (Resource) other ).getId());
  }

 
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getId() )
      .toHashCode();
  }


  @Override
  public String toString() {
    return "Resource [resourceId=" + id + ", resourceName=" + name
        + ", resourceDescription=" + description + ", parentResourceId="
        + parentResourceId + ", resourceType=" + type + "]";
  }
  
  

  
}
