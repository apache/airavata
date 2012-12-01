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
package org.apache.airavata.core.gfac.provider.utils;

import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.exception.ToolsException;
import org.apache.xmlbeans.XmlException;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.POSIXApplicationDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.POSIXApplicationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSDLGenerator {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    public static JobDefinitionDocument configureRemoteJob(InvocationContext context) throws ToolsException {
        //todo read the InvocationContext and generate JSDL and return
        try {
//            GramApplicationDeploymentType app = (GramApplicationDeploymentType) context.getExecutionDescription().getApp().getType();
//            JobDefinitionDocument jobDefinitionDocument = JobDefinitionDocument.Factory.newInstance();
//            JobDefinitionType jobDefinitionType = jobDefinitionDocument.addNewJobDefinition();
//            JobDescriptionType jobDescriptionType = jobDefinitionType.addNewJobDescription();
//            POSIXApplicationDocument posixApplicationDocument = POSIXApplicationDocument.Factory.newInstance();
//            POSIXApplicationType posixApplicationType = posixApplicationDocument.addNewPOSIXApplication();
//            posixApplicationType.addNewExecutable().setFilesystemName(app.getExecutableLocation());
//            jobDescriptionType.setApplication(posixApplicationType);
            return JobDefinitionDocument.Factory.parse("<jsdl:JobDefinition xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:app=\"http://schemas.ggf.org/jsdl/2006/07/jsdl-hpcpa\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" xmlns:jsdl=\"http://schemas.ggf.org/jsdl/2005/11/jsdl\" xmlns:bes=\"http://schemas.ggf.org/bes/2006/08/bes-factory\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns=\"http://schemas.ggf.org/jsdl/2005/11/jsdl-posix\">\n" +
                    "  <jsdl:JobDescription>\n" +
                    "    <jsdl:Application>\n" +
                    "      <POSIXApplication>\n" +
                    "        <Executable>/bin/date</Executable>\n" +
                    "      </POSIXApplication>\n" +
                    "    </jsdl:Application>\n" +
                    "  </jsdl:JobDescription>\n" +
                    "</jsdl:JobDefinition>");
        } catch (XmlException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

}
