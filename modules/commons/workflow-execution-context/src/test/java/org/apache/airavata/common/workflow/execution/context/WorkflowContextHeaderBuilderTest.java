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
package org.apache.airavata.common.workflow.execution.context;

import org.apache.airavata.common.utils.XMLUtil;
import org.junit.Test;

import java.io.File;

public class WorkflowContextHeaderBuilderTest {
     @Test
	public void testExecute() {
         WorkflowContextHeaderBuilder builder  = new WorkflowContextHeaderBuilder("brokerurl","gfacurl","registryurl","experimentid","workflowid");

         try {
             File testFile = new File(this.getClass().getClassLoader().getResource("result.xml").getPath());
             System.out.println(XMLUtil.xmlElementToString(XMLUtil.xmlElement3ToXmlElement5(builder.getXml())));
             System.out.println(XMLUtil.xmlElementToString(XMLUtil.loadXML(testFile)));
             org.junit.Assert.assertTrue(XMLUtil.isEqual(XMLUtil.loadXML(testFile), XMLUtil.xmlElement3ToXmlElement5(builder.getXml())));
         } catch (Exception e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }

}
