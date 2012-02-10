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

package org.apache.airavata.core.gfac.extension;

import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.exception.ExtensionException;

/**
 * The implementation of Chain of Responsibility with twist. The item in the chain can stop the process by return
 * <code>true</code> from execution method.
 * 
 */
public abstract class ExitableChain extends Chain<ExitableChain> {
    public final void start(InvocationContext context) throws ExtensionException {
        boolean breakTheChain = this.execute(context);
        if (getNext() != null && !breakTheChain) {
            this.getNext().start(context);
        }
    }

    /**
     * Execution method to be called for each item
     * 
     * @param context
     * @return true if no need for further processing
     * @throws ExtensionException
     */
    protected abstract boolean execute(InvocationContext context) throws ExtensionException;
}
