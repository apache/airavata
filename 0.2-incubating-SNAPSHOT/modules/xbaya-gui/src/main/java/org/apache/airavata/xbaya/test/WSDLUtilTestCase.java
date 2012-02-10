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

import java.net.URI;

import org.apache.airavata.common.utils.WSDLUtil;

public class WSDLUtilTestCase extends XBayaTestCase {

    /**
     *
     */
    public void testAppendWSDLQuary() {
        URI uri0 = URI.create("http://localhost:8080");
        URI wsdlURI0 = WSDLUtil.appendWSDLQuary(uri0);
        assertEquals("http://localhost:8080/?wsdl", wsdlURI0.toString());

        URI uri1 = URI.create("http://localhost:8080/");
        URI wsdlURI1 = WSDLUtil.appendWSDLQuary(uri1);
        assertEquals("http://localhost:8080/?wsdl", wsdlURI1.toString());

        URI uri2 = URI.create("http://localhost:8080/service");
        URI wsdlURI2 = WSDLUtil.appendWSDLQuary(uri2);
        assertEquals("http://localhost:8080/service?wsdl", wsdlURI2.toString());

        URI uri3 = URI.create("http://localhost:8080/?wsdl");
        URI wsdlURI3 = WSDLUtil.appendWSDLQuary(uri3);
        assertEquals("http://localhost:8080/?wsdl", wsdlURI3.toString());
    }

}