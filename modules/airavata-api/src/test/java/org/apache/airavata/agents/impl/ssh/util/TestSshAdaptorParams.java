/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.agents.impl.ssh.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.airavata.agents.ssh.SshAdaptorParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

public class TestSshAdaptorParams {

    @TempDir
    Path tempDir;

    @Test
    public void testSshAdaptorParamsCreationAndFileWrite() throws IOException {
        SshAdaptorParams params = new SshAdaptorParams();
        params.setUserName("dimuthu");
        params.setPassword("upe");
        params.setHostName("localhost");

        // Verify parameters are set
        assertEquals("dimuthu", params.getUserName());
        assertEquals("upe", params.getPassword());
        assertEquals("localhost", params.getHostName());

        // Write to temporary file
        File tempFile = tempDir.resolve("ssh-param.json").toFile();
        params.writeToFile(tempFile);

        // Verify file was created
        assertTrue(tempFile.exists(), "SSH params file should be created");
        assertTrue(tempFile.length() > 0, "SSH params file should not be empty");
    }
}
