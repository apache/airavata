/**
 * Autogenerated by Thrift Compiler (0.10.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.apache.airavata.sharing.registry.models;


import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;

/**
 * <p>This is an system internal enum used to define single user groups and multi users groups. Every user is also
 * considered as a group in it's own right for implementation ease</p>
 * 
 */
public enum GroupCardinality implements org.apache.thrift.TEnum {
  SINGLE_USER(0),
  MULTI_USER(1);

  private final int value;

  private GroupCardinality(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static GroupCardinality findByValue(int value) { 
    switch (value) {
      case 0:
        return SINGLE_USER;
      case 1:
        return MULTI_USER;
      default:
        return null;
    }
  }
}