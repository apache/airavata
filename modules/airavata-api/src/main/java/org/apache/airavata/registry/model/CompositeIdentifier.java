/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.registry.model;

/**
 * Record to uniquely identify third layer child objects.
 * For example, workflow node status object can be uniquely identified with experiment id and node id.
 */
public record CompositeIdentifier(
        Object topLevelIdentifier,
        Object secondLevelIdentifier,
        Object thirdLevelIdentifier
) {
    /**
     * Compact constructor for two-level identifiers.
     */
    public CompositeIdentifier(Object topLevelIdentifier, Object secondLevelIdentifier) {
        this(topLevelIdentifier, secondLevelIdentifier, null);
    }

    // Provide getter aliases for backward compatibility
    public Object getTopLevelIdentifier() {
        return topLevelIdentifier;
    }

    public Object getSecondLevelIdentifier() {
        return secondLevelIdentifier;
    }

    public Object getThirdLevelIdentifier() {
        return thirdLevelIdentifier;
    }

    @Override
    public String toString() {
        return switch (this) {
            case CompositeIdentifier(String top, String second, String third) when third != null ->
                    top + "," + second + "," + third;
            case CompositeIdentifier(String top, String second, _) ->
                    top + "," + second;
            case CompositeIdentifier(String top, _, _) ->
                    top;
            default ->
                    secondLevelIdentifier != null ? secondLevelIdentifier.toString() : "";
        };
    }
}
