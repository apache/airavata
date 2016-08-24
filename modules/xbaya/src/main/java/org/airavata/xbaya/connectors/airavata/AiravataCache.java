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
package org.airavata.xbaya.connectors.airavata;

import java.util.ArrayList;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LRUMap;


public class AiravataCache<K, T> {

    private long timeToLive;
    private LRUMap crunchifyCacheMap;

    protected class AiravataCacheObject {
        public long lastAccessed = System.currentTimeMillis();
        public T value;

        protected AiravataCacheObject(T value) {
            this.value = value;
        }
    }

    public AiravataCache(long crunchifyTimeToLive, final long crunchifyTimerInterval, int maxItems) {
        this.timeToLive = crunchifyTimeToLive * 1000;

        crunchifyCacheMap = new LRUMap(maxItems);

        if (timeToLive > 0 && crunchifyTimerInterval > 0) {

            Thread t = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(crunchifyTimerInterval * 1000);
                        } catch (InterruptedException ex) {
                        }
                        cleanup();
                    }
                }
            });

            t.setDaemon(true);
            t.start();
        }
    }

    public void put(K key, T value) {
        synchronized (crunchifyCacheMap) {
            crunchifyCacheMap.put(key, new AiravataCacheObject(value));
        }
    }

    @SuppressWarnings("unchecked")
    public T get(K key) {
        synchronized (crunchifyCacheMap) {
            AiravataCacheObject c = (AiravataCacheObject) crunchifyCacheMap.get(key);

            if (c == null)
                return null;
            else {
                c.lastAccessed = System.currentTimeMillis();
                return c.value;
            }
        }
    }

    public void remove(K key) {
        synchronized (crunchifyCacheMap) {
            crunchifyCacheMap.remove(key);
        }
    }

    public int size() {
        synchronized (crunchifyCacheMap) {
            return crunchifyCacheMap.size();
        }
    }

    @SuppressWarnings("unchecked")
    public void cleanup() {

        long now = System.currentTimeMillis();
        ArrayList<K> deleteKey = null;

        synchronized (crunchifyCacheMap) {
            MapIterator itr = crunchifyCacheMap.mapIterator();

            deleteKey = new ArrayList<K>((crunchifyCacheMap.size() / 2) + 1);
            K key = null;
            AiravataCacheObject c = null;

            while (itr.hasNext()) {
                key = (K) itr.next();
                c = (AiravataCacheObject) itr.getValue();

                if (c != null && (now > (timeToLive + c.lastAccessed))) {
                    deleteKey.add(key);
                }
            }
        }

        for (K key : deleteKey) {
            synchronized (crunchifyCacheMap) {
                crunchifyCacheMap.remove(key);
            }

            Thread.yield();
        }
    }
}