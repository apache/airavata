/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Autogenerated by Thrift Compiler (0.21.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.apache.airavata.model.status;


@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.21.0)")
public enum JobState implements org.apache.thrift.TEnum {
  SUBMITTED(0),
  QUEUED(1),
  ACTIVE(2),
  COMPLETE(3),
  CANCELED(4),
  FAILED(5),
  SUSPENDED(6),
  UNKNOWN(7),
  NON_CRITICAL_FAIL(8);

  private final int value;

  private JobState(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  @Override
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  @org.apache.thrift.annotation.Nullable
  public static JobState findByValue(int value) { 
    switch (value) {
      case 0:
        return SUBMITTED;
      case 1:
        return QUEUED;
      case 2:
        return ACTIVE;
      case 3:
        return COMPLETE;
      case 4:
        return CANCELED;
      case 5:
        return FAILED;
      case 6:
        return SUSPENDED;
      case 7:
        return UNKNOWN;
      case 8:
        return NON_CRITICAL_FAIL;
      default:
        return null;
    }
  }
}
