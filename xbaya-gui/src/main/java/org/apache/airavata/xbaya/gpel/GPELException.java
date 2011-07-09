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

package org.apache.airavata.xbaya.gpel;

import org.apache.airavata.xbaya.XBayaException;

public class GPELException extends XBayaException {

    /**
     * Constructs a GPELException.
     * 
     */
    public GPELException() {
        super();
    }

    /**
     * Constructs a GPELException.
     * 
     * @param message
     */
    public GPELException(String message) {
        super(message);
    }

    /**
     * Constructs a GPELException.
     * 
     * @param cause
     */
    public GPELException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a GPELException.
     * 
     * @param message
     * @param cause
     */
    public GPELException(String message, Throwable cause) {
        super(message, cause);
    }

}