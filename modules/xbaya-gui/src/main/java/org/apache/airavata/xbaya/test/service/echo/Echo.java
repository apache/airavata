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

package org.apache.airavata.xbaya.test.service.echo;

import java.io.File;

import org.apache.airavata.xbaya.test.service.Service;
import org.xmlpull.v1.builder.XmlElement;

import xsul.xwsif_runtime.XmlElementBasedStub;

public interface Echo extends XmlElementBasedStub {

    /**
     * SERVICE_NAME
     */
    public final static String SERVICE_NAME = "EchoService";

    /**
     * WSDL_NAME
     */
    public final static String WSDL_NAME = "echo.wsdl";

    /**
     * WSDL_PATH
     */
    public final static String WSDL_PATH = Service.MATH_DIRECTORY_NAME + File.separator + WSDL_NAME;

    /**
     * @param input
     *            the input message
     * @return the output message
     */
    public XmlElement echo(XmlElement input);
}