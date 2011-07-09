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

package org.apache.airavata.core.gfac.extension.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.context.impl.ParameterContextImpl;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.GfacException.FaultCode;
import org.apache.airavata.core.gfac.extension.DataServiceChain;
import org.apache.airavata.core.gfac.model.ExecutionModel;
import org.apache.airavata.core.gfac.registry.RegistryService;
import org.apache.airavata.core.gfac.type.StringParameter;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.xmlbeans.XmlException;
import org.ogce.schemas.gfac.documents.ApplicationDescriptionDocument;
import org.ogce.schemas.gfac.documents.ApplicationDescriptionType;
import org.ogce.schemas.gfac.documents.DeploymentDescriptionType;
import org.ogce.schemas.gfac.documents.GlobusGatekeeperType;
import org.ogce.schemas.gfac.documents.HostDescriptionDocument;
import org.ogce.schemas.gfac.documents.HostDescriptionType;
import org.ogce.schemas.gfac.documents.NameValuePairType;
import org.ogce.schemas.gfac.documents.OutputParameterType;
import org.ogce.schemas.gfac.documents.ServiceMapDocument;
import org.ogce.schemas.gfac.documents.ServiceMapType;

public class RegistryDataService extends DataServiceChain {

    private static final String INPUT_MESSAGE_CONTEXT = "input";
    private static final String OUTPUT_MESSAGE_CONTEXT = "output";

    public boolean execute(InvocationContext context) throws GfacException {

        /*
         * Load host and app description from registry
         */
        RegistryService registryService = context.getExecutionContext().getRegistryService();
        String serviceMapStr = registryService.getServiceMap(context.getServiceName());
        System.out.println(serviceMapStr);
        if (serviceMapStr != null) {
            try {

                ServiceMapType serviceMap = ServiceMapDocument.Factory.parse(serviceMapStr).getServiceMap();
                QName appName = GfacUtils.findApplcationName(serviceMap);

                // host name
                String hostName = findHostFromServiceMap(registryService, appName);

                // app
                String appDesc = registryService.getAppDesc(appName.toString(), hostName);
                ApplicationDescriptionType appDescType = ApplicationDescriptionDocument.Factory.parse(appDesc)
                        .getApplicationDescription();

                // host desc
                String hostDesc = registryService.getHostDesc(hostName);
                HostDescriptionType hostDescType = HostDescriptionDocument.Factory.parse(hostDesc).getHostDescription();

                // application deployment
                DeploymentDescriptionType deploymentDesc = appDescType.getDeploymentDescription();
                String tmpDir = deploymentDesc.getTmpDir();
                if (tmpDir == null && hostDescType != null) {
                    tmpDir = hostDescType.getHostConfiguration().getTmpDir();
                }

                if (tmpDir == null) {
                    tmpDir = "/tmp";
                }

                String date = new Date().toString();
                date = date.replaceAll(" ", "_");
                date = date.replaceAll(":", "_");

                tmpDir = tmpDir + File.separator + appDescType.getApplicationName().getStringValue() + "_" + date + "_"
                        + UUID.randomUUID();

                String workingDir = deploymentDesc.getWorkDir();
                if (workingDir == null || workingDir.trim().length() == 0) {
                    workingDir = tmpDir;
                }

                String stdOut = tmpDir + File.separator + appDescType.getApplicationName().getStringValue() + ".stdout";
                String stderr = tmpDir + File.separator + appDescType.getApplicationName().getStringValue() + ".stderr";
                String executable = deploymentDesc.getExecutable();
                String host = deploymentDesc.getHostName();

                NameValuePairType[] env = deploymentDesc.getApplicationEnvArray();
                Map<String, String> envMap = new HashMap<String, String>();
                if (env != null) {
                    for (int i = 0; i < env.length; i++) {
                        envMap.put(env[i].getName(), env[i].getValue());
                    }
                }

                String inputDataDir = tmpDir + File.separator + "inputData";
                String outputDataDir = tmpDir + File.separator + "outputData";

                GlobusGatekeeperType[] gatekeepers = hostDescType.getHostConfiguration().getGlobusGatekeeperArray();

                ExecutionModel model = new ExecutionModel();
                model.setHost(host);
                model.setExecutable(executable);
                model.setTmpDir(tmpDir);
                model.setWorkingDir(workingDir);
                model.setStdOut(stdOut);
                model.setStderr(stderr);
                model.setInputDataDir(inputDataDir);
                model.setOutputDataDir(outputDataDir);
                model.setEnv(envMap);
                model.setAplicationDesc(appDescType);
                model.setHostDesc(hostDescType);
                model.setGatekeeper(gatekeepers[0]);

                // input parameter
                ArrayList<String> tmp = new ArrayList<String>();
                for (Iterator<String> iterator = context.getMessageContext(INPUT_MESSAGE_CONTEXT).getParameterNames(); iterator
                        .hasNext();) {
                    String key = iterator.next();
                    tmp.add(context.getMessageContext(INPUT_MESSAGE_CONTEXT).getStringParameterValue(key));
                }
                model.setInputParameters(tmp);

                context.getExecutionContext().setExectionModel(model);

                // output parameter
                // TODO type mapping
                if (serviceMap.getPortTypeArray(0).getMethodArray(0).getOutputParameterArray() != null) {
                    ParameterContextImpl outtmp = new ParameterContextImpl();
                    for (OutputParameterType output : serviceMap.getPortTypeArray(0).getMethodArray(0)
                            .getOutputParameterArray()) {
                        outtmp.addParameter(output.getParameterName(), output.getParameterType().toString(),
                                new StringParameter());
                    }
                    context.addMessageContext(OUTPUT_MESSAGE_CONTEXT, outtmp);
                }

            } catch (XmlException e) {
                throw new GfacException(e, FaultCode.InitalizationError);
            }
        } else {
            throw new GfacException("Service Map for " + context.getServiceName()
                    + " does not found on resource Catalog " + registryService, FaultCode.InvalidRequest);
        }

        return false;
    }

    private String findHostFromServiceMap(RegistryService regService, QName appName) throws GfacException {

        System.out.println("Searching registry for some deployed application hosts\n");
        String[] hosts = regService.app2Hosts(appName.toString());
        if (hosts.length > 1) {
            String hostNames = "";
            for (int i = 0; i < hosts.length; i++) {
                hostNames = hostNames + hosts[i];
            }
            System.out.println("Application deployed on more than one machine. The full Host list is " + hostNames
                    + "\n");
        }
        if (hosts.length >= 1) {
            System.out.println("Found Host = " + hosts[0]);
            return hosts[0];
        } else {
            System.out.println("Applcation  " + appName.getLocalPart() + " not found in registry");
            return null;
        }
    }

}
