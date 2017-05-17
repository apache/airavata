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
package org.apache.airavata.workflow.model.component.ws;

import javax.xml.namespace.QName;

public class WSComponentKey {

    private final String id;

    private final QName portType;

    private final String operation;

    /**
     * Constructs a WSComponentKey.
     * 
     * @param id
     * @param portType
     * @param operation
     */
    public WSComponentKey(String id, QName portType, String operation) {
        this.id = id;
        this.portType = portType;
        this.operation = operation;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof WSComponentKey)) {
            return false;
        }
        WSComponentKey key = (WSComponentKey) object;
        return this.id.equals(key.id) && this.portType.equals(key.portType) && this.operation.equals(key.operation);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public final int hashCode() {
        return this.id.hashCode() ^ this.portType.hashCode() ^ this.operation.hashCode();
    }
}