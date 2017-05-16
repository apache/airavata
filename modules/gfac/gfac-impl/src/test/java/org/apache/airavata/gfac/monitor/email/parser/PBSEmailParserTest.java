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
package org.apache.airavata.gfac.monitor.email.parser;

import org.apache.airavata.gfac.core.monitor.JobStatusResult;
import org.apache.airavata.model.status.JobState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by syodage on 9/9/16.
 */
public class PBSEmailParserTest {

    @Test
    public void parseContent_non_ASCII() throws Exception {
        PBSEmailParser parser = new PBSEmailParser();
        // test for non ascii contents
        String nonascii = "PBS Job Id: 33.torque_server\n" +
                "Job Name:   A2085606929\n" +
                "Exec host:  compute-0/0-9\n" +
                "Begun execution";
        JobStatusResult jsr = new JobStatusResult();
        parser.parseContent(nonascii, jsr);
        Assert.assertNotNull(jsr.getJobId());
        Assert.assertEquals("33.torque_server", jsr.getJobId());
        Assert.assertEquals("A2085606929", jsr.getJobName());
        Assert.assertEquals(JobState.ACTIVE, jsr.getState());
    }

}