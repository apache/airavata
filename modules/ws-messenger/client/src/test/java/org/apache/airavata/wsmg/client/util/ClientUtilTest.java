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

package org.apache.airavata.wsmg.client.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ClientUtilTest {

    /**
     * Test method for {@link org.apache.airavata.wsmg.util.WsmgUtil#formatURLString(java.lang.String)}.
     */
    @Test
    public void testFormatURLString() {

        String url = "http://www.test.com/unit_test";

        assertSame(url, ClientUtil.formatURLString(url));

        url = "scp://test/test";

        assertSame(url, ClientUtil.formatURLString(url));

        url = "test/test";

        assertTrue(ClientUtil.formatURLString(url).startsWith("http://"));

    }

    /**
     * Test method for {@link org.apache.airavata.wsmg.util.WsmgUtil#getHostIP()}.
     */
    @Test
    public void testGetHostIP() {
        assertNotNull(ClientUtil.getHostIP());
    }

}
