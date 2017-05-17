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
package org.apache.airavata.xbaya.interpreter;

public class LevenshteinDistanceService {
    public int computeDistance(String sequence1, String sequence2) {
        int[][] distance = new int[sequence1.length() + 1][sequence2.length() + 1];

        for (int i = 0; i <= sequence1.length(); i++) {
            distance[i][0] = i;
        }
        for (int j = 0; j <= sequence2.length(); j++) {
            distance[0][j] = j;
        }

        for (int i = 1; i <= sequence1.length(); i++) {
            for (int j = 1; j <= sequence2.length(); j++) {
                distance[i][j] = min(distance[i - 1][j] + 1, distance[i][j - 1] + 1, distance[i - 1][j - 1] + ((sequence1.charAt(i - 1) == sequence2.charAt(j - 1)) ? 0 : 1));
            }
        }

         return distance[sequence1.length()][sequence2.length()];
    }

    private int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

}
