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
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.Map;
//import java.util.Set;
//
//import javax.xml.namespace.QName;
//
//import org.apache.airavata.common.utils.StringUtil;
//import org.apache.airavata.common.utils.WSDLUtil;
//import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
//import org.apache.airavata.workflow.model.gpel.DSCUtil;
//import org.apache.airavata.workflow.model.utils.WorkflowConstants;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.xmlpull.infoset.XmlBuilderException;
//import org.xmlpull.infoset.XmlInfosetBuilder;
//import org.xmlpull.infoset.XmlNamespace;
//
//import xsul5.wsdl.WsdlBinding;
//import xsul5.wsdl.WsdlBindingOperation;
//import xsul5.wsdl.WsdlDefinitions;
//import xsul5.wsdl.WsdlMessage;
//import xsul5.wsdl.WsdlMessagePart;
//import xsul5.wsdl.WsdlPort;
//import xsul5.wsdl.WsdlPortType;
//import xsul5.wsdl.WsdlPortTypeOperation;
//import xsul5.wsdl.WsdlService;
//
//public class ODEWSDLTransformer {
//
//    private static final Logger log = LoggerFactory.getLogger(ODEWSDLTransformer.class);
//    /**
//     * SCHEMA_LOCATION_URI
//     */
//    private static final String SCHEMA_LOCATION_URI = "lead-context.xsd";
//    /**
//     * SCHEMA
//     */
//    private static final String SCHEMA = "schema";
//    /**
//     * IMPORT
//     */
//    private static final String IMPORT = "import";
//    /**
//     * NAMESPACE
//     */
//    private static final String NAMESPACE = "namespace";
//    /**
//     * SCHEMA_LOCATION
//     */
//    private static final String SCHEMA_LOCATION = "schemaLocation";
//    /**
//     * LEAD_CONTEXT_HEADER_NS
//     */
//    private static final String LEAD_CONTEXT_HEADER_NS = "http://lead.extreme.indiana.edu/namespaces/2005/10/lead-context-header";
//    /**
//     * LC
//     */
//    private static final String LC = "lc";
//    /**
//     * LC_CONTEXT
//     */
//    private static final String LC_CONTEXT = "lc:context";
//    /**
//     * ELEMENT
//     */
//    private static final String ELEMENT = "element";
//    /**
//     * BODY
//     */
//    private static final String BODY = "body";
//    /**
//     * MESSAGE
//     */
//    private static final String MESSAGE = "message";
//    /**
//     * HEADER
//     */
//    private static final String HEADER = "header";
//    /**
//     * LITERAL
//     */
//    private static final String LITERAL = "literal";
//    /**
//     * USE
//     */
//    private static final String USE = "use";
//    /**
//     * LEAD_HEADER
//     */
//    private static final String LEAD_HEADER = "leadHeader";
//    /**
//     * PART
//     */
//    private static final String PART = "part";
//    /**
//     * INPUT
//     */
//    private static final String INPUT = "input";
//    /**
//     * PARTS
//     */
//    private static final String PARTS = "parts";
//    /**
//     * BUILDER
//     */
//    private static final XmlInfosetBuilder BUILDER = xsul5.XmlConstants.BUILDER;
//
//    /**
//     * Constructs a ODEWSDLTransformer.
//     * 
//     */
//    public ODEWSDLTransformer() {
//
//    }
//
//    /**
//     * @param workflowName
//     * @param dscUrl
//     * @param workflowWsdl
//     * @param wsdls
//     */
//    public void trasnformToODEWsdls(String workflowName, URI dscUrl, WsdlDefinitions workflowWsdl,
//            Map<String, WsdlDefinitions> wsdls) {
//
//        addCrosscutImportsIfNecessary(workflowWsdl);
//        makeWorkflowWSDLConcrete(workflowWsdl, workflowName, dscUrl);
//        changePartnerLinkNS(workflowWsdl);
//        addImportsAndHeaderMessage(workflowWsdl);
//        transformServiceWsdls(wsdls, dscUrl);
//
//    }
//
//    /**
//     * This is a safe or idempotant Operation
//     * 
//     * @param workflowWsdl
//     * @param workflowName
//     */
//    public void makeWorkflowWSDLConcrete(WsdlDefinitions workflowWsdl, String workflowName, URI dscUrl) {
//
//        addBindings(workflowWsdl, dscUrl);
//        setODEAddress(workflowWsdl, workflowName);
//    }
//
//    private void changePartnerLinkNS(WsdlDefinitions workflowWsdl) {
//        org.xmlpull.infoset.XmlElement xml = workflowWsdl.xml();
//        Iterator<XmlNamespace> itr = xml.namespaces().iterator();
//
//        LinkedList<XmlNamespace> namespaces = new LinkedList<XmlNamespace>();
//        while (itr.hasNext()) {
//            XmlNamespace ns = itr.next();
//            if (!"http://schemas.xmlsoap.org/ws/2004/03/partner-link/".equals(ns.getName())) {
//                namespaces.add(ns);
//            }
//        }
//
//        xml.removeAllNamespaceDeclarations();
//
//        for (XmlNamespace xmlNamespace : namespaces) {
//            xml.declareNamespace(xmlNamespace);
//        }
//
//        xml.setAttributeValue("xmlns:plnk", "http://docs.oasis-open.org/wsbpel/2.0/plnktype");
//
//        Iterator<org.xmlpull.infoset.XmlElement> plItr = xml.elements(null, "partnerLinkType").iterator();
//        while (plItr.hasNext()) {
//            org.xmlpull.infoset.XmlElement xmlElement = plItr.next();
//            XmlNamespace plinkNs = BUILDER.newNamespace("http://docs.oasis-open.org/wsbpel/2.0/plnktype");
//            xmlElement.setNamespace(plinkNs);
//            Iterator childItr = xmlElement.children().iterator();
//            while (childItr.hasNext()) {
//                Object object = (Object) childItr.next();
//                if (object instanceof org.xmlpull.infoset.XmlElement) {
//                    ((org.xmlpull.infoset.XmlElement) object).setNamespace(plinkNs);
//                }
//            }
//        }
//    }
//
//    private void transformServiceWsdls(Map<String, WsdlDefinitions> wsdls, URI dscUrl) {
//        Set<String> keys = wsdls.keySet();
//
//        for (String string : keys) {
//            WsdlDefinitions wsdl = wsdls.get(string);
//            // Replacing the gfac xsd remote urls
//            // this was done because avoid network inaccisibilities
//            WSDLUtil.replaceAttributeValue(wsdl.getTypes(), "schemaLocation",
//                    "http://www.extreme.indiana.edu/gfac/gfac-simple-types.xsd", "gfac-simple-types.xsd");
//            addBindings(wsdl, dscUrl);
//            addImportsAndHeaderMessage(wsdl);
//        }
//    }
//
//    private void setODEAddress(WsdlDefinitions workflowWsdl, String workflowName) {
//        Iterator<WsdlService> serviceItr = workflowWsdl.services().iterator();
//        if (serviceItr.hasNext()) {
//            Iterator<WsdlPort> portItr = serviceItr.next().ports().iterator();
//            if (portItr.hasNext()) {
//                org.xmlpull.infoset.XmlElement address = portItr.next().xml().element("address");
//                if (!(WorkflowConstants.DEFAULT_ODE_URL + "/ode/processes/" + StringUtil
//                        .convertToJavaIdentifier(workflowName)).equals(address.attributeValue("location"))) {
//                    address.removeAllAttributes();
//                    address.setAttributeValue("location", WorkflowConstants.DEFAULT_ODE_URL + "/ode/processes/"
//                            + StringUtil.convertToJavaIdentifier(workflowName));
//                }
//            }
//        }
//    }
//
//    /**
//     * @param wsdl
//     */
//    private void addBindings(WsdlDefinitions wsdl, URI dscUrl) {
//        Iterator<WsdlBinding> itr = wsdl.bindings().iterator();
//        int count = 0;
//        while (itr.hasNext()) {
//            itr.next();
//            ++count;
//        }
//        if (0 == count) {
//            DSCUtil.convertToCWSDL(wsdl, dscUrl);
//        }
//
//    }
//
//    private void addImportsAndHeaderMessage(WsdlDefinitions wsdl) {
//        try {
//
//            // Add the namespace to the lead context
//
//            org.xmlpull.infoset.XmlElement types = wsdl.getTypes();
//            // get the ns of schema from existing element
//            XmlNamespace schemaNs = BUILDER.newNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
//
//            addCrosscutImportsIfNecessary(wsdl);
//
//            org.xmlpull.infoset.XmlElement schema = BUILDER.newFragment(schemaNs, SCHEMA);
//            types.addElement(0, schema);
//
//            org.xmlpull.infoset.XmlElement importElement = BUILDER.newFragment(schema.getNamespace(), IMPORT);
//            importElement.setAttributeValue(NAMESPACE, LEAD_CONTEXT_HEADER_NS);
//            importElement.setAttributeValue(SCHEMA_LOCATION, SCHEMA_LOCATION_URI);
//            schema.addElement(0, importElement);
//
//            wsdl.xml().declareNamespace(BUILDER.newNamespace(LC, LEAD_CONTEXT_HEADER_NS));
//
//            Iterator<WsdlPortType> iterator = wsdl.portTypes().iterator();
//            while (iterator.hasNext()) {
//                WsdlPortType portType = iterator.next();
//                Iterator<WsdlPortTypeOperation> operations = portType.operations().iterator();
//                while (operations.hasNext()) {
//                    WsdlPortTypeOperation operation = operations.next();
//                    WsdlMessagePart leadHeaderPart = new WsdlMessagePart(LEAD_HEADER);
//                    leadHeaderPart.setName(LEAD_HEADER);
//                    // we hand set this element to xml because when you use the
//                    // API it doent add the namespace correctly
//                    leadHeaderPart.xml().setAttributeValue(ELEMENT, LC_CONTEXT);
//
//                    wsdl.getMessage(operation.getInput().getMessage().getLocalPart()).addPart(leadHeaderPart);
//                }
//            }
//
//            Iterator<WsdlBinding> bindingItr = wsdl.bindings().iterator();
//            while (bindingItr.hasNext()) {
//                WsdlBinding wsdlBinding = (WsdlBinding) bindingItr.next();
//                Iterator<WsdlBindingOperation> operationsItr = wsdlBinding.operations().iterator();
//                while (operationsItr.hasNext()) {
//                    WsdlBindingOperation wsdlBindingOperation = (WsdlBindingOperation) operationsItr.next();
//                    org.xmlpull.infoset.XmlElement input = wsdlBindingOperation.xml().element(INPUT);
//                    org.xmlpull.infoset.XmlElement body = input.element(BODY);
//                    if (body == null) {
//                        // This is a HTTP binding so continue with the next
//                        continue;
//
//                    }
//
//                    body.setAttributeValue(PARTS, INPUT);
//                    XmlNamespace ns = body.getNamespace();
//
//                    org.xmlpull.infoset.XmlElement header = input.newElement(ns, HEADER);
//                    header.setAttributeValue(PART, LEAD_HEADER);
//                    header.setAttributeValue(USE, LITERAL);
//                    String inputMessage = findInputMessage(wsdlBindingOperation, wsdl);
//                    header.setAttributeValue(MESSAGE, inputMessage);
//                    body.removeAttribute(body.attribute(PARTS));
//                    String inputPartName = null;
//
//                    WsdlMessage wsdlMessage = wsdl.getMessage(findInputMessaQname(wsdlBindingOperation, wsdl)
//                            .getLocalPart());
//                    Iterable<WsdlMessagePart> parts = wsdlMessage.parts();
//                    Iterator<WsdlMessagePart> partsItr = parts.iterator();
//                    while (partsItr.hasNext()) {
//                        WsdlMessagePart wsdlMessagePart = (WsdlMessagePart) partsItr.next();
//                        if (!LEAD_HEADER.equals(wsdlMessagePart.getName())) {
//                            inputPartName = wsdlMessagePart.getName();
//                            break;
//                        }
//                    }
//
//                    if (null == inputPartName) {
//                        throw new WorkflowRuntimeException("Could not find a partname in message :" + inputMessage
//                                + " for binding :" + wsdlBindingOperation);
//                    }
//
//                    body.setAttributeValue(PARTS, inputPartName);
//
//                    input.addChild(header);
//
//                }
//            }
//        } catch (XmlBuilderException e) {
//            log.error(e.getMessage(), e);
//        }
//    }
//
//    /**
//     * 
//     * @param wsdl
//     */
//    private void addCrosscutImportsIfNecessary(WsdlDefinitions wsdl) {
//        org.xmlpull.infoset.XmlElement types = wsdl.getTypes();
//        XmlNamespace schemaNs = BUILDER.newNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
//        Iterable<org.xmlpull.infoset.XmlElement> schemas = types.elements(null, SCHEMA);
//        for (org.xmlpull.infoset.XmlElement schema : schemas) {
//
//            if (WSDLUtil.attributeExist(schema, "type", "crosscutns:LeadCrosscutParameters")) {
//                // so its there now check whether the impport is already there
//                boolean found = false;
//                Iterable<org.xmlpull.infoset.XmlElement> imports = schema.elements(schemaNs, IMPORT);
//                for (org.xmlpull.infoset.XmlElement importElement : imports) {
//                    found = found
//                            || WSDLUtil.attributeExist(importElement, "namespace",
//                                    "http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/");
//                }
//                if (!found) {
//                    org.xmlpull.infoset.XmlElement crosscutImport = BUILDER.newFragment(schemaNs, "import");
//                    crosscutImport.setAttributeValue("namespace",
//                            "http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/");
//                    crosscutImport.setAttributeValue("schemaLocation", "lead-crosscut-parameters.xsd");
//                    schema.addChild(0, crosscutImport);
//                }
//            }
//
//            if (WSDLUtil.attributeExist(schema, "type", "globalTypens:LEADFileIDArrayType")) {
//                // so its there now check whether the impport is already there
//                boolean found = false;
//                Iterable<org.xmlpull.infoset.XmlElement> imports = schema.elements(schemaNs, IMPORT);
//                for (org.xmlpull.infoset.XmlElement importElement : imports) {
//                    found = found
//                            || WSDLUtil.attributeExist(importElement, "namespace",
//                                    "http://www.extreme.indiana.edu/lead/xsd");
//                }
//                if (!found) {
//                    org.xmlpull.infoset.XmlElement crosscutImport = BUILDER.newFragment(schemaNs, "import");
//                    crosscutImport.setAttributeValue("namespace", "http://www.extreme.indiana.edu/lead/xsd");
//                    crosscutImport.setAttributeValue("schemaLocation", "gfac-simple-types.xsd");
//                    schema.addChild(0, crosscutImport);
//                }
//            }
//        }
//    }
//
//    /**
//     * @param wsdlBindingOperation
//     * @param wsdl
//     * @return
//     */
//    private String findInputMessage(WsdlBindingOperation wsdlBindingOperation, WsdlDefinitions wsdl) {
//
//        QName message = findInputMessaQname(wsdlBindingOperation, wsdl);
//        return message.getPrefix() + ":" + message.getLocalPart();
//    }
//
//    private QName findInputMessaQname(WsdlBindingOperation wsdlBindingOperation, WsdlDefinitions wsdl) {
//        String operationName = wsdlBindingOperation.getName();
//        WsdlPortType portType = wsdl.getPortType(wsdlBindingOperation.getBinding().getPortType().getLocalPart());
//        WsdlPortTypeOperation operation = portType.getOperation(operationName);
//        QName message = operation.getInput().getMessage();
//        return message;
//    }
//
//    public void setOdeLocation(String ODEEprEndingWithPort, String workflowName, WsdlDefinitions wsdl) {
//        Iterator<WsdlService> serviceItr = wsdl.services().iterator();
//        if (serviceItr.hasNext()) {
//            WsdlService service = serviceItr.next();
//            Iterator<WsdlPort> portItr = service.ports().iterator();
//            if (portItr.hasNext()) {
//                WsdlPort port = portItr.next();
//                org.xmlpull.infoset.XmlElement address = port.xml().element("address");
//                if (address != null) {
//                    URI uri = null;
//                    try {
//                        uri = new URI(ODEEprEndingWithPort + "/ode/processes/"
//                                + StringUtil.convertToJavaIdentifier(workflowName));
//                    } catch (URISyntaxException e) {
//                        throw new RuntimeException(e);
//                    }
//                    address.setAttributeValue("location", uri.toString());
//                } else {
//                    throw new IllegalStateException("No address found in :" + wsdl.xmlStringPretty());
//                }
//            } else {
//                throw new IllegalStateException("No port found in :" + wsdl.xmlStringPretty());
//            }
//        } else {
//            throw new IllegalStateException("No service found in :" + wsdl.xmlStringPretty());
//        }
//    }
//
//}