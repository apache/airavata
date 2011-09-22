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

package org.apache.airavata.wsmg.util;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.NameSpaceConstants;
import org.apache.airavata.wsmg.util.BrokerUtil;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BrokerUtilTest extends TestCase {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }
    
    /**
     * Test method for
     * {@link org.apache.airavata.wsmg.util.BrokerUtil#sameStringValue(java.lang.String, java.lang.String)} .
     */
    @Test
    public void testSameStringValue() {

        assertTrue(BrokerUtil.sameStringValue(null, null));
        assertTrue(BrokerUtil.sameStringValue("test", "test"));

        assertFalse(BrokerUtil.sameStringValue("one", "two"));
        assertFalse(BrokerUtil.sameStringValue(null, "test"));
        assertFalse(BrokerUtil.sameStringValue("test", null));

    }  

    /**
     * Test method for {@link org.apache.airavata.wsmg.util.BrokerUtil#getTopicLocalString(java.lang.String)}.
     */
    @Test
    public void testGetTopicLocalString() {

        assertEquals("localstring", (BrokerUtil.getTopicLocalString("prefix:localstring")));

        assertEquals("localstring", BrokerUtil.getTopicLocalString("localstring"));

    }

    /**
     * Test method for {@link org.apache.airavata.wsmg.util.BrokerUtil#getXPathString(org.apache.axiom.om.OMElement)}.
     */
    @Test
    public void testGetXPathString() {

        OMFactory factory = OMAbstractFactory.getOMFactory();

        try {

            BrokerUtil.getXPathString(null);
            fail("method should validate invalid arguments");
        } catch (IllegalArgumentException e) {

        } catch (AxisFault e) {
            fail("invalid exception thrown");
        }

        try {

            QName invalidQName = new QName("invalidURI", "invalidLocalName");

            OMElement xpathEl = factory.createOMElement(invalidQName);

            BrokerUtil.getXPathString(xpathEl);

            fail("method should validate arguments");

        } catch (AxisFault fault) {

        }

        try {

            String xpathExpression = "testXpathExpression";
            String dialect = "unknownXpathDialect";

            OMNamespace ns = factory.createOMNamespace("unit_test", "jnt");

            OMElement xpathEl = factory.createOMElement("TestXpath", ns);
            xpathEl.addAttribute("Dialect", dialect, NameSpaceConstants.WSE_NS);

            xpathEl.setText(xpathExpression);

            BrokerUtil.getXPathString(xpathEl);

            fail("method should reject unknown dialect");
        } catch (AxisFault e) {

        }

        try {

            String xpathExpression = "textXpathExpression";
            String dialect = WsmgCommonConstants.XPATH_DIALECT;

            OMNamespace ns = factory.createOMNamespace("unit_test", "jnt");

            OMElement xpathEl = factory.createOMElement("TestXpath", ns);
            xpathEl.addAttribute("Dialect", dialect, NameSpaceConstants.WSE_NS);

            xpathEl.setText(xpathExpression);
            assertEquals(xpathExpression, BrokerUtil.getXPathString(xpathEl));

        } catch (AxisFault e) {
            fail("unable to extract xpath query: " + e.toString());
        }

    }

    /**
     * Test method for {@link org.apache.airavata.wsmg.util.BrokerUtil#getTopicFromRequestPath(java.lang.String)}.
     */
    @Test
    public void testGetTopicFromRequestPath() {

        assertNull(BrokerUtil.getTopicFromRequestPath(null));
        assertNull(BrokerUtil.getTopicFromRequestPath(""));
        assertNull(BrokerUtil.getTopicFromRequestPath("/"));
        assertNull(BrokerUtil.getTopicFromRequestPath("/subscribe/url/"));
        assertNull(BrokerUtil.getTopicFromRequestPath("/subscribe/url/topic/"));

        assertEquals(BrokerUtil.getTopicFromRequestPath("/requestpath/topic/xyz"), "xyz");

    }
}
