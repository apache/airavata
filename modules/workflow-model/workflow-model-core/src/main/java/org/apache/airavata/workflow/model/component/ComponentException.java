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
package org.apache.airavata.workflow.model.component;

import org.apache.airavata.workflow.model.exceptions.WorkflowException;

/**
 * The ComponentException class represents an Exception related to Components. Mostly, it is thrown when parsing a
 * Component XML.
 * 
 */
public class ComponentException extends WorkflowException {

    /**
     * Constructs a ComponentException.
     */
    public ComponentException() {
        super();
    }

    /**
     * @param message
     */
    public ComponentException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ComponentException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ComponentException(String message, Throwable cause) {
        super(message, cause);
    }

}