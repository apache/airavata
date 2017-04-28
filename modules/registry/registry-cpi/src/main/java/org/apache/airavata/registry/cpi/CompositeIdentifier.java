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
package org.apache.airavata.registry.cpi;

/**
 * This class is to uniquely identify third layer child objects. For example, workflow node status object can be
 * uniquely identified with experiment id and node id.
 */
public class CompositeIdentifier {
    private Object topLevelIdentifier;
    private Object secondLevelIdentifier;
    private Object thirdLevelIdentifier;

    public CompositeIdentifier(Object topLevelIdentifier, Object secondLevelIdentifier) {
        this.topLevelIdentifier = topLevelIdentifier;
        this.secondLevelIdentifier = secondLevelIdentifier;
    }

    public CompositeIdentifier(Object topLevelIdentifier, Object secondLevelIdentifier, Object thirdLevelIdentifier) {
        this(topLevelIdentifier, secondLevelIdentifier);
        this.thirdLevelIdentifier = thirdLevelIdentifier;
    }

    public Object getTopLevelIdentifier() {
        return topLevelIdentifier;
    }

    public Object getSecondLevelIdentifier() {
        return secondLevelIdentifier;
    }

    public Object getThirdLevelIdentifier() { return thirdLevelIdentifier; }

    @Override
    public String toString() {
        if (thirdLevelIdentifier != null && thirdLevelIdentifier instanceof String && topLevelIdentifier instanceof String && secondLevelIdentifier instanceof String) {
            return topLevelIdentifier + "," + secondLevelIdentifier + "," + thirdLevelIdentifier;
        } else if (topLevelIdentifier instanceof String && secondLevelIdentifier instanceof String) {
            return topLevelIdentifier + "," + secondLevelIdentifier;
        }else if (topLevelIdentifier instanceof String ) {
            return topLevelIdentifier.toString();
        } else {
            return secondLevelIdentifier.toString();
        }

    }
}
