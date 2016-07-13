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
package org.apache.airavata.gfac.core;


/**
 * These are the different modes of execution chains in gfac
 * if the mode is SYNCHRONOUS then gfac will waits until the provider invoke mthod returns and then
 * invoke the out handlers explicitly, otherwise gfac will not invoke out hanlders, implementation
 * has to handler when to invoke out handlers, and default execution mode is synchronous.
 */
public enum ExecutionMode {
    SYNCHRONOUS,ASYNCHRONOUS;

    public static ExecutionMode fromString(String mode){
        if("async".equals(mode) ||  "asynchronous".equals(mode)){
            return ExecutionMode.ASYNCHRONOUS;
        }
        return ExecutionMode.SYNCHRONOUS;
    }
}
