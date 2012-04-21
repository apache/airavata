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

package org.apache.airavata.core.gfac.context.message;

import java.util.Iterator;

/**
 *
 * MessageContext represents a message that could be used by all provider or in specific provider. Mostly, this context
 * will be stored in the format of <key, value> pair. For example, MessageContext<AbstractParameter> represents a
 * message for input or output as a parameter to the service.
 *
 * @param <T>
 *            class that associate with this message
 */
public interface MessageContext<T> {

    /**
     * Get list of names in the context
     *
     * @return
     */
    public Iterator<String> getNames();

    /**
     * Return value associated with the key
     *
     * @param name
     * @return value
     */
    T getValue(String name);

    /**
     * Return value associated with the key as a String object
     *
     * @param name
     * @return string represents value
     */
    String getStringValue(String name);

    /**
     * Add new object associated with the key
     *
     * @param name
     * @param value
     */
    void add(String name, T value);

    /**
     *
     * @param name
     * @param value
     */
    void remove(String name);
    /**
     * Update the current value associated with the key
     *
     * @param name
     * @param value
     */
    void setValue(String name, T value);
}
