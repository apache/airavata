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
package org.apache.airavata.model.messaging.event;


@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.21.0)")
public enum MessageType implements org.apache.thrift.TEnum {
  EXPERIMENT(0),
  EXPERIMENT_CANCEL(1),
  TASK(2),
  PROCESS(3),
  JOB(4),
  LAUNCHPROCESS(5),
  TERMINATEPROCESS(6),
  PROCESSOUTPUT(7),
  DB_EVENT(8),
  INTERMEDIATE_OUTPUTS(9);

  private final int value;

  private MessageType(int value) {
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
  public static MessageType findByValue(int value) { 
    switch (value) {
      case 0:
        return EXPERIMENT;
      case 1:
        return EXPERIMENT_CANCEL;
      case 2:
        return TASK;
      case 3:
        return PROCESS;
      case 4:
        return JOB;
      case 5:
        return LAUNCHPROCESS;
      case 6:
        return TERMINATEPROCESS;
      case 7:
        return PROCESSOUTPUT;
      case 8:
        return DB_EVENT;
      case 9:
        return INTERMEDIATE_OUTPUTS;
      default:
        return null;
    }
  }
}
