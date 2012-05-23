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
package org.apache.airavata.core.gfac;

import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.core.gfac.context.GFacConfiguration;
import org.apache.airavata.core.gfac.context.JobContext;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultExecutionContext;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultInvocationContext;
import org.apache.airavata.core.gfac.context.message.impl.ParameterContextImpl;
import org.apache.airavata.core.gfac.context.message.impl.WorkflowContextImpl;
import org.apache.airavata.core.gfac.context.security.impl.GSISecurityContext;
import org.apache.airavata.core.gfac.factory.PropertyServiceFactory;
import org.apache.airavata.core.gfac.notification.impl.LoggingNotification;
import org.apache.airavata.core.gfac.notification.impl.WorkflowTrackingNotification;
import org.apache.airavata.core.gfac.services.GenericService;
import org.apache.airavata.registry.api.Axis2Registry;
import org.apache.airavata.schemas.gfac.*;
import org.apache.airavata.schemas.wec.SecurityContextDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

public class GfacAPI {
    private static final Logger log = LoggerFactory.getLogger(GfacAPI.class);
    public static final String REPOSITORY_PROPERTIES = "repository.properties";

    public DefaultInvocationContext gridJobSubmit(JobContext jobContext,GFacConfiguration gfacConfig) throws Exception {
        String workflowNodeId = WorkflowContextHeaderBuilder.getCurrentContextHeader().getWorkflowMonitoringContext().getWorkflowNodeId();
        String workflowInstanceId = WorkflowContextHeaderBuilder.getCurrentContextHeader().getWorkflowMonitoringContext().getWorkflowInstanceId();
        WorkflowTrackingNotification workflowNotification = new WorkflowTrackingNotification(jobContext.getBrokerURL(),
                jobContext.getTopic(),workflowNodeId,workflowInstanceId);
        LoggingNotification loggingNotification = new LoggingNotification();
        DefaultInvocationContext invocationContext = new DefaultInvocationContext();
        invocationContext.setExecutionContext(new DefaultExecutionContext());
        invocationContext.setServiceName(jobContext.getServiceName());
        invocationContext.getExecutionContext().setRegistryService(gfacConfig.getRegistry());
        invocationContext.getExecutionContext().addNotifiable(workflowNotification);
        invocationContext.getExecutionContext().addNotifiable(loggingNotification);

        GSISecurityContext gssContext = new GSISecurityContext();
//        if (gridMyproxyRepository == null) {
            gssContext.setMyproxyPasswd(gfacConfig.getMyProxyPassphrase());
            gssContext.setMyproxyUserName(gfacConfig.getMyProxyUser());
            gssContext.setMyproxyLifetime(gfacConfig.getMyProxyLifeCycle());
            gssContext.setMyproxyServer(gfacConfig.getMyProxyServer());
//        } else {
//            gssContext.setMyproxyPasswd(gridMyproxyRepository.getPassword());
//            gssContext.setMyproxyUserName(gridMyproxyRepository.getUsername());
//            gssContext.setMyproxyLifetime(gridMyproxyRepository.getLifeTimeInhours());
//            gssContext.setMyproxyServer(gridMyproxyRepository.getMyproxyServer());
//        }
        gssContext.setTrustedCertLoc(gfacConfig.getTrustedCertLocation());

        invocationContext.addSecurityContext("myproxy", gssContext);

        /*
    * Add workflow context
    */
        ServiceDescription serviceDescription = gfacConfig.getRegistry().getServiceDescription(jobContext.getServiceName());
        ServiceDescriptionType serviceDescriptionType = serviceDescription.getType();
        ParameterContextImpl inputParam = new ParameterContextImpl();
        WorkflowContextImpl workflowContext = new WorkflowContextImpl();
        workflowContext.setValue(WorkflowContextImpl.WORKFLOW_ID, URI.create(jobContext.getTopic()).toString());
        invocationContext.addMessageContext(WorkflowContextImpl.WORKFLOW_CONTEXT_NAME, workflowContext);
        for(Parameter parameter:jobContext.getParameters().keySet()){
            inputParam.add(parameter.getParameterName(), jobContext.getParameters().get(parameter));
        }

        /*
    * Output
    */
        ParameterContextImpl outputParam = new ParameterContextImpl();


        // List<Parameter> outputs = serviceDescription.getOutputParameters();
        for (OutputParameterType parameter : serviceDescriptionType.getOutputParametersArray()) {
            ActualParameter actualParameter = new ActualParameter();
            if ("String".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(StringParameterType.type);
            } else if ("Double".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(DoubleParameterType.type);
            } else if ("Integer".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(IntegerParameterType.type);
            } else if ("Float".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(FloatParameterType.type);
            } else if ("Boolean".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(BooleanParameterType.type);
            } else if ("File".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(FileParameterType.type);
            } else if ("URI".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(URIParameterType.type);
            } else if ("StringArray".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(StringArrayType.type);
            } else if ("DoubleArray".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(DoubleArrayType.type);
            } else if ("IntegerArray".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(IntegerArrayType.type);
            } else if ("FloatArray".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(FloatArrayType.type);
            } else if ("BooleanArray".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(BooleanArrayType.type);
            } else if ("FileArray".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(FileArrayType.type);
            } else if ("URIArray".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(URIArrayType.type);
            }
            outputParam.add(parameter.getParameterName(), actualParameter);
        }

        invocationContext.setInput(inputParam);
        invocationContext.setOutput(outputParam);
        GenericService service = new PropertyServiceFactory(GfacAPI.REPOSITORY_PROPERTIES).createService();
        service.execute(invocationContext);
        return invocationContext;
    }


}