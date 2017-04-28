/**
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
 */
package org.apache.airavata.xbaya.interpretor;

public class WorkflowInterpreterInvoker {
    // implements HeaderConstants {
    //
    // public void invoke(Workflow workflow, String serverURL, String userName, String password, String topic)
    // throws AxisFault, RemoteException, ComponentException {
    //
    // String workflowAsString = workflow.toXMLText();
    // NameValue[] configurations = new NameValue[6];
    // configurations[0] = new NameValue();
    // configurations[0].setName(HEADER_ELEMENT_GFAC);
    // configurations[0].setValue(XBayaConstants.DEFAULT_GFAC_URL.toString());
    // configurations[1] = new NameValue();
    // configurations[1].setName(HEADER_ELEMENT_REGISTRY);
    // configurations[2] = new NameValue();
    // configurations[2].setName(HEADER_ELEMENT_PROXYSERVER);
    // configurations[2].setValue(XBayaConstants.DEFAULT_MYPROXY_SERVER);
    //
    // configurations[3] = new NameValue();
    // configurations[3].setName(HEADER_ELEMENT_MSGBOX);
    // configurations[3].setValue(XBayaConstants.DEFAULT_MESSAGE_BOX_URL.toString());
    //
    // configurations[4] = new NameValue();
    // configurations[4].setName(HEADER_ELEMENT_DSC);
    // configurations[4].setValue(XBayaConstants.DEFAULT_DSC_URL.toString());
    //
    // configurations[5] = new NameValue();
    // configurations[5].setName(HEADER_ELEMENT_BROKER);
    // configurations[5].setValue(XBayaConstants.DEFAULT_BROKER_URL.toString());
    //
    // LinkedList<NameValue> nameValPairsList = new LinkedList<NameValue>();
    // List<InputNode> wfInputs = new ODEClient().getInputNodes(workflow);
    // for (InputNode node : wfInputs) {
    // NameValue nameValue = new NameValue();
    // nameValue.setName(node.getName());
    // nameValue.setValue(node.getDefaultValue().toString());
    // }
    //
    // new WorkflowInterpretorStub(serverURL).launchWorkflow(workflowAsString, topic, password, userName,
    // nameValPairsList.toArray(new NameValue[0]), configurations);
    //
    // }

}