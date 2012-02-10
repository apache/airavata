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

import org.apache.airavata.common.utils.XMLUtil;
import org.junit.Test;


public class XMLUtilTest {
    @Test
    public void isXMLTest(){
        String xml = "<test>testing</test>";
        org.junit.Assert.assertTrue(XMLUtil.isXML(xml));
        org.junit.Assert.assertFalse(XMLUtil.isXML("NonXMLString"));
    }

    @Test
    public void isEqualTest(){
        String xml1 = "<test><inner>innerValue</inner></test>";
        String xml2 = "<test><inner>innerValue</inner></test>";
        String xml3 = "<test1><inner>innerValue</inner></test1>";
        try {
            org.junit.Assert.assertTrue(XMLUtil.isEqual(XMLUtil.stringToXmlElement(xml1), XMLUtil.stringToXmlElement(xml2)));
            org.junit.Assert.assertFalse(XMLUtil.isEqual(XMLUtil.stringToXmlElement(xml1), XMLUtil.stringToXmlElement(xml3)));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    @Test
    public void getQNameTest(){
        String qname = "ns1:a";
        org.junit.Assert.assertEquals("a",XMLUtil.getLocalPartOfQName(qname));
        org.junit.Assert.assertEquals("ns1",XMLUtil.getPrefixOfQName(qname));
    }
}
