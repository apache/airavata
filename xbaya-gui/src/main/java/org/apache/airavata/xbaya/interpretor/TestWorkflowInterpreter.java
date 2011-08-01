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

package org.apache.airavata.xbaya.interpretor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.ode.ODEClient;
import org.apache.airavata.xbaya.security.SecurityUtil;
import org.apache.airavata.xbaya.util.Pair;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.proxy.GPELWorkflowClient;
import org.apache.airavata.xbaya.workflow.proxy.GPELWorkflowContext;
import org.apache.airavata.xbaya.workflow.proxy.WorkflowClient;
import org.apache.airavata.xbaya.workflow.proxy.WorkflowContext;
import org.ietf.jgss.GSSException;

import xsul5.wsdl.WsdlDefinitions;

public class TestWorkflowInterpreter {

    public static void main(String[] args) throws XBayaException, URISyntaxException, GSSException, IOException {
        String workflowAsString = getWorkflow();

        Workflow workflow = new Workflow(workflowAsString);
        WsdlDefinitions wsdl = workflow.getTridentWorkflowWSDL(XBayaConstants.DEFAULT_DSC_URL,
                XBayaConstants.DEFAULT_ODE_URL.toString());
        System.out.println("llllllllllllllllllllllllll");
        System.out.println(wsdl.xmlStringPretty());

        ODEClient client = new ODEClient();
        Pair<String, String>[] in = new Pair[3];

        List<WSComponentPort> inputs = client.getInputs(workflow);
        for (WSComponentPort port : inputs) {
            if (port.getName().equals("CrossCuttingConfigurations")) {
                // Object val = client.parseValue(port,

                String val1 = "<CrossCuttingConfigurations n2:leadType='LeadCrosscutParameters' xmlns:n2='http://www.extreme.indiana.edu/namespaces/2004/01/gFac'>"
                        + "<lcp:nx xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>203</lcp:nx>"
                        + "<lcp:ny xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>203</lcp:ny>"
                        + "<lcp:dx xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>5000</lcp:dx>"
                        + "<lcp:dy xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>5000</lcp:dy>"
                        + "<lcp:ctrlat xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>44.85</lcp:ctrlat>"
                        + "<lcp:ctrlon xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>-86.25</lcp:ctrlon>"
                        + "<lcp:fcst_time xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>36</lcp:fcst_time>"
                        + "<lcp:start_date xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>7/30/2010</lcp:start_date>"
                        + "<lcp:start_hour xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>9</lcp:start_hour>"
                        + "<lcp:westbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>-90.74158</lcp:westbc>"
                        + "<lcp:eastbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>-81.75842</lcp:eastbc>"
                        + "<lcp:northbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>47.95601</lcp:northbc>"
                        + "<lcp:southbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>41.56573</lcp:southbc>"
                        + "</CrossCuttingConfigurations>";
                in[0] = new Pair<String, String>("CrossCuttingConfigurations", val1);

                port.setDefaultValue(val1);
                // port.setValue(val);

            } else if (port.getName().equals("NAMInitialData")) {
                // Object val = client.parseValue(port,
                in[1] = new Pair<String, String>(
                        "NAMInitialData",
                        "gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f03");
                port.setDefaultValue("gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f03");
                // port.setValue(val);
            } else if (port.getName().equals("NAMLateralBoundaryData")) {
                // Object val = client.parseValue(port,
                in[2] = new Pair<String, String>(
                        "NAMLateralBoundaryData",
                        "gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f06 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f09 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f12 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f15 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f18 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f21 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f24 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f27 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f30 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f33 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f36 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f39");
                port.setDefaultValue("gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f06 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f09 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f12 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f15 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f18 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f21 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f24 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f27 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f30 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f33 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f36 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f39");
                // port.setValue(val);
            }

        }

        String userName = "chathura";
        String password = "changeme";
        SecurityUtil.getGSSCredential(userName, password, XBayaConstants.DEFAULT_MYPROXY_SERVER);

        invoker(userName, password, workflowAsString, in, workflow);

        //
        // workflowAsString = getWorkflow();
        //
        //
        // workflow = new Workflow(workflowAsString);
        // wsdl =
        // workflow.getTridentWorkflowWSDL(XBayaConstants.DEFAULT_DSC_URL,
        // XBayaConstants.DEFAULT_ODE_URL.toString());
        // System.out.println("llllllllllllllllllllllllll");
        // System.out.println(wsdl.xmlStringPretty());

        // List<InputNode> inputs = new ODEClient().getInputNodes(workflow);
        // for (Iterator iterator = inputs.iterator(); iterator.hasNext();) {
        // InputNode inputNode = (InputNode) iterator.next();
        // if ("input".equals(inputNode.getName())) {
        // inputNode.setDefaultValue("MyechoString");
        // } // other else ifs for other inputs
        //
        // }
        //
        // XBayaConfiguration configuration = getConfiguration();
        //
        //
        // WorkflowInterpreter interpreter = new WorkflowInterpreter(
        // configuration, "mytopic", workflow, "username", "password");
        // interpreter.scheduleDynamically();

        // String userName = "chathura";
        // String password = "changeme";
        // ODEClient client = new ODEClient();
        // GSSCredential credential = client.getGSSCredential(userName,
        // password,
        // XBayaConstants.DEFAULT_MYPROXY_SERVER);
        //
        // Workflow workflow1 = client.getWorkflowFromOGCE(new URI(
        // "https://ogceportal.iu.teragrid.org:19443/xregistry"),
        // credential, new
        // QName("Public_NAM_Initialized_WRF_Forecastc55d6223-7f79-4c07-824c-804c6b12782d"));
        //
        // try {
        // BufferedWriter out = new BufferedWriter(new
        // FileWriter("/nfs/mneme/home/users/cherath/projects/test/extremeWorkspace/xbaya-gui/workflows/t-new.xwf"));
        // out.write(workflow1.toXMLText());
        // out.close();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        //
        //
        // List<InputNode> inputNodes = client.getInputNodes(workflow1);
        // for (InputNode port : inputNodes) {
        // if (port.getName().equals("CrossCuttingConfigurations")) {
        // // Object val = client.parseValue(port,
        //
        // String val1 =
        // "<CrossCuttingConfigurations n2:leadType='LeadCrosscutParameters' xmlns:n2='http://www.extreme.indiana.edu/namespaces/2004/01/gFac'>"
        // +"<lcp:nx xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>203</lcp:nx>"
        // +"<lcp:ny xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>203</lcp:ny>"
        // +"<lcp:dx xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>5000</lcp:dx>"
        // +"<lcp:dy xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>5000</lcp:dy>"
        // +"<lcp:ctrlat xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>44.85</lcp:ctrlat>"
        // +"<lcp:ctrlon xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>-86.25</lcp:ctrlon>"
        // +"<lcp:fcst_time xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>36</lcp:fcst_time>"
        // +"<lcp:start_date xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>7/30/2010</lcp:start_date>"
        // +"<lcp:start_hour xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>9</lcp:start_hour>"
        // +"<lcp:westbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>-90.74158</lcp:westbc>"
        // +"<lcp:eastbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>-81.75842</lcp:eastbc>"
        // +"<lcp:northbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>47.95601</lcp:northbc>"
        // +"<lcp:southbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>41.56573</lcp:southbc>"
        // +"</CrossCuttingConfigurations>";
        //
        // port.setDefaultValue(val1);
        // // port.setValue(val);
        //
        //
        //
        //
        // } else if (port.getName().equals("NAMInitialData")) {
        // // Object val = client.parseValue(port,
        // port.setDefaultValue("gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f03");
        // // port.setValue(val);
        // } else if (port.getName().equals("NAMLateralBoundaryData")) {
        // // Object val = client.parseValue(port,
        // port.setDefaultValue("gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f06 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f09 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f12 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f15 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f18 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f21 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f24 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f27 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f30 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f33 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f36 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f39");
        // // port.setValue(val);
        // }
        //
        // }
        //
        // WorkflowInterpreter interpreter = new WorkflowInterpreter(
        // getConfiguration(), "mytopic333", workflow, "chathura", "changeme");
        // interpreter.scheduleDynamically();

    }

    /**
     * @return
     */
    private static XBayaConfiguration getConfiguration() {
        XBayaConfiguration configuration = new XBayaConfiguration();
        configuration.setBrokerURL(XBayaConstants.DEFAULT_BROKER_URL);
        configuration.setDSCURL(XBayaConstants.DEFAULT_DSC_URL);
        configuration.setGFacURL(XBayaConstants.DEFAULT_GFAC_URL);
        configuration.setMessageBoxURL(XBayaConstants.DEFAULT_MESSAGE_BOX_URL);
        configuration.setMyProxyLifetime(XBayaConstants.DEFAULT_MYPROXY_LIFTTIME);
        configuration.setMyProxyPort(XBayaConstants.DEFAULT_MYPROXY_PORT);
        configuration.setMyProxyServer(XBayaConstants.DEFAULT_MYPROXY_SERVER);
        configuration.setXRegistryURL(XBayaConstants.DEFAULT_XREGISTRY_URL);
        return configuration;
    }

    /**
     * @return
     */
    private static String getWorkflow() {
        File file = new File(
                "/nfs/mneme/home/users/cherath/projects/test/extremeWorkspace/xbaya/workflows/Vortex2_2010_Workflow_April_27.xwf");
        String workflowAsString = "";
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line = in.readLine();

            while (null != line) {
                workflowAsString += line;
                line = in.readLine();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return workflowAsString;
    }

    public static Pair<String, String> invoker(String userName, String password, String workflowAsAString,
            Pair<String, String>[] inputs, Workflow workflow) throws IOException, GSSException {

        WorkflowClient wfClient = null;
        WorkflowContext context = null;
        String topic = UUID.randomUUID().toString();

        context = new GPELWorkflowContext(topic, userName, password);
        wfClient = new GPELWorkflowClient(context, workflow);
        wfClient.init();
        try {
            System.out.println(workflow.getGPELTemplateID());
            context.prepare(wfClient, workflow);
        } catch (GSSException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return wfClient.invoke(inputs);

    }

}