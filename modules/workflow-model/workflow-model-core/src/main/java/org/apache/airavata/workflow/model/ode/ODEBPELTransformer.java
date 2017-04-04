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
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.workflow.model.ode;
//
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.Map;
//
//import javax.xml.namespace.QName;
//
//import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
//import org.apache.airavata.common.utils.StringUtil;
//import org.gpel.model.GpelActivity;
//import org.gpel.model.GpelAssign;
//import org.gpel.model.GpelAssignCopy;
//import org.gpel.model.GpelAssignCopyFrom;
//import org.gpel.model.GpelAssignCopyTo;
//import org.gpel.model.GpelElse;
//import org.gpel.model.GpelElseIf;
//import org.gpel.model.GpelFlow;
//import org.gpel.model.GpelIf;
//import org.gpel.model.GpelInvoke;
//import org.gpel.model.GpelProcess;
//import org.gpel.model.GpelReceive;
//import org.gpel.model.GpelReply;
//import org.gpel.model.GpelSequence;
//import org.gpel.model.GpelVariable;
//import org.gpel.model.GpelVariablesContainer;
//import org.xmlpull.infoset.XmlElement;
//import org.xmlpull.infoset.XmlNamespace;
//
//import xsul5.XmlConstants;
//import xsul5.wsdl.WsdlDefinitions;
//import xsul5.wsdl.WsdlMessage;
//import xsul5.wsdl.WsdlMessagePart;
//import xsul5.wsdl.WsdlPortType;
//import xsul5.wsdl.WsdlPortTypeInput;
//import xsul5.wsdl.WsdlPortTypeOperation;
//
//public class ODEBPELTransformer {
//
//    /**
//     * KEEP_SRC_ELEMENT_NAME_STR
//     */
//    private static final String KEEP_SRC_ELEMENT_NAME_STR = "keepSrcElementName";
//    /**
//     * WORKFLOW_INPUT_STR
//     */
//    private static final String WORKFLOW_INPUT_STR = "WorkflowInput";
//    /**
//     * LEAD_HEADER_STR
//     */
//    private static final String LEAD_HEADER_STR = "leadHeader";
//    /**
//     * NO_STR
//     */
//    private static final String NO_STR = "no";
//    /**
//     * VALIDATE_STR
//     */
//    private static final String VALIDATE_STR = "validate";
//    /**
//     * INPUT_VARIABLE_STR
//     */
//    private static final String INPUT_VARIABLE_STR = "inputVariable";
//    /**
//     * VARIABLE_STR
//     */
//    private static final String VARIABLE_STR = "variable";
//    /**
//     * PART_STR
//     */
//    private static final String PART_STR = "part";
//    /**
//     * OPERATION_STR
//     */
//    private static final String OPERATION_STR = "operation";
//    /**
//     * PORT_TYPE_STR
//     */
//    private static final String PORT_TYPE_STR = "portType";
//    /**
//     * QUERY_STR
//     */
//    private static final String QUERY_STR = "query";
//    /**
//     * YES_STR
//     */
//    private static final String YES_STR = "yes";
//    /**
//     * CREATE_INSTANCE_STR
//     */
//    private static final String CREATE_INSTANCE_STR = "createInstance";
//    /**
//     * NAME
//     */
//    private static final String NAME = "name";
//
//    public ODEBPELTransformer() {
//    }
//
//    public void generateODEBPEL(GpelProcess gpelProcess, String workflowName, WsdlDefinitions workflowWSDL,
//            Map<String, WsdlDefinitions> wsdls) {
//
//        XmlElement bpelXml = gpelProcess.xml();
//        if (null != bpelXml.attributeValue(NAME)) {
//            // already done
//            return;
//        }
//
//        gpelProcess.xml().setAttributeValue(NAME, StringUtil.convertToJavaIdentifier(workflowName));
//        GpelActivity activity = gpelProcess.getActivity();
//
//        addImports(gpelProcess, workflowWSDL, wsdls);
//
//        if (activity instanceof GpelSequence) {
//
//            LinkedList<GpelAssign> result = new LinkedList<GpelAssign>();
//
//            formatXpathFromValueCopy(((GpelSequence) activity).activities().iterator());
//            evaluateFlowAndSequenceForAddingInits(wsdls, workflowWSDL, ((GpelSequence) activity).activities()
//                    .iterator(), result);
//            addVariableManipulationBeforeInvoke(((GpelSequence) activity).activities().iterator());
//            findReplaceAssign(((GpelSequence) activity).activities().iterator());
//            Iterator<GpelActivity> iterator = ((GpelSequence) activity).activities().iterator();
//            while (iterator.hasNext()) {
//                GpelActivity next = iterator.next();
//                if (next instanceof GpelReceive) {
//                    ((GpelReceive) next).xml().setAttributeValue(CREATE_INSTANCE_STR, YES_STR);
//                    break;
//                }
//            }
//
//            for (GpelAssign gpelAssignCopy : result) {
//                // second element because the receive would be the first element
//                activity.xml().addChild(1, gpelAssignCopy.xml());
//            }
//        }
//        LinkedList<GpelInvoke> invokeList = new LinkedList<GpelInvoke>();
//
//        replaceVariableMessageTypeValuesWithMessageNameInsteadOfPortType(gpelProcess, wsdls, activity, invokeList);
//
//    }
//
//    private void replaceVariableMessageTypeValuesWithMessageNameInsteadOfPortType(GpelProcess gpelProcess,
//            Map<String, WsdlDefinitions> wsdls, GpelActivity activity, LinkedList<GpelInvoke> invokeList) {
//        if (isSequence(activity)) {
//            findInvokes(activity, invokeList);
//            GpelVariablesContainer variables = gpelProcess.getVariables();
//            for (GpelInvoke gpelInvoke : invokeList) {
//                String variable = gpelInvoke.getInputVariableName();
//                String opName = gpelInvoke.getOperationNcName();
//                QName portType = gpelInvoke.getPortTypeQName();
//                GpelVariable gpelVar = findVariable(variables, variable);
//                QName messageQname = findMessage(gpelProcess, portType, opName, true, wsdls);
//                String nsPrefix = findNamespacePrefix(gpelProcess, messageQname);
//                gpelVar.setMessageTypeQName(new QName(messageQname.getNamespaceURI(), messageQname.getLocalPart(),
//                        nsPrefix));
//
//                variable = gpelInvoke.gelOutputVariableName();
//                gpelVar = findVariable(variables, variable);
//                messageQname = findMessage(gpelProcess, portType, opName, false, wsdls);
//                nsPrefix = findNamespacePrefix(gpelProcess, messageQname);
//                gpelVar.setMessageTypeQName(new QName(messageQname.getNamespaceURI(), messageQname.getLocalPart(),
//                        nsPrefix));
//            }
//
//        }
//    }
//
//    /**
//     * @param gpelProcess
//     * @param messageQname
//     * @return
//     */
//    private String findNamespacePrefix(GpelProcess gpelProcess, QName messageQname) {
//        Iterator<XmlNamespace> iterator = gpelProcess.xml().namespaces().iterator();
//        while (iterator.hasNext()) {
//            XmlNamespace xmlNamespace = (XmlNamespace) iterator.next();
//            if (xmlNamespace.getName().equals(messageQname.getNamespaceURI())) {
//                return xmlNamespace.getPrefix();
//            }
//        }
//
//        throw new WorkflowRuntimeException("Cannot locate the Namespace  for Qname:" + messageQname + " in the BPEL");
//    }
//
//    /**
//     * @param portType
//     * @param opName
//     * @param b
//     */
//    private QName findMessage(GpelProcess gpelProcess, QName portType, String opName, boolean input,
//            Map<String, WsdlDefinitions> wsdls) {
//        Iterator<String> iterator = wsdls.keySet().iterator();
//        while (iterator.hasNext()) {
//            String key = (String) iterator.next();
//            WsdlDefinitions wsdlDefinitions = wsdls.get(key);
//            WsdlPortType pType = wsdlDefinitions.getPortType(portType.getLocalPart());
//            WsdlPortTypeOperation operation = null;
//            if (null != pType && null != (operation = pType.getOperation(opName))) {
//
//                if (input) {
//                    WsdlPortTypeInput messageRef = operation.getInput();
//                    if (null != messageRef && null != messageRef.getMessage()) {
//                        WsdlMessage message = wsdlDefinitions.getMessage(messageRef.getMessage().getLocalPart());
//                        if (null != message) {
//                            return new QName(wsdlDefinitions.getTargetNamespace(), message.getName(), key);
//                        }
//                    }
//                } else {
//                    xsul5.wsdl.WsdlPortTypeOutput messageRef = operation.getOutput();
//                    if (null != messageRef && null != messageRef.getMessage()) {
//                        WsdlMessage message = wsdlDefinitions.getMessage(messageRef.getMessage().getLocalPart());
//                        if (null != message) {
//                            return new QName(wsdlDefinitions.getTargetNamespace(), message.getName(), key);
//                        }
//                    }
//                }
//
//            }
//        }
//        throw new WorkflowRuntimeException("Unable to find the Message for the PortType " + portType + " operation:"
//                + opName);
//    }
//
//    private GpelVariable findVariable(GpelVariablesContainer variables, String variable) {
//        Iterator<GpelVariable> iterator = variables.variables().iterator();
//
//        while (iterator.hasNext()) {
//            GpelVariable gpelVariable = iterator.next();
//            if (variable.equals(gpelVariable.getName())) {
//                return gpelVariable;
//            }
//        }
//        throw new WorkflowRuntimeException("Unable to fine the variable :" + variable + "  in the BPEL variables "
//                + variables);
//    }
//
//    /**
//     * @param invokeList
//     */
//    private void findInvokes(GpelActivity activity, LinkedList<GpelInvoke> invokeList) {
//        if (isFlow(activity)) {
//            Iterator<GpelActivity> iterator = ((GpelFlow) activity).activities().iterator();
//            findInvokes(iterator, invokeList);
//
//        } else if (activity instanceof GpelSequence) {
//            Iterator<GpelActivity> iterator = ((GpelSequence) activity).activities().iterator();
//            findInvokes(iterator, invokeList);
//        } else if (activity instanceof GpelIf) {
//            Iterator<GpelActivity> iterator = ((GpelIf) activity).activities().iterator();
//            findInvokes(iterator, invokeList);
//            iterator = ((GpelIf) activity).getElse().activities().iterator();
//            findInvokes(iterator, invokeList);
//        }
//
//    }
//
//    /**
//     * @param invokeList
//     */
//    private void findInvokes(Iterator<GpelActivity> iterator, LinkedList<GpelInvoke> invokeList) {
//        while (iterator.hasNext()) {
//            GpelActivity next = iterator.next();
//            if (isSequence(next) || isFlow(next) || isIf(next)) {
//                findInvokes(next, invokeList);
//            } else if (isInvoke(next)) {
//                invokeList.add((GpelInvoke) next);
//            }
//        }
//    }
//
//    /**
//	 * 
//	 */
//    private void addImports(GpelProcess process, WsdlDefinitions workflowWSDL, Map<String, WsdlDefinitions> wsdls) {
//        Iterator<String> iterator = wsdls.keySet().iterator();
//        while (iterator.hasNext()) {
//            String id = iterator.next();
//            WsdlDefinitions wsdl = wsdls.get(id);
//            XmlElement importElement = process.xml().newElement(process.xml().getNamespace(), "import");
//            importElement.setAttributeValue("importType", "http://schemas.xmlsoap.org/wsdl/");
//            importElement.setAttributeValue("location", wsdl.xml().attributeValue("name") + ".wsdl");
//            importElement.setAttributeValue("namespace", wsdl.getTargetNamespace());
//            process.xml().addChild(0, importElement);
//
//        }
//
//        XmlElement importElement = process.xml().newElement(process.xml().getNamespace(), "import");
//        importElement.setAttributeValue("importType", "http://schemas.xmlsoap.org/wsdl/");
//
//        importElement.setAttributeValue("location", workflowWSDL.xml().attributeValue("name") + ".wsdl");
//        importElement.setAttributeValue("namespace", workflowWSDL.getTargetNamespace());
//        process.xml().addChild(0, importElement);
//
//        process.xml().declareNamespace(
//                XmlConstants.BUILDER.newNamespace("leadcntx",
//                        "http://lead.extreme.indiana.edu/namespaces/2005/10/lead-context-header"));
//
//    }
//
//    private void findReplaceAssign(GpelActivity activity) {
//        if (isFlow(activity)) {
//            Iterator<GpelActivity> iterator = ((GpelFlow) activity).activities().iterator();
//            findReplaceAssign(iterator);
//
//        } else if (activity instanceof GpelSequence) {
//            Iterator<GpelActivity> iterator = ((GpelSequence) activity).activities().iterator();
//            findReplaceAssign(iterator);
//        }
//    }
//
//    private void findReplaceAssign(Iterator<GpelActivity> iterator) {
//        while (iterator.hasNext()) {
//            GpelActivity next = iterator.next();
//            if (isSequence(next) || isFlow(next)) {
//                findReplaceAssign(next);
//            } else if (isAssign(next)) {
//                GpelAssign assign = (GpelAssign) next;
//                Iterator<GpelAssignCopy> itr = assign.copyOperations().iterator();
//                while (itr.hasNext()) {
//                    GpelAssignCopy copy = itr.next();
//                    String query = copy.getFrom().xml().attributeValue(QUERY_STR);
//                    if (query != null) {
//                        copy.getFrom().xml().addElement(copy.getFrom().xml().getNamespace(), QUERY_STR)
//                                .addChild("<![CDATA[" + query + "]]>");
//                    }
//                    query = copy.getTo().xml().attributeValue(QUERY_STR);
//                    if (query != null) {
//                        copy.getTo().xml().addElement(copy.getFrom().xml().getNamespace(), QUERY_STR)
//                                .addChild("<![CDATA[" + query + "]]>");
//                    }
//                }
//            }
//        }
//    }
//
//    private void formatXpathFromValueCopy(GpelActivity activity) {
//        if (isFlow(activity)) {
//            Iterator<GpelActivity> iterator = ((GpelFlow) activity).activities().iterator();
//            formatXpathFromValueCopy(iterator);
//
//        } else if (activity instanceof GpelSequence) {
//            Iterator<GpelActivity> iterator = ((GpelSequence) activity).activities().iterator();
//            formatXpathFromValueCopy(iterator);
//        }
//    }
//
//    private void formatXpathFromValueCopy(Iterator<GpelActivity> iterator) {
//        while (iterator.hasNext()) {
//            GpelActivity next = iterator.next();
//            if (isSequence(next) || isFlow(next)) {
//                formatXpathFromValueCopy(next);
//            } else if (isAssign(next)) {
//                GpelAssign assign = (GpelAssign) next;
//                Iterator<GpelAssignCopy> itr = assign.copyOperations().iterator();
//                while (itr.hasNext()) {
//                    GpelAssignCopy copy = itr.next();
//                    String query = copy.getFrom().xml().attributeValue(QUERY_STR);
//                    XmlElement copyElmt = copy.getFrom().xml();
//                    // remove if attribute is found earlier
//                    if (null != query) {
//                        copyElmt.removeAttribute(copyElmt.attribute(QUERY_STR));
//                        copyElmt.setAttributeValue(QUERY_STR, "/" + extractDataType(query));
//                    }
//
//                    query = copy.getTo().xml().attributeValue(QUERY_STR);
//                    XmlElement toElmt = copy.getTo().xml();
//                    // remove if attribute is found earlier
//                    if (null != query) {
//                        toElmt.removeAttribute(toElmt.attribute(QUERY_STR));
//                        toElmt.setAttributeValue(QUERY_STR, "/" + extractDataType(query));
//                    }
//
//                }
//            }
//        }
//    }
//
//    private void evaluateFlowAndSequenceForAddingInits(Map<String, WsdlDefinitions> wsdls,
//            WsdlDefinitions workflowWSDL, GpelActivity activity, LinkedList<GpelAssign> list) {
//        if (isFlow(activity)) {
//            Iterator<GpelActivity> iterator = ((GpelFlow) activity).activities().iterator();
//            evaluateFlowAndSequenceForAddingInits(wsdls, workflowWSDL, iterator, list);
//
//        } else if (activity instanceof GpelSequence) {
//            Iterator<GpelActivity> iterator = ((GpelSequence) activity).activities().iterator();
//            evaluateFlowAndSequenceForAddingInits(wsdls, workflowWSDL, iterator, list);
//        }
//    }
//
//    private void evaluateFlowAndSequenceForAddingInits(Map<String, WsdlDefinitions> wsdls,
//            WsdlDefinitions workflowWSDL, Iterator<GpelActivity> iterator, LinkedList<GpelAssign> list) {
//        GpelActivity last = null;
//        while (iterator.hasNext()) {
//            GpelActivity next = iterator.next();
//            if (isSequence(next) || isFlow(next)) {
//                evaluateFlowAndSequenceForAddingInits(wsdls, workflowWSDL, next, list);
//            } else if (isInvoke(next) || isReply(next)) {
//                if (last == null || !isAssign(last)) {
//                    throw new WorkflowRuntimeException("Assign activity not found for the Invoke "
//                            + next.xmlStringPretty());
//                }
//
//                GpelAssign assign = (GpelAssign) last;
//                XmlNamespace ns = assign.xml().getNamespace();
//
//                XmlElement container = XmlConstants.BUILDER.parseFragmentFromString("<dummyelement></dummyelement>");
//
//                String portTypeattr = next.xml().attributeValue(PORT_TYPE_STR);
//                String operation = next.xml().attributeValue(OPERATION_STR);
//                if (null == portTypeattr || "".equals(portTypeattr)) {
//                    throw new WorkflowRuntimeException("No Porttype found for Invoke:" + next);
//                }
//                String portTypeName = portTypeattr.substring(portTypeattr.indexOf(':') + 1);
//                String messagePartName = null;
//                if (isInvoke(next)) {
//                    Iterator<String> keys = wsdls.keySet().iterator();
//
//                    while (keys.hasNext()) {
//                        String key = keys.next();
//                        WsdlDefinitions wsdl = wsdls.get(key);
//                        WsdlPortType portType = wsdl.getPortType(portTypeName);
//                        if (null != portType) {
//                            WsdlPortTypeOperation wsdlOperation = portType.getOperation(operation);
//                            WsdlMessagePart part = wsdl
//                                    .getMessage(wsdlOperation.getInput().getMessage().getLocalPart()).parts()
//                                    .iterator().next();
//                            XmlElement childElement = container.addElement(part.getElement().getLocalPart());
//                            Iterator<GpelAssignCopy> copyItr = assign.copyOperations().iterator();
//                            while (copyItr.hasNext()) {
//                                GpelAssignCopy copyItm = copyItr.next();
//                                childElement.addElement(getElementName(copyItm.getTo().getQuery()));
//                                if (messagePartName == null) {
//                                    messagePartName = copyItm.getTo().xml().attributeValue(PART_STR);
//                                }
//                            }
//                            break;
//                        }
//                    }
//                } else {
//                    // reply
//
//                    WsdlPortType portType = workflowWSDL.getPortType(portTypeName);
//                    if (null != portType) {
//                        WsdlPortTypeOperation wsdlOperation = portType.getOperation(operation);
//                        WsdlMessagePart part = workflowWSDL
//                                .getMessage(wsdlOperation.getOutput().getMessage().getLocalPart()).parts().iterator()
//                                .next();
//                        XmlElement childElement = container.addElement(part.getElement().getLocalPart());
//                        Iterator<GpelAssignCopy> copyItr = assign.copyOperations().iterator();
//                        while (copyItr.hasNext()) {
//                            GpelAssignCopy copyItm = copyItr.next();
//                            childElement.addElement(getElementName(copyItm.getTo().getQuery()));
//                            if (messagePartName == null) {
//                                messagePartName = copyItm.getTo().xml().attributeValue(PART_STR);
//                            }
//                        }
//                    }
//                }
//
//                GpelAssignCopyFrom from = new GpelAssignCopyFrom(ns);
//                from.setLiteral(container);
//
//                GpelAssignCopyTo to = new GpelAssignCopyTo(ns);
//                to.xml().setAttributeValue(PART_STR, messagePartName);
//                if (isInvoke(next)) {
//                    to.xml().setAttributeValue(VARIABLE_STR, next.xml().attributeValue(INPUT_VARIABLE_STR));
//                } else {
//                    to.xml().setAttributeValue(VARIABLE_STR, next.xml().attributeValue(VARIABLE_STR));
//                }
//                GpelAssignCopy newAssign = new GpelAssignCopy(ns, from, to);
//                newAssign.xml().setAttributeValue(VALIDATE_STR, NO_STR);
//                GpelAssign gpelAssign = new GpelAssign(ns, newAssign);
//                list.add(gpelAssign);
//
//            }
//            last = next;
//        }
//    }
//
//    /**
//     * @param query
//     * @return
//     */
//    private String getElementName(String query) {
//        int index = query.indexOf('/');
//        if (-1 != index) {
//            return query.substring(index + 1);
//        }
//        return query;
//    }
//
//    private void addVariableManipulationBeforeInvoke(GpelActivity activity) {
//
//        if (isFlow(activity)) {
//            Iterator<GpelActivity> iterator = ((GpelFlow) activity).activities().iterator();
//            addVariableManipulationBeforeInvoke(iterator);
//
//        } else if (activity instanceof GpelSequence) {
//            Iterator<GpelActivity> iterator = ((GpelSequence) activity).activities().iterator();
//            addVariableManipulationBeforeInvoke(iterator);
//        }
//        // else do nothing
//
//    }
//
//    private void addVariableManipulationBeforeInvoke(Iterator<GpelActivity> iterator) {
//        GpelActivity last = null;
//        while (iterator.hasNext()) {
//            GpelActivity next = iterator.next();
//            if (isSequence(next) || isFlow(next)) {
//                addVariableManipulationBeforeInvoke(next);
//            } else if (isInvoke(next)) {
//                if (last == null || !isAssign(last)) {
//                    throw new WorkflowRuntimeException("Assign activity not found for the Invoke" + next.xmlStringPretty());
//                }
//
//                // we are good and should add the header copy.
//                XmlNamespace ns = last.xml().getNamespace();
//                GpelAssignCopyFrom headerFrom = new GpelAssignCopyFrom(ns);
//                headerFrom.xml().setAttributeValue(PART_STR, LEAD_HEADER_STR);
//                headerFrom.xml().setAttributeValue(VARIABLE_STR, WORKFLOW_INPUT_STR);
//
//                GpelAssignCopyTo headerTo = new GpelAssignCopyTo(ns);
//                headerTo.xml().setAttributeValue(PART_STR, LEAD_HEADER_STR);
//                headerTo.xml().setAttributeValue(VARIABLE_STR, next.xml().attributeValue(INPUT_VARIABLE_STR));
//                GpelAssignCopy headerCopy = new GpelAssignCopy(ns, headerFrom, headerTo);
//                GpelAssign assign = (GpelAssign) last;
//                assign.addCopy(headerCopy);
//
//                GpelAssignCopyFrom nodeIDFrom = new GpelAssignCopyFrom(ns);
//                nodeIDFrom.setLiteral(XmlConstants.BUILDER.parseFragmentFromString("<dummyelement>"
//                        + next.xml().attributeValue(NAME) + "</dummyelement>"));
//                GpelAssignCopyTo nodeIDTo = new GpelAssignCopyTo(ns);
//
//                nodeIDTo.xml().setAttributeValue(PART_STR, LEAD_HEADER_STR);
//                nodeIDTo.xml().setAttributeValue(VARIABLE_STR, next.xml().attributeValue(INPUT_VARIABLE_STR));
//                // TODO is this ok?? what of the query language
//                nodeIDTo.setQuery("/leadcntx:workflow-node-id");
//
//                GpelAssignCopy nodeIDCopy = new GpelAssignCopy(ns, nodeIDFrom, nodeIDTo);
//                nodeIDCopy.xml().setAttributeValue(KEEP_SRC_ELEMENT_NAME_STR, NO_STR);
//                assign.addCopy(nodeIDCopy);
//
//                GpelAssignCopyFrom timeStepFrom = new GpelAssignCopyFrom(ns);
//                timeStepFrom.setLiteral(XmlConstants.BUILDER.parseFragmentFromString("<dummyelement>" + "5"
//                        + "</dummyelement>"));
//                GpelAssignCopyTo timeStepTo = new GpelAssignCopyTo(ns);
//                timeStepTo.xml().setAttributeValue(PART_STR, LEAD_HEADER_STR);
//                timeStepTo.xml().setAttributeValue(VARIABLE_STR, next.xml().attributeValue(INPUT_VARIABLE_STR));
//                // TODO is this ok?? what of the query language
//                timeStepTo.setQuery("/leadcntx:workflow-time-step");
//
//                GpelAssignCopy timeStepCopy = new GpelAssignCopy(ns, timeStepFrom, timeStepTo);
//                timeStepCopy.xml().setAttributeValue(KEEP_SRC_ELEMENT_NAME_STR, NO_STR);
//                assign.addCopy(timeStepCopy);
//
//            }
//            last = next;
//        }
//    }
//
//    /**
//     * @param query
//     */
//    private String extractDataType(String query) {
//        int index = query.indexOf(':');
//        if (index == -1) {
//            throw new WorkflowRuntimeException("Invalid query no : delimeter found " + query);
//        }
//        String[] split = query.substring(index + 1).trim().split("/");
//        if (split.length == 0) {
//            throw new WorkflowRuntimeException("Unknown Xpath " + query.substring(index));
//        }
//        return split[split.length - 1];
//    }
//
//    private boolean isSequence(GpelActivity activity) {
//        return activity instanceof GpelSequence;
//    }
//
//    private boolean isFlow(GpelActivity activity) {
//        return activity instanceof GpelFlow;
//    }
//
//    private boolean isAssign(GpelActivity activity) {
//        return activity instanceof GpelAssign;
//    }
//
//    private boolean isInvoke(GpelActivity activity) {
//        return activity instanceof GpelInvoke;
//    }
//
//    private boolean isReply(GpelActivity activity) {
//        return activity instanceof GpelReply;
//    }
//
//    private boolean isIf(GpelActivity activity) {
//        return activity instanceof GpelIf;
//    }
//
//    private boolean isElse(GpelActivity activity) {
//        return activity instanceof GpelElse;
//    }
//
//    private boolean isElseIf(GpelActivity activity) {
//        return activity instanceof GpelElseIf;
//    }
//}