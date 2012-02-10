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
 * Class implements the Chain of Responsibility Pattern
 */
public abstract class Chain<T> {
    private T next;

    /**
     * Set the next item in the chain
     * 
     * @param nextChain
     * @return the next item
     */
    public T setNext(T nextChain) {
        this.next = nextChain;
        return this.next;
    }

    /**
     * Get the next item in the Chain
     * 
     * @return next items
     */
    protected T getNext() {
        return next;
    }

    /**
     * Start the chain
     * 
     * @param context
     * @throws ExtensionException
     */
    public abstract void start(InvocationContext context) throws ExtensionException;
}
