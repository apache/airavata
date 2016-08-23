/**
 * 
 */
package org.apache.airavata.grouper.resource;


/**
 * @author vsachdeva
 *
 */
public class ResourceNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;


  public ResourceNotFoundException() { 
    super(); 
  }
  

  public ResourceNotFoundException(String msg) { 
    super(msg); 
  }
  

  public ResourceNotFoundException(String msg, Throwable cause) { 
    super(msg, cause); 
  }
  

  public ResourceNotFoundException(Throwable cause) { 
    super(cause); 
  }

}
