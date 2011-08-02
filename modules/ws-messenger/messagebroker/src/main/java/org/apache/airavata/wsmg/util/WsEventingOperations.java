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

public enum WsEventingOperations {

    RENEW("renew"), PUBLISH("publish"), GET_STATUS("getStatus"), SUBSCRIPTION_END("subscriptionEnd"), SUBSCRIBE(
            "subscribe"), UNSUBSCRIBE("unsubscribe");

    private final String name;

    private WsEventingOperations(String n) {
        name = n;
    }

    public String toString() {
        return name;
    }

    public boolean equals(String s) {
        return name.equals(s);
    }

    public static WsEventingOperations valueFrom(String s) {
        for (WsEventingOperations status : WsEventingOperations.values()) {
            if (status.toString().equalsIgnoreCase(s)) {
                return status;
            }

        }

        throw new RuntimeException("invalid WsEventingOperation:- " + s);

    }

}
