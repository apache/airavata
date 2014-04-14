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

import java.io.IOException;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class XppTestCase extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(XppTestCase.class);

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @throws XmlPullParserException
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     * @throws IOException
     */
    public void testEscape() throws XmlPullParserException, IllegalArgumentException, IllegalStateException,
            IOException {

        String testString = "<tag>&lt;test/><[CDATA[<tag2/>]]></tag>";

        logger.info("before:");
        logger.info(testString);

        XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
        XmlSerializer xmlSerializer = xppFactory.newSerializer();
        StringWriter stringwriter = new StringWriter();
        xmlSerializer.setOutput(stringwriter);
        xmlSerializer.text(testString);
        String afterString = stringwriter.toString();

        logger.info("after:");
        logger.info(afterString);
    }
}