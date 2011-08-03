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

package org.apache.airavata.workflow.tracking.util;

import org.apache.airavata.workflow.tracking.types.BaseNotificationType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

public class XmlBeanUtils {
    // public static void addNameValuePair(XmlObject parent, QName name, Object value)
    // throws WorkflowTrackingException {
    // XmlCursor c = parent.newCursor();
    // // TODO may be we need to check we are not moving in to a namespace or a
    // // attribute.
    // c.toNextToken();
    // XmlBeanUtils.addNameValuePair(c, name, value);
    // c.dispose();
    // }
    //
    //
    // public static void createAFragment(QName name, Properties nameValuePairs)
    // throws WorkflowTrackingException {
    // XmlCursor c = XmlObject.Factory.newInstance();
    // c.beginElement(name);
    // for(String key: nameValuePairs.keySet()){
    // XmlBeanUtils.addNameValuePair(c, new QName(key), nameValuePairs.get(key));
    // }
    // c.dispose();
    // }
    public static BaseNotificationType extractBaseNotificationType(XmlObject xmldata) {
        XmlCursor c = xmldata.newCursor();
        c.toNextToken();

        // System.out.println(c.getObject().getClass());
        BaseNotificationType type = (BaseNotificationType) c.getObject();
        c.dispose();
        return type;
    }

}
