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

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

//Used for stress test. use together with TimerThread
public class Counter {

    private AtomicLong counter = new AtomicLong(0);

    private AtomicReference<String> otherStringValue = new AtomicReference<String>();

    public void addCounter() {
        counter.getAndIncrement();

    }

    public synchronized void addCounter(String otherValue) {
        counter.getAndIncrement();
        otherStringValue.set(otherValue);
    }

    /**
     * @return Returns the counterValue.
     */
    public long getCounterValue() {

        return counter.get();
    }

    /**
     * @param counterValue
     *            The counterValue to set.
     */
    public void setCounterValue(long counterValue) {
        counter.set(counterValue);

    }

    /**
     * @return Returns the otherValueString.
     */
    public String getOtherValueString() {

        return otherStringValue.get();
    }

    /**
     * @param otherValueString
     *            The otherValueString to set.
     */
    public void setOtherValueString(String otherValueString) {
        otherStringValue.set(otherValueString);
    }

}
