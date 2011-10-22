/*
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
 *
 */

package org.apache.airavata.wsmg.commons;

public class WsmgVersion {

    public final static String SPEC_VERSION = "1.0.0";

    private final static String BUILD = "";

    private final static String PROJECT_NAME = "WSMG";

    private final static String IMPL_VERSION = SPEC_VERSION + BUILD;

    private final static String USER_AGENT = PROJECT_NAME + "/" + IMPL_VERSION;

    private static int VERSION_MAJOR = -1;

    private static int VERSION_MINOR = -1;

    private static int VERSION_INCREMENT = -1;

    public static String getUserAgent() {
        return USER_AGENT;
    }

    public static String getVersion() {
        return SPEC_VERSION;
    }

    public static String getSpecVersion() {
        return SPEC_VERSION;
    }

    public static String getImplementationVersion() {
        return IMPL_VERSION;
    }

    /**
     * Print version when exxecuted from command line.
     */
    public static void main(String[] args) {
        String SPEC_OPT = "-spec";
        String IMPL_OPT = "-impl";
        if (SPEC_OPT.equals(args[0])) {
            System.out.println(SPEC_VERSION);
        } else if (IMPL_OPT.equals(args[0])) {
            System.out.println(IMPL_VERSION);
        } else {
            System.err
                    .println(WsmgVersion.class.getName() + " Error: " + SPEC_OPT + " or " + IMPL_OPT + " is required");
            System.exit(1);
        }

    }

    public static void requireVersionOrExit(String version) {
        try {
            requireVersion(version);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.err.println("Error: could not find required version " + version + " of " + PROJECT_NAME + ": "
                    + ex.getMessage());
            System.err.println("Please make sure that JAR file with " + PROJECT_NAME + " with version " + version
                    + " (or higher) is available.");
            System.err.println("Please make sure there is no more than one JAR file with " + PROJECT_NAME);
            System.err.println("Exiting");
            System.exit(1);
        }
    }

    /**
     * Version mut be of form M.N[.K] where M is major version, N is minor version and K is icrement. This method
     * returns true if current major version is the same and minor is bigger or equal to current minor verion. If
     * provided major and minor verisons are equals to current version then increment is also checked and check is
     * passed when increment is bigger or equal to current increment version.
     */
    public static void requireVersion(String version) throws IllegalStateException {
        // check dependencies

        // NOTE: this is safe as int operations are atomic ...
        if (VERSION_MAJOR < 0)
            extractCurrentVersion();
        int[] parsed;
        try {
            parsed = parseVersion(version);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("could not parse " + PROJECT_NAME + " version string " + version);
        }
        int major = parsed[0];
        int minor = parsed[1];
        int increment = parsed[2];

        if (major != VERSION_MAJOR) {
            throw new IllegalStateException("required " + PROJECT_NAME + " " + version + " has different major version"
                    + " from current " + SPEC_VERSION);
        }
        if (minor > VERSION_MINOR) {
            throw new IllegalStateException("required " + PROJECT_NAME + " " + version + " has too big minor version"
                    + " when compared to current " + SPEC_VERSION);
        }
        if (minor == VERSION_MINOR) {
            if (increment > VERSION_INCREMENT) {
                throw new IllegalStateException("required " + PROJECT_NAME + " " + version
                        + " has too big increment version" + " when compared to current " + SPEC_VERSION);
            }
        }
    }

    /**
     * Parse verion string N.M[.K] into thre subcomponents (M=major,N=minor,K=increment) that are returned in array with
     * three elements. M and N must be non negative, and K if present must be positive integer. Increment K is optional
     * and if not present in verion strig it is returned as zero.
     */
    public static int[] parseVersion(String version) throws NumberFormatException {
        int[] parsed = new int[3];
        int firstDot = version.indexOf('.');
        if (firstDot == -1) {
            throw new NumberFormatException("expected version string N.M but there is no dot in " + version);
        }
        String majorVersion = version.substring(0, firstDot);
        parsed[0] = Integer.parseInt(majorVersion);
        if (parsed[0] < 0) {
            throw new NumberFormatException("major N version number in N.M can not be negative in " + version);
        }
        int secondDot = version.indexOf('.', firstDot + 1);
        String minorVersion;
        if (secondDot >= 0) {
            minorVersion = version.substring(firstDot + 1, secondDot);
        } else {
            minorVersion = version.substring(firstDot + 1);
        }
        parsed[1] = Integer.parseInt(minorVersion);
        if (parsed[1] < 0) {
            throw new NumberFormatException("minor M version number in N.M can not be negative in " + version);
        }
        if (secondDot >= 0) {
            String incrementVersion = version.substring(secondDot + 1);
            parsed[2] = Integer.parseInt(incrementVersion);
            if (parsed[2] < 0) {
                throw new NumberFormatException("increment K version number in N.M.K must be positive number in "
                        + version);
            }
        }
        return parsed;
    }

    private static synchronized void extractCurrentVersion() throws IllegalStateException {
        int[] parsed;
        try {
            parsed = parseVersion(SPEC_VERSION);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("internal problem: could not parse current " + PROJECT_NAME
                    + " version string " + SPEC_VERSION);
        }
        VERSION_MAJOR = parsed[0];
        VERSION_MINOR = parsed[1];
        VERSION_INCREMENT = parsed[2];
    }

}
