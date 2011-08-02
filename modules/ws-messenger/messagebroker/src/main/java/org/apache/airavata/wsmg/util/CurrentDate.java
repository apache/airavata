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

package org.apache.airavata.wsmg.util;

import java.util.TimeZone;

import org.apache.axiom.om.OMElement;

public class CurrentDate implements Cloneable {
    private OMElement parent;

    public CurrentDate(OMElement parent) {
        this.parent = parent;

    }

    public Object clone() throws CloneNotSupportedException {

        OMElement element = parent.cloneOMElement();

        return new CurrentDate(element);

    }

    public String getText() {
        // active entity: value computed on demand
        // TODO use static method that would format System.currentTimeMillis +
        // TZ
        DcDate now = new DcDate(TimeZone.getDefault());
        return now.toString();
    }

    public Boolean isWhitespaceContent() {
        return null;
    }

    public OMElement getParent() {
        return parent;
    }

    public void setParent(OMElement currentDateParent) {
        this.parent = currentDateParent;
    }
}
