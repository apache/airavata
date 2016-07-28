/**
 * 
 */
package org.apache.airavata.grouper.resource;

import static org.apache.airavata.grouper.AiravataGrouperUtil.DATA_STEM_NAME;
import static org.apache.airavata.grouper.AiravataGrouperUtil.EXPERIMENT_STEM_NAME;
import static org.apache.airavata.grouper.AiravataGrouperUtil.OTHER_STEM_NAME;
import static org.apache.airavata.grouper.AiravataGrouperUtil.PROJECT_STEM_NAME;

/**
 * @author vsachdeva
 *
 */
public enum ResourceType {
  
  PROJECT,
  EXPERIMENT,
  DATA,
  OTHER;
  
  public ResourceType getParentResoruceType() {
    
    switch (this) {
      case EXPERIMENT:
        return PROJECT;
      case DATA:
        return EXPERIMENT;
      default:
        return null;
    }
  }
  
  public String getStemFromResourceType() {
    
    switch (this) {
      case PROJECT:
        return PROJECT_STEM_NAME;
      case EXPERIMENT:
        return EXPERIMENT_STEM_NAME;
      case DATA:
        return DATA_STEM_NAME;
      case OTHER:
        return OTHER_STEM_NAME;
      default:
        return null;
    }
    
  }

}
