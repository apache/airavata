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

import junit.framework.TestSuite;

import org.apache.airavata.common.utils.WSDLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xsul.wsdl.WsdlResolver;

public class XBayaSecurityTestCase extends XBayaTestCase {

    private static final Logger logger = LoggerFactory.getLogger(XBayaSecurityTestCase.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(XBayaSecurityTestCase.class));
    }

    /**
     * @see org.apache.airavata.xbaya.test.XBayaTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        // Security related XSUL is configured in super.setUp();
        super.setUp();
    }

    /**
     *
     */
    public void testWSDL() {
        URI gFacURL = this.configuration.getGFacURL();
        URI gFacWSDLURL = WSDLUtil.appendWSDLQuary(gFacURL);
        gFacWSDLURL = URI.create("https://tyr12.cs.indiana.edu:22443/data_catalog?wsdl"); // FAIL
        gFacWSDLURL = URI.create("https://tyr12.cs.indiana.edu:9443/wcs?wsdl"); // FAIL
        gFacWSDLURL = URI.create("https://tyr13.cs.indiana.edu:7443/gpel/"); // OK

        logger.info("accessing " + gFacWSDLURL);
        WsdlResolver.getInstance().loadWsdl(gFacWSDLURL);
    }

}