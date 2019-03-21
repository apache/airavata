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
package org.apache.airavata.service.security.authzcache;

/**
 * Cache entry in the default authorization cache.
 */
public class AuthzCacheEntry {
    //authorization decision for the authorization request associated with this cache entry.
    private boolean decision;
    //time to live value for the access token in seconds.
    private long expiryTime;
    //time stamp in milli seconds at the time this entry is put into the cache
    private long entryTimestamp;

    public AuthzCacheEntry(boolean decision, long expiryTime, long entryTimestamp) {
        this.decision = decision;
        this.expiryTime = expiryTime;
        this.entryTimestamp = entryTimestamp;
    }

    public long getEntryTimestamp() {
        return entryTimestamp;
    }

    public void setEntryTimestamp(long entryTimestamp) {
        this.entryTimestamp = entryTimestamp;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long timestamp) {
        this.expiryTime = timestamp;
    }

    public boolean getDecision() {
        return decision;
    }

    public void setDecision(boolean decision) {
        this.decision = decision;
    }
}
