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
 * <p>This is an internal enum type for managing sharings</p>
 * 
 */
public enum SharingType implements org.apache.thrift.TEnum {
  DIRECT_NON_CASCADING(0),
  DIRECT_CASCADING(1),
  INDIRECT_CASCADING(2);

  private final int value;

  private SharingType(int value) {
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
  public static SharingType findByValue(int value) { 
    switch (value) {
      case 0:
        return DIRECT_NON_CASCADING;
      case 1:
        return DIRECT_CASCADING;
      case 2:
        return INDIRECT_CASCADING;
      default:
        return null;
    }
  }
}