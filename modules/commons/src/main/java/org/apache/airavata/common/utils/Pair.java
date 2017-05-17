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
package org.apache.airavata.common.utils;

public class Pair<L, R> {

    private L left;

    private R right;

    /**
     * Constructs a Pair.
     * 
     * @param left
     * @param right
     */
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Returns the left.
     * 
     * @return The left
     */
    public L getLeft() {
        return this.left;
    }

    /**
     * Sets left.
     * 
     * @param left
     *            The left to set.
     */
    public void setLeft(L left) {
        this.left = left;
    }

    /**
     * Returns the right.
     * 
     * @return The right
     */
    public R getRight() {
        return this.right;
    }

    /**
     * Sets right.
     * 
     * @param right
     *            The right to set.
     */
    public void setRight(R right) {
        this.right = right;
    }

}