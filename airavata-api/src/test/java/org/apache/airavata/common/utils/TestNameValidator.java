package org.apache.airavata.common.utils;

public class TestNameValidator extends NameValidator {

    /**
     * @param args
     * @Description some quick tests
     */
    public static void main(String[] args) {
        System.out.println(validate("abc90_90abc")); // true
        System.out.println(validate("abc_abc_123")); // true
        System.out.println(validate("abc_abc_")); // true
        System.out.println(validate("abc_abc")); // true
        System.out.println(validate("abc.abc")); // true
        System.out.println(validate("9abc_abc")); // false, name cannot start with number
        System.out.println(validate("_abc_abc")); // false, name cannot start with "_"
        System.out.println(validate("\\abc_abc")); // false, name cannot start with "\"
        System.out.println(validate("abc\\_abc")); // false, name cannot contain "\"
    }
}
