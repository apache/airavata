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

package org.apache.airavata.common.utils;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.airavata.common.exception.ApplicationSettingsException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 7/5/13
 * Time: 4:39 PM
 */

public class ApplicationSettingsTest extends TestCase {
    public void testGetAbsoluteSetting() throws Exception {

        System.setProperty(AiravataUtils.EXECUTION_MODE, "SERVER");
        String url = ApplicationSettings.getAbsoluteSetting("registry.service.wsdl");
        Assert.assertEquals("http://192.2.33.12:8080/airavata-server/services/RegistryService?wsdl", url);

    }

    public void testGetAbsoluteSettingWithSpecialCharacters() throws Exception {

        System.setProperty(AiravataUtils.EXECUTION_MODE, "SERVER");
        String url = ApplicationSettings.getAbsoluteSetting("registry.service.wsdl2");
        Assert.assertEquals("http://localhost:8080/airavata-server/services/RegistryService?wsdl", url);

    }


}
