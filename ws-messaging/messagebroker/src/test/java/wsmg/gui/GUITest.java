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

package wsmg.gui;

import java.net.URL;
import java.util.Properties;

import org.apache.airavata.wsmg.gui.NotificationViewer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import wsmg.util.ConfigKeys;

public class GUITest {

    static Properties configs = new Properties();

    @Before
    public void setUp() throws Exception {
        URL configURL = ClassLoader.getSystemResource(ConfigKeys.CONFIG_FILE_NAME);
        configs.load(configURL.openStream());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGUI() {
        String[] args = new String[] { configs.getProperty(ConfigKeys.AXIS2_REPO) };

        NotificationViewer.main(args);

    }

}
