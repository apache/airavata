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

package org.apache.airavata.services.gfac.axis2.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.axis2.context.MessageContext;

public class MessageContextUtil {

    /**
     * Add object to Map object in message context's property. Create a new list if necessary.
     * 
     * @param msgContext
     * @param propertyString
     * @param item
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static synchronized void addContextToProperty(MessageContext msgContext, String propertyString, String name,
            Object item) {
        Map<String, Object> m = null;
        if (msgContext.getProperty(propertyString) != null) {
            Object obj = msgContext.getProperty(propertyString);
            m = (Map) obj;
        } else {
            m = new HashMap<String, Object>();
        }
        m.put(name, item);
        msgContext.setProperty(propertyString, m);
    }
}
