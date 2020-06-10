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
 package org.apache.airavata.helix.impl;

import java.util.ArrayList;
import java.util.List;

public final class SpecUtils {

    /**
     * Range can be defined in the format of 0-20 or 0-20,25-50 or 0,1,2,3 or 0,1,2,4-8
     */
    public static List<Integer> decodeRange(String rangeStr) {
        List<Integer> rangeVals = new ArrayList<>();
        String[] rangeParts = rangeStr.split(",");
        for (String rangePart : rangeParts) {
            String[] subParts = rangePart.split("-");
            if (subParts.length == 1) {
                rangeVals.add(Integer.parseInt(subParts[0]));
            }
            if (subParts.length == 2) {
                int minMargin = Integer.parseInt(subParts[0]);
                int maxMargin = Integer.parseInt(subParts[1]);
                for (int i = minMargin; i <= maxMargin; i++) {
                    rangeVals.add(i);
                }
            }
        }
        return rangeVals;
    }
}
