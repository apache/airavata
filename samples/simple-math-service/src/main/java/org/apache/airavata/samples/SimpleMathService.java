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

package org.apache.airavata.samples;

public class SimpleMathService {
    /**
     * @param x
     * @param y
     * @return x plus y
     */
    public int add(int x, int y) {
        return x + y;
    }

    /**
     * @param x
     * @param y
     * @return x minus y
     */
    public int subtract(int x, int y) {
        return x + y;
    }

    /**
     * @param x
     * @param y
     * @return x multiply y
     */
    public int multiply(int x, int y) {
        return x * y;
    }

    /**
     * @param x
     * @param size
     * @return a string array with size contains x
     */
    public String[] stringArrayGenerate(String x, int size) {
        String[] result = new String[size];
        for (int i = 0; i < result.length; i++) {
            result[i] = x;
        }
        return result;
    }

    /**
     * @param x
     * @param size
     * @return an integer array with size contains x
     */
    public int[] intArrayGenerate(int x, int size) {
        int[] result = new int[size];
        for (int i = 0; i < result.length; i++) {
            result[i] = x;
        }
        return result;
    }

    public String greet(String echo) {
        return "Hello World " + echo + " !";
    }
}
