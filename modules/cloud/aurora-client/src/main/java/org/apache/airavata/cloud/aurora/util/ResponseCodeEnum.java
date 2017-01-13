/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
