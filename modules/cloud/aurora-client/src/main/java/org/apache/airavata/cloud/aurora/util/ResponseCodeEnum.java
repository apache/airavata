package org.apache.airavata.cloud.aurora.util;

/**
 * The Enum ResponseCodeEnum.
 */
public enum ResponseCodeEnum {
	
	/** The invalid request. */
	INVALID_REQUEST(0),
	
	/** The ok. */
	OK(1),
	
	/** The error. */
	ERROR(2),
	
	/** The warning. */
	WARNING(3),
	
	/** The auth failed. */
	AUTH_FAILED(4),
	
	/** The lock error. */
	LOCK_ERROR(5),
	
	/** The error transient. */
	ERROR_TRANSIENT(6);
	
	/** The value. */
	private final int value;
	
	/**
	 * Instantiates a new response code enum.
	 *
	 * @param value the value
	 */
	private ResponseCodeEnum(int value) {
		this.value = value;
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() {
		return value;
	}
	
	/**
	 * Find by value.
	 *
	 * @param value the value
	 * @return the response code enum
	 */
	public static ResponseCodeEnum findByValue(int value) { 
	    switch (value) {
	      case 0:
	        return INVALID_REQUEST;
	      case 1:
	        return OK;
	      case 2:
	        return ERROR;
	      case 3:
	        return WARNING;
	      case 4:
	        return AUTH_FAILED;
	      case 5:
	        return LOCK_ERROR;
	      case 6:
	        return ERROR_TRANSIENT;
	      default:
	        return null;
	    }
	}
}
