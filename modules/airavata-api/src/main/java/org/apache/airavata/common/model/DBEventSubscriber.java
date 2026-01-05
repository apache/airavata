/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.common.model;

import java.util.Objects;

/**
 * Domain model: DBEventSubscriber
 */
public class DBEventSubscriber {
    private String subscriberService;

    public DBEventSubscriber() {}

    public String getSubscriberService() {
        return subscriberService;
    }

    public void setSubscriberService(String subscriberService) {
        this.subscriberService = subscriberService;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBEventSubscriber that = (DBEventSubscriber) o;
        return Objects.equals(subscriberService, that.subscriberService);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriberService);
    }

    @Override
    public String toString() {
        return "DBEventSubscriber{" + "subscriberService='" + subscriberService + '\'' + '}';
    }
}
