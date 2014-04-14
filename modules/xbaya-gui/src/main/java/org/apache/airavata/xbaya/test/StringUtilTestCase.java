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

package org.apache.airavata.xbaya.test;

import junit.framework.TestCase;

import org.apache.airavata.common.utils.StringUtil;

public class StringUtilTestCase extends TestCase {

    /**
     * 
     */
    public void testIncrementName() {
        String name1 = "test";
        String name2 = StringUtil.incrementName(name1);
        assertEquals(name2, "test_2");
        String name3 = StringUtil.incrementName(name2);
        assertEquals(name3, "test_3");

        String name9 = "test_9";
        String name10 = StringUtil.incrementName(name9);
        assertEquals(name10, "test_10");
        String name11 = StringUtil.incrementName(name10);
        assertEquals(name11, "test_11");

        String nameA = "test_a";
        String nameA2 = StringUtil.incrementName(nameA);
        assertEquals(nameA2, "test_a_2");

        String name = "test_";
        String name_2 = StringUtil.incrementName(name);
        assertEquals(name_2, "test__2");
    }

}