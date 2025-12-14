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
 * Domain model: DBEventMessageContext
 * Union type that can be either a publisher or subscriber.
 */
public class DBEventMessageContext {
    private DBEventPublisher publisher;
    private DBEventSubscriber subscriber;

    public DBEventMessageContext() {}

    public DBEventPublisher getPublisher() {
        return publisher;
    }

    public void setPublisher(DBEventPublisher publisher) {
        this.publisher = publisher;
        this.subscriber = null; // Clear subscriber when setting publisher
    }

    public DBEventSubscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(DBEventSubscriber subscriber) {
        this.subscriber = subscriber;
        this.publisher = null; // Clear publisher when setting subscriber
    }

    /**
     * Static factory method to create a publisher context.
     */
    public static DBEventMessageContext publisher(DBEventPublisher publisher) {
        DBEventMessageContext context = new DBEventMessageContext();
        context.setPublisher(publisher);
        return context;
    }

    /**
     * Static factory method to create a subscriber context.
     */
    public static DBEventMessageContext subscriber(DBEventSubscriber subscriber) {
        DBEventMessageContext context = new DBEventMessageContext();
        context.setSubscriber(subscriber);
        return context;
    }

    public boolean isPublisher() {
        return publisher != null;
    }

    public boolean isSubscriber() {
        return subscriber != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBEventMessageContext that = (DBEventMessageContext) o;
        return Objects.equals(publisher, that.publisher) && Objects.equals(subscriber, that.subscriber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publisher, subscriber);
    }

    @Override
    public String toString() {
        if (publisher != null) {
            return "DBEventMessageContext{publisher=" + publisher + "}";
        } else if (subscriber != null) {
            return "DBEventMessageContext{subscriber=" + subscriber + "}";
        } else {
            return "DBEventMessageContext{empty}";
        }
    }
}
