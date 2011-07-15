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

package org.apache.airavata.xbaya.streaming;

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBaya;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.ws.WSNode;
import org.apache.airavata.xbaya.util.WSDLUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.gpel.model.GpelProcess;
import org.gpel.model.GpelVariable;
import org.gpel.model.GpelVariablesContainer;
//import org.python.modules.newmodule;
import org.xmlpull.infoset.XmlElement;
import org.xmlpull.infoset.XmlInfosetBuilder;
import org.xmlpull.infoset.XmlNamespace;
import org.xmlpull.infoset.view.XmlValidationException;

import xsul5.XmlConstants;
import xsul5.wsdl.WsdlBinding;
import xsul5.wsdl.WsdlBindingOperation;
import xsul5.wsdl.WsdlDefinitions;
import xsul5.wsdl.WsdlMessage;
import xsul5.wsdl.WsdlMessagePart;
import xsul5.wsdl.WsdlPortType;
import xsul5.wsdl.WsdlPortTypeOperation;

/**
 * @author Chathura Herath
 */
public class StreamTransformer {

    /**
     * BUILDER
     */
    private static final XmlInfosetBuilder BUILDER = XmlConstants.BUILDER;

    public void removeReply(GpelProcess process) {

        XmlElement sequence = process.xml().element("sequence");
        if (null != sequence) {
            XmlElement reply = sequence.element("reply");
            sequence.removeChild(reply);
        }

    }

    public void removeOutputMessageReferences(WsdlDefinitions definition) {
        Iterable<WsdlPortType> portTypes = definition.portTypes();
        for (WsdlPortType portType : portTypes) {
            Iterable<WsdlPortTypeOperation> operations = portType.operations();
            for (WsdlPortTypeOperation wsdlPortTypeOperation : operations) {
                wsdlPortTypeOperation.xml().removeChild(wsdlPortTypeOperation.getOutput().xml());
            }
        }

        Iterable<WsdlBinding> bindings = definition.bindings();
        for (WsdlBinding wsdlBinding : bindings) {
            Iterable<WsdlBindingOperation> operations = wsdlBinding.operations();
            for (WsdlBindingOperation wsdlBindingOperation : operations) {
                XmlElement output = wsdlBindingOperation.xml().element("output");
                wsdlBindingOperation.xml().removeChild(output);
            }
        }
    }

    /**
     * 
     * @param process
     * @param workflowWsdl
     * @param operationName
     * @param receiveMessage
     * @throws CloneNotSupportedException
     */
    public void addreceive(GpelProcess process, WsdlDefinitions workflowWsdl, String operationName,
            String receiveMessage) throws CloneNotSupportedException {

        GpelVariablesContainer variables = process.getVariables();
        XmlNamespace ns = variables.xml().getNamespace();
        GpelVariable var = new GpelVariable(ns, "newReceiveVar");
        variables.addVariable(var);
        var.setMessageTypeQName(new QName(workflowWsdl.getTargetNamespace(), receiveMessage));

        XmlElement topSeq = process.getActivity().xml();
        Iterator iterator = topSeq.children().iterator();
        int count = 0;

        XmlElement firstReceive = null;
        XmlElement invoke = null;

        int invokeCount = 0;
        while (iterator.hasNext()) {
            XmlElement object = (XmlElement) iterator.next();

            if (object.getName().equals("receive")) {
                firstReceive = object;
            } else if (object.getName().equals("invoke")) {

                if (invokeCount < XBaya.preservice + 1) {
                } else {
                    invoke = object;
                    break;
                }
                invokeCount++;
            }
            ++count;
        }

        XmlElement receive = BUILDER.newFragment(topSeq.getNamespace(), "receive");
        receive.setAttributeValue("partnerLink", "workflowUserPartner");
        receive.setAttributeValue("portType", firstReceive.attributeValue("portType"));
        receive.setAttributeValue("operation", operationName);
        receive.setAttributeValue("variable", "newReceiveVar");
        topSeq.removeChild(invoke);

        XmlElement whileElmt = BUILDER.newFragment(topSeq.getNamespace(), "while");
        whileElmt.setAttributeValue("name", "receiveLoop");
        XmlElement condition = whileElmt.addElement(topSeq.getNamespace(), "condition");
        condition.addChild("true()");
        XmlElement whileSeq = whileElmt.addElement(topSeq.getNamespace(), "sequence");
        whileSeq.setAttributeValue("name", "whileSeq");

        // copy element
        XmlElement assign = topSeq.element(count - 1).clone();
        String assignString = BUILDER.serializeToStringPretty(assign);
        assignString = assignString.replaceAll("WorkflowInput", "newReceiveVar");
        int start = assignString.indexOf("variable=\"") + "variable=\"".length();
        int end = assignString.indexOf("\"", start);
        String oldVar = assignString.substring(start, end);
        assignString = assignString.replace(oldVar, "newReceiveVar");
        // TODO generalize
        // TODO
        // FIXME
        assignString = assignString.replace("parameters", "input");
        assignString = assignString.replaceAll("return", "input");
        assign = BUILDER.parseFragmentFromString(assignString);

        whileSeq.addChild(receive);

        whileSeq.addChild(assign);

        whileSeq.addChild(invoke);

        topSeq.addChild(count, whileElmt);

        count = 0;
        Iterator children = process.xml().children().iterator();
        while (children.hasNext()) {
            Object object = children.next();
            if (object instanceof XmlElement) {
                if (((XmlElement) object).getName().equals("sequence")) {
                    break;
                }
                ++count;
            }
        }

        addCorelationToBPEL(process, topSeq, firstReceive, receive);

    }

    public static void main(String[] args) {

        String assignString = "<bpel:assign>"
                + "      <bpel:copy>"
                + "        <bpel:from variable=\"SleepPortType_sleepOutput\" part=\"parameters\" query=\"/return\">"
                + "          <bpel:query>&lt;![CDATA[/return]]&gt;</bpel:query>"
                + "        </bpel:from>"
                + "        <bpel:to variable=\"EchoLogStream_RunInput\" part=\"input\" query=\"/input\">"
                + "          <bpel:query>&lt;![CDATA[/input]]&gt;</bpel:query>"
                + "        </bpel:to>"
                + "      </bpel:copy>"
                + "      <bpel:copy>"
                + "        <bpel:from part=\"leadHeader\" variable=\"WorkflowInput\" />"
                + "        <bpel:to part=\"leadHeader\" variable=\"EchoLogStream_RunInput\" />"
                + "      </bpel:copy>"
                + "      <bpel:copy keepSrcElementName=\"no\">"
                + "        <bpel:from>"
                + "          <bpel:literal>EchoLogStream_Run</bpel:literal>"
                + "        </bpel:from>"
                + "        <bpel:to part=\"leadHeader\" variable=\"EchoLogStream_RunInput\" query=\"/leadcntx:workflow-node-id\">"
                + "          <bpel:query>&lt;![CDATA[/leadcntx:workflow-node-id]]&gt;</bpel:query>"
                + "        </bpel:to>"
                + "      </bpel:copy>"
                + "      <bpel:copy keepSrcElementName=\"no\">"
                + "        <bpel:from>"
                + "          <bpel:literal>5</bpel:literal>"
                + "        </bpel:from>"
                + "        <bpel:to part=\"leadHeader\" variable=\"EchoLogStream_RunInput\" query=\"/leadcntx:workflow-time-step\">"
                + "          <bpel:query>&lt;![CDATA[/leadcntx:workflow-time-step]]&gt;</bpel:query>"
                + "        </bpel:to>" + "     </bpel:copy>" + "    </bpel:assign>";
        assignString = assignString.replaceAll("WorkflowInput", "newReceiveVar");
        int start = assignString.indexOf("variable=\"") + "variable=\"".length();
        int end = assignString.indexOf("\"", start);

        String oldVar = assignString.substring(start, end);
        assignString = assignString.replace(oldVar, "newReceiveVar");

        assignString = assignString.replace("parameters", "input");
        assignString = assignString.replaceAll("return", "input");

        System.out.println(assignString);
    }

    /**
     * @param process
     * @param topSeq
     * @param firstReceive
     * @param receive
     */
    private void addCorelationToBPEL(GpelProcess process, XmlElement topSeq, XmlElement firstReceive, XmlElement receive) {
        XmlElement correlationSets = BUILDER.newFragment(topSeq.getNamespace(), "correlationSets");
        XmlElement correlationSet = correlationSets.addElement(topSeq.getNamespace(), "correlationSet");
        correlationSet.setAttributeValue("name", "sessionCorrelation");
        correlationSet.setAttributeValue("properties", "tns:experiment-id");

        Iterable bpelElements = process.xml().children();
        int count = 0;
        for (Object object : bpelElements) {
            if (object instanceof XmlElement && "sequence".equals(((XmlElement) object).getName())) {
                break;
            }
            ++count;
        }
        process.xml().addElement(count, correlationSets);

        XmlElement correlations = firstReceive.addElement(topSeq.getNamespace(), "correlations");
        XmlElement correlation = correlations.addElement(topSeq.getNamespace(), "correlation");
        correlation.setAttributeValue("set", "sessionCorrelation");
        correlation.setAttributeValue("initiate", "yes");
        correlation.setAttributeValue("pattern", "response");

        correlations = receive.addElement(topSeq.getNamespace(), "correlations");
        correlation = correlations.addElement(topSeq.getNamespace(), "correlation");
        correlation.setAttributeValue("set", "sessionCorrelation");
        correlation.setAttributeValue("initiate", "no");
        correlation.setAttributeValue("pattern", "response");
    }

    @SuppressWarnings("null")
    public void addReceive(WsdlDefinitions wsdl, Node activeNode, Workflow workflow) throws CloneNotSupportedException,
            XmlValidationException, GraphException, ComponentException {
        XmlElement types = wsdl.getTypes();

        Iterator<XmlElement> schemas = types.elements(null, "schema").iterator();
        while (schemas.hasNext()) {
            schemas.next();
        }

        WSNode node = (WSNode) activeNode;
        WsdlDefinitions serviceWSDL = workflow.getWSDLs().get(node.getWSDLID());
        XmlElement serviceSchema = serviceWSDL.getTypes().element(null, "schema");
        String serviceTns = serviceSchema.attributeValue("targetNamespace");

        String nsPrefix = null;
        String oldNSPrefix = null;
        boolean introduceNewNS = false;
        String operationName = node.getOperationName();
        WsdlMessage newInputMessage = null;
        Iterable<WsdlPortType> servicePortTypes = serviceWSDL.portTypes();
        WsdlPortTypeOperation serviceOperation = null;
        WsdlPortType servicePortType = null;
        for (WsdlPortType wsdlPortType : servicePortTypes) {

            serviceOperation = wsdlPortType.getOperation(operationName);
            if (serviceOperation != null) {
                QName inputMessageName = serviceOperation.getInput().getMessage();
                WsdlMessagePart part = WSDLUtil.getfirst(serviceWSDL.getMessage(inputMessageName.getLocalPart())
                        .parts());

                nsPrefix = part.getElement().getPrefix();
                oldNSPrefix = nsPrefix;
                introduceNewNS = false;
                while (null != wsdl.xml().lookupNamespaceByPrefix(nsPrefix)) {
                    // this namespace is already there so keep adding control at the end and check for
                    // its existance
                    nsPrefix += "_control";
                    introduceNewNS = true;
                }

                wsdl.xml().declareNamespace(BUILDER.newNamespace(nsPrefix, serviceTns));

                WsdlMessage inputMessage = serviceWSDL.getMessage(inputMessageName.getLocalPart());
                newInputMessage = wsdl.addMessage(inputMessageName.getLocalPart());
                Iterable<WsdlMessagePart> parts = inputMessage.parts();
                for (WsdlMessagePart wsdlMessagePart : parts) {
                    XmlElement newPart = null;
                    if (introduceNewNS) {
                        XmlElement clone = wsdlMessagePart.xml().clone();
                        String xmlAsString = BUILDER.serializeToString(clone).replaceAll(oldNSPrefix + ":",
                                nsPrefix + ":");
                        newPart = BUILDER.parseFragmentFromString(xmlAsString);
                    } else {
                        newPart = wsdlMessagePart.xml().clone();
                    }
                    newInputMessage.xml().addElement(newPart);
                }
                // add lead header part to the new input messag because this message is copied from the service wsdl.

                XmlElement newLeadHeaderPart = newInputMessage.xml().addElement(newInputMessage.xml().getNamespace(),
                        "part");
                newLeadHeaderPart.setAttributeValue("name", "leadHeader");
                newLeadHeaderPart.setAttributeValue("element", "lc:context");

                WsdlMessage newOutputMessage = null;
                if (null != serviceOperation.getOutput()) {
                    QName outputMessageName = serviceOperation.getOutput().getMessage();
                    WsdlMessage outputMessage = serviceWSDL.getMessage(outputMessageName.getLocalPart());
                    newOutputMessage = wsdl.addMessage(outputMessageName.getLocalPart());
                    Iterable<WsdlMessagePart> parts2 = outputMessage.parts();
                    for (WsdlMessagePart wsdlMessagePart : parts2) {
                        XmlElement newPart = null;
                        if (introduceNewNS) {
                            XmlElement clone = wsdlMessagePart.xml().clone();
                            String xmlAsString = BUILDER.serializeToString(clone).replaceAll(oldNSPrefix + ":",
                                    nsPrefix + ":");
                            newPart = BUILDER.parseFragmentFromString(xmlAsString);
                        } else {
                            newPart = wsdlMessagePart.xml().clone();
                        }
                        newOutputMessage.xml().addElement(newPart);
                    }
                }

                if (introduceNewNS) {
                    List<XmlNamespace> namespacesPresentInSchema = WSDLUtil.getNamespaces(serviceSchema);
                    WSDLUtil.print(serviceSchema);
                    for (XmlNamespace shouldBeDefinedNamespaces : namespacesPresentInSchema) {
                        if (shouldBeDefinedNamespaces.getPrefix() != null
                                && null == wsdl.xml().lookupNamespaceByPrefix(shouldBeDefinedNamespaces.getPrefix())) {
                            wsdl.xml().declareNamespace(shouldBeDefinedNamespaces);
                        }

                    }
                    XmlElement clone = serviceSchema.clone();
                    String xmlAsString = BUILDER.serializeToString(clone).replaceAll(oldNSPrefix + ":", nsPrefix + ":");
                    XmlElement newSchema = BUILDER.parseFragmentFromString(xmlAsString);
                    types.addChild(newSchema);

                    XmlElement crosscutImport = BUILDER.newFragment(newSchema.getNamespace(), "import");
                    crosscutImport.setAttributeValue("namespace",
                            "http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/");
                    crosscutImport.setAttributeValue("schemaLocation", "lead-crosscut-parameters.xsd");
                    newSchema.addChild(0, crosscutImport);
                } else {
                    types.addChild(serviceSchema.clone());
                }
                // adding new operation for the receive
                Iterator<WsdlPortType> portTypes = wsdl.portTypes().iterator();
                if (portTypes.hasNext()) {
                    WsdlPortType newOperationPortType = portTypes.next();
                    WsdlPortTypeOperation newOp = newOperationPortType.addOperation(node.getOperationName());
                    XmlNamespace wsaNS = BUILDER.newNamespace("http://www.w3.org/2006/05/addressing/wsdl");

                    newOp.setInput(newInputMessage.getName(), newInputMessage);
                    String inputAction = serviceOperation.getInput().xml().attributeValue(wsaNS, "Action");
                    newInputMessage.xml().setAttributeValue(wsaNS, "Action", inputAction);

                    if (newOutputMessage != null) {
                        newOp.setOutput(newOutputMessage.getName(), newOutputMessage);
                        String outputAction = serviceOperation.getOutput().xml().attributeValue(wsaNS, "Action");
                        newOutputMessage.xml().setAttributeValue(wsaNS, "Action", outputAction);
                    }
                }
                servicePortType = wsdlPortType;
            }

            addreceive(workflow.getOdeProcess(null, null), wsdl, operationName, newInputMessage.getName());
        }

        // find the binding for the operation in the service wsdl
        Iterable<WsdlBinding> serviceBindings = serviceWSDL.bindings();
        WsdlBinding serviceBinding = null;
        for (WsdlBinding wsdlBinding : serviceBindings) {
            if (wsdlBinding.getPortType().equals(servicePortType.getQName())) {
                serviceBinding = wsdlBinding;
                break;
            }
        }
        WsdlBindingOperation serviceBindingOperation = serviceBinding.getOperation(node.getOperationName());

        // find the binding in the final wsdl
        Iterator<WsdlPortType> portTypeItr = wsdl.portTypes().iterator();
        if (portTypeItr.hasNext()) {
            WsdlPortType portType = portTypeItr.next();
            Iterable<WsdlBinding> bindings = wsdl.bindings();
            for (WsdlBinding wsdlBinding : bindings) {
                if (wsdlBinding.getPortType().equals(portType.getQName())) {
                    WsdlBindingOperation newBindingOperation = wsdlBinding.addOperation(node.getOperationName());
                    Iterable serviceBindingChildren = serviceBindingOperation.xml().children();
                    for (Object object : serviceBindingChildren) {
                        if (object instanceof XmlElement) {
                            XmlElement newBindingOperationChild = ((XmlElement) object).clone();
                            newBindingOperation.xml().addElement(newBindingOperationChild);

                            // if this is the input element add a header binding because that is missing in the
                            // wsdl that this was copied from
                            if ("input".equals(newBindingOperationChild.getName())) {
                                XmlElement bindingBody = newBindingOperationChild.element("body");
                                bindingBody.setAttributeValue("parts", "input");
                                XmlNamespace soapNS = XmlConstants.BUILDER
                                        .newNamespace("http://schemas.xmlsoap.org/wsdl/soap/");
                                XmlElement bindingHeader = newBindingOperationChild.addElement(soapNS, "header");
                                bindingHeader.setAttributeValue("part", "leadHeader");
                                bindingHeader.setAttributeValue("use", "literal");
                                bindingHeader.setAttributeValue("message", "tns:" + newInputMessage.getName());

                            }

                        }
                    }
                    break;
                }
            }
        }

        WsdlPortType portType = WSDLUtil.getfirst(wsdl.portTypes());
        // Thinking that the first operation is the the one that is workflow input
        QName workflowInputMessage = WSDLUtil.getfirst(portType.operations()).getInput().getMessage();
        addCorrelationProperties(wsdl, wsdl.getMessage(workflowInputMessage.getLocalPart()), newInputMessage);

    }

    private void addCorrelationProperties(WsdlDefinitions wsdl, WsdlMessage workflowInputMessage,
            WsdlMessage secondReceiveMessage) {
        XmlNamespace propns = BUILDER.newNamespace("http://docs.oasis-open.org/wsbpel/2.0/varprop");

        String tnsPrefix = wsdl.xml().lookupNamespaceByName(wsdl.getTargetNamespace()).getPrefix();

        XmlElement property = wsdl.xml().addElement(propns, "property");
        property.setAttributeValue("name", "experiment-id");
        property.setAttributeValue("type", "xsd:string");

        XmlElement propertyAlias = wsdl.xml().addElement(propns, "propertyAlias ");
        propertyAlias.setAttributeValue("propertyName", "tns:experiment-id");

        XmlNamespace bpelNS = BUILDER.newNamespace("bpel", "http://docs.oasis-open.org/wsbpel/2.0/process/executable");
        propertyAlias.setAttributeValue("messageType", tnsPrefix + ":" + workflowInputMessage.getName());
        propertyAlias.setAttributeValue("part", "leadHeader");
        String corelationXpath = "/lc:experiment-id";
        XmlElement query = propertyAlias.addElement(bpelNS, "query");
        query.addChild(corelationXpath);

        corelationXpath = "/lc:experiment-id";
        propertyAlias = wsdl.xml().addElement(propns, "propertyAlias ");
        propertyAlias.setAttributeValue("propertyName", "tns:experiment-id");
        propertyAlias.setAttributeValue("messageType", tnsPrefix + ":" + secondReceiveMessage.getName());
        propertyAlias.setAttributeValue("part", "leadHeader");
        query = propertyAlias.addElement(bpelNS, "query");
        query.addChild(corelationXpath);
    }

}