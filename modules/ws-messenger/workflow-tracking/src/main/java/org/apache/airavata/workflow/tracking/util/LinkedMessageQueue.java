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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * similar to linkedblocking queue but Non-concurrent version. can have only one thread putting elements into queue and
 * another thread getting elements from queue. has added method get that does a block-peek that is missing in
 * linkedblocking queue. implemented using a linked list.
 */
public class LinkedMessageQueue<E> implements Iterable {

    private final LinkedList<E> list;
    private final AtomicInteger count;
    private final int capacity;

    private final Object takeLock = new Object();
    private final Object putLock = new Object();

    public LinkedMessageQueue() {
        this(Integer.MAX_VALUE); // default capacity is MAX_INT
    }

    public LinkedMessageQueue(int maxSize) {
        list = new LinkedList<E>();
        count = new AtomicInteger(0);
        capacity = maxSize;
    }

    /*** add, offer, and put are called by application thread adding to notification queue ***/

    /** add to tail of queue if not full; throw exceptionif unable to add */
    public final void add(E entry) {

        if (count.get() >= capacity) {
            throw new IllegalStateException("Cannot add element. queue is full.");
        }

        list.add(entry);
        count.incrementAndGet();

        synchronized (putLock) {
            putLock.notify();
        }
    }

    /** add to tail of queue if possible; return false if unable to add */
    public final boolean offer(E entry) {

        if (count.get() >= capacity) {
            return false;
        }

        list.add(entry);
        count.incrementAndGet();

        synchronized (putLock) {
            putLock.notify();
        }
        return true;
    }

    /** add to tail of queue, blocking if necessary */
    public final void put(E entry) throws InterruptedException {

        if (count.get() >= capacity) { // do initial check before checking & waiting
            synchronized (takeLock) {
                while (count.get() >= capacity) {
                    takeLock.wait();
                }
            }
        }

        list.add(entry);
        count.incrementAndGet();

        synchronized (putLock) {
            putLock.notify();
        }
    }

    /*** poll, get, peek, and take are called by publisher thread removing from notification queue ***/

    /** return & remove head of queue; do not block & return null if none available */
    public final E poll() {

        if (count.get() <= 0)
            return null;

        count.decrementAndGet();
        E entry = list.removeFirst();

        synchronized (takeLock) {
            takeLock.notify();
        }
        return entry;
    }

    /** return (but dont remove) head of queue; block if empty */
    public final E get() throws InterruptedException {

        if (count.get() <= 0) { // do initial check before checking & waiting
            synchronized (putLock) {
                while (count.get() <= 0) {
                    putLock.wait();
                }
            }
        }

        return list.getFirst();
    }

    /** return (but dont remove) head of queue; return null if empty */
    public final E peek() {

        if (count.get() <= 0)
            return null;
        else
            return list.getFirst();
    }

    /** return & remove head of queue; block if empty */
    public final E take() throws InterruptedException {

        if (count.get() <= 0) { // do initial check before checking & waiting
            synchronized (putLock) {
                while (count.get() <= 0) {
                    putLock.wait();
                }
            }
        }

        count.decrementAndGet();
        final E entry = list.removeFirst();

        synchronized (takeLock) {
            takeLock.notify();
        }
        return entry;
    }

    /** return number of entries in queue */
    public final int size() {

        return count.get();
    }

    public final Iterator<E> iterator() {

        return list.iterator();
    }

}
