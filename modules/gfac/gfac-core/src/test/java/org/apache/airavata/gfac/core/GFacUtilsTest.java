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
package org.apache.airavata.gfac.core;

import org.junit.Assert;
import org.junit.Test;

public class GFacUtilsTest {

    @Test
    public void testGetQoS_1() throws Exception {
        String qos = "shared=oneweek";
        String shared = GFacUtils.getQoS(qos, "shared");
        Assert.assertNotNull(shared);
        Assert.assertEquals("oneweek", shared);
    }
    @Test
    public void testGetQoS_2() throws Exception {
        String qos = "shared=oneweek,compute=oneweek";
        String shared = GFacUtils.getQoS(qos, "shared");
        Assert.assertNotNull(shared);
        Assert.assertEquals("oneweek", shared);
    }


    @Test
    public void testGetQoS_3() throws Exception {
        String qos = "shared=oneweek";
        String shared = GFacUtils.getQoS(qos, "compute");
        Assert.assertNull(shared);
    }

    @Test
    public void parserCommandTest() throws Exception {
        String command = "mkdir -p $scratchLocation/$gatewayId/$gatewayUserName/$applicationName";
        GroovyMap groovyMap = new GroovyMap();
        groovyMap.add(Script.SCRATCH_LOCATION, "/my/scratch");
        groovyMap.add(Script.GATEWAY_ID, "seagrid");
        groovyMap.add(Script.GATEWAY_USER_NAME, "John");
        groovyMap.add(Script.APPLICATION_NAME, "gaussian");
        String value = GFacUtils.parseCommands(command, groovyMap);
        Assert.assertNotNull(value);
        Assert.assertEquals("mkdir -p /my/scratch/seagrid/John/gaussian", value);
    }

    @Test
    public void parserCommandTestWithEscapeChar() throws Exception {
        String command = "abq_job=\\${baseinp%.*}";
        GroovyMap groovyMap = new GroovyMap();
        String value = GFacUtils.parseCommands(command, groovyMap);
        Assert.assertNotNull(value);
        Assert.assertEquals("abq_job=${baseinp%.*}", value);
    }
}