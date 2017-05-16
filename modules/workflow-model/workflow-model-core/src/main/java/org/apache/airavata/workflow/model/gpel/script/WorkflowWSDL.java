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
package org.apache.airavata.workflow.model.gpel.script;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.WSConstants;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.component.ws.WSComponentPort;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.system.ParameterNode;
import org.apache.airavata.workflow.model.graph.system.SystemDataPort;
import org.apache.airavata.workflow.model.graph.ws.WSGraph;
import org.apache.airavata.workflow.model.utils.WorkflowConstants;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;
import org.xmlpull.infoset.XmlNamespace;

//import xsul5.XmlConstants;
//import xsul5.wsdl.WsdlDefinitions;
//import xsul5.wsdl.WsdlDocumentation;
//import xsul5.wsdl.WsdlMessage;
//import xsul5.wsdl.WsdlPortType;
//import xsul5.wsdl.WsdlPortTypeOperation;
//import xsul5.wsdl.plnk.PartnerLinkRole;
//import xsul5.wsdl.plnk.PartnerLinkType;

public class WorkflowWSDL {

    /**
     * Run
     */
    private String workflowOperationName;

    private static final String INPUT_MESSAGE_ELEMENT_SUFFIX = "";

    private static final String OUTPUT_MESSAGE_ELEMENT_SUFFIX = "Response";

    /**
     * Run
     */
    private String workflowInputMessageElelmentName;

    /**
     * RunOutput
     */
    private String workflowOutputMessageElementName;

    private static final String INPUT_MESSAGE_SUFFIX = "InputMessage";

    private static final String OUTPUT_MESSAGE_SUFFIX = "OutputMessage";

    /**
     * RunInputMessage
     */
    private String workflowInputMessageName;

    /**
     * RunOutputMessage
     */
    private String workflowOutputMessageName;

    /**
     * input
     */
    public static final String INPUT_PART_NAME = "input";

    /**
     * output
     */
    public static final String OUTPUT_PART_NAME = "output";

    private static final String TARGET_NS_NAME_PREFIX = WorkflowConstants.NS_URI_XBAYA;

    private static final String TYPE_SUFFIX = "Type";

    private Workflow workflow;

    private WSGraph graph;

//    private WsdlDefinitions definitions;

    private XmlNamespace targetNamespace;

    private XmlNamespace typesNamespace;

    private QName portTypeQName;

//    private Map<QName, PartnerLinkRole> partnerLinkRoleMap;
    private static final Logger log = LoggerFactory.getLogger(WorkflowWSDL.class);

    /**
     * Constructs a WorkflowWsdl.
     * 
     * @param workflow
     * @param operationName
     */
    public WorkflowWSDL(Workflow workflow, String operationName) {
        this.workflow = workflow;
        this.graph = workflow.getGraph();
//        this.partnerLinkRoleMap = new HashMap<QName, PartnerLinkRole>();
        workflowOperationName = operationName;

        workflowInputMessageElelmentName = workflowOperationName + INPUT_MESSAGE_ELEMENT_SUFFIX;

        workflowOutputMessageElementName = workflowOperationName + OUTPUT_MESSAGE_ELEMENT_SUFFIX;

        workflowInputMessageName = workflowOperationName + INPUT_MESSAGE_SUFFIX;

        workflowOutputMessageName = workflowOperationName + OUTPUT_MESSAGE_SUFFIX;
    }

//    /**
//     * @return the WSLD definitions
//     */
//    public WsdlDefinitions getWsdlDefinitions() {
//        return this.definitions;
//    }

    /**
     * @return The target namespace.
     */
    public XmlNamespace getTargetNamespace() {
        return this.targetNamespace;
    }

    /**
     * @return The types namespace. typens:"http://..../xsd/"
     */
    public XmlNamespace getTypesNamespace() {
        return this.typesNamespace;
    }

    /**
     * @return The portType QName.
     */
    public QName getPortTypeQName() {
        return this.portTypeQName;
    }

    /**
     * Creates WSDL.
     * 
     * @throws GraphException
     */
    public void create() throws GraphException {

        try {
//            String targetNSName = TARGET_NS_NAME_PREFIX + this.graph.getID() + "/";
//            this.targetNamespace = XmlConstants.BUILDER.newNamespace(WSConstants.TARGET_NS_PREFIX, targetNSName);
//            String typesNSName = targetNSName + "xsd/";
//            this.typesNamespace = XmlConstants.BUILDER.newNamespace(WSConstants.TYPE_NS_PREFIX, typesNSName);
//
//            this.definitions = new WsdlDefinitions(targetNSName);
//            this.definitions.xml().setAttributeValue(WSConstants.NAME_ATTRIBUTE, this.graph.getID());
//
//            this.definitions.xml().declareNamespace(this.targetNamespace);
//            this.definitions.xml().declareNamespace(this.typesNamespace);
//            this.definitions.xml().declareNamespace(WSConstants.XSD_NS);
//            this.definitions.xml().declareNamespace(PartnerLinkType.NS);
//            addDocumentation();
//            addTypes();
//            WsdlMessage inputMessage = createInputMessage();
//            WsdlMessage outputMessage = createOutputMessage();
//            createPortType(inputMessage, outputMessage);
            addComment();
        } catch (RuntimeException e) {
            throw new GraphException(e);
        }
    }

    /**
     * @param servicePortTypeQName
     * @return PartnerLinkRole
     */
//    public PartnerLinkRole getPartnerRoll(QName servicePortTypeQName) {
//        return this.partnerLinkRoleMap.get(servicePortTypeQName);
//    }

    /**
     * Adds a partnerLinkType.
     * 
     * This method is called by BPELScript.
     * 
     * @param partnerLinkTypeName
     * @param partnerRollName
     * @param servicePortTypeQName
     * @return PartnerLinkRole
     */
//    public PartnerLinkRole addPartnerLinkTypeAndRoll(String partnerLinkTypeName, String partnerRollName,
//            QName servicePortTypeQName) {
//        PartnerLinkType partnerLinkType = new PartnerLinkType(partnerLinkTypeName);
//        PartnerLinkRole partnerRoll = new PartnerLinkRole(partnerRollName, servicePortTypeQName);
//        partnerLinkType.addRole(partnerRoll);
//
//        declareNamespaceIfNecessary("p", servicePortTypeQName.getNamespaceURI(), true);
//        this.definitions.xml().addElement(partnerLinkType.xml());
//
//        this.partnerLinkRoleMap.put(servicePortTypeQName, partnerRoll);
//        return partnerRoll;
//    }

    /**
     * @param operationName
     * @param receiveNode
     * @return The portType added.
     */
//    public WsdlPortType addReceivePortType(String operationName, ReceiveNode receiveNode) {
//        //
//        // <types>
//        //
//
//        // <types> and <schema> have been defined.
//        XmlElement types = this.definitions.getTypes();
//        XmlElement schema = types.element(WSConstants.SCHEMA_TAG);
//
//        XmlElement sequence = setupParameterType(operationName, null, schema);
//        for (DataPort outputPort : receiveNode.getOutputPorts()) {
//            addParameter(receiveNode, (SystemDataPort) outputPort, sequence, schema);
//        }
//
//        //
//        // <message>
//        //
//        String messageName = operationName + INPUT_MESSAGE_SUFFIX;
//        String partName = INPUT_PART_NAME;
//        String messageElementName = operationName + INPUT_MESSAGE_ELEMENT_SUFFIX;
//        WsdlMessage inputMessage = createMessage(messageName, partName, messageElementName);
//
//        String portTypeName = operationName;
//        WsdlPortType portType = createPortType(portTypeName, operationName, inputMessage, null);
//        return portType;
//    }

    private void addComment() {
//        XmlComment comment = this.definitions.xml().newComment(
//                "\nThis document is automatically generated by " + WorkflowConstants.APPLICATION_NAME_ + " "
//                        + ApplicationVersion.VERSION + ".\n");
//        this.definitions.xml().insertChild(0, "\n");
//        this.definitions.xml().insertChild(0, comment);
//        this.definitions.xml().insertChild(0, "\n");
    }

    /**
     * Sets the documentation element.
     */
    private void addDocumentation() {
        String description = this.workflow.getDescription();
//        if (description != null) {
//            WsdlDocumentation documentation = new WsdlDocumentation(description);
//            this.definitions.setDocumentation(documentation);
//        }
    }

    /**
     * Adds the types element.
     * 
     * @return The types element
     */
//    private XmlElement addTypes() {
//        XmlElement types = this.definitions.getOrCreateTypes();
//
//        XmlElement schema = types.addElement(WSConstants.SCHEMA_TAG);
//        schema.setAttributeValue(WSConstants.TARGET_NAMESPACE_ATTRIBUTE, this.typesNamespace.getName());
//        schema.setAttributeValue(WSConstants.XMLNS, WSConstants.XSD_NS_URI);
//        schema.setAttributeValue(WSConstants.ELEMENT_FORM_DEFAULT_ATTRIBUTE, WSConstants.UNQUALIFIED_VALUE);
//        List<InputNode> inputNodes = GraphUtil.getInputNodes(this.graph);
////        XmlElement inputMetadata = this.graph.getInputMetadata();
////        addParameters(workflowInputMessageElelmentName, inputMetadata, inputNodes, schema);
//
////        List<OutputNode> outputNodes = GraphUtil.getOutputNodes(this.graph);
////        XmlElement outputMetadata = this.graph.getOutputMetadata();
////        addParameters(workflowOutputMessageElementName, outputMetadata, outputNodes, schema);
//
//        return types;
//    }

    private void addParameters(String name, XmlElement appinfo, List<? extends ParameterNode> nodes, XmlElement schema) {
        XmlElement sequence = setupParameterType(name, appinfo, schema);
        for (ParameterNode node : nodes) {
            addParameter(node, sequence, schema);
        }
    }

    /**
     * @param name
     * @param appinfo
     * @param schema
     * @return The sequence element.
     */
    private XmlElement setupParameterType(String name, XmlElement appinfo, XmlElement schema) {
        XmlElement element = schema.addElement(WSConstants.ELEMENT_TAG);
        element.setAttributeValue(WSConstants.NAME_ATTRIBUTE, name);
        String type = name + TYPE_SUFFIX;
        element.setAttributeValue(WSConstants.TYPE_ATTRIBUTE, WSConstants.TYPE_NS_PREFIX + ":" + type);

        // add metadata
        if (appinfo != null) {
            XmlElement annotation = element.addElement(WSConstants.ANNOTATION_TAG);
            try {
                annotation.addElement(XMLUtil.deepClone(appinfo));
            } catch (AiravataException e) {
                log.error(e.getMessage(), e);
            }
        }

        XmlElement complex = schema.addElement(WSConstants.COMPLEX_TYPE_TAG);
        complex.setAttributeValue(WSConstants.NAME_ATTRIBUTE, type);

        XmlElement sequence = complex.addElement(WSConstants.SEQUENCE_TAG);
        return sequence;
    }

    /**
     * Adds the parameter element.
     * 
     * @param node
     * @param sequence
     * @param schema
     * @return The parameter element
     */
    private XmlElement addParameter(ParameterNode node, XmlElement sequence, XmlElement schema) {
        XmlElement element;
        SystemDataPort port = node.getPort();
        element = addParameter(node, port, sequence, schema);

        //
        // Annotation
        //
        String description = node.getDescription();
        XmlElement appinfo = node.getMetadata();

        // description
        if (description != null && description.trim().length() != 0) {
            XmlElement annotation = element.element(null, WSConstants.ANNOTATION_TAG, true);
            XmlElement documentation = annotation.addElement(WSConstants.DOCUMENTATION_TAG);
            documentation.setText(node.getDescription());
        }

        // appinfo
        if (appinfo != null) {
            XmlElement annotation = element.element(null, WSConstants.ANNOTATION_TAG, true);
            try {
                annotation.addElement(XMLUtil.deepClone(appinfo));
            } catch (AiravataException e) {
                log.error(e.getMessage(), e);
            }
        }

        //
        // Add default value if it's input.
        //
        if (node instanceof InputNode) {
            InputNode inputNode = (InputNode) node;
            Object value = inputNode.getDefaultValue();
            if (value instanceof String) {
                element.setAttributeValue(WSConstants.DEFAULT_ATTRIBUTE, (String) value);
            } else if (value instanceof XmlElement) {
                // Add the default value in <annotation><default> because there
                // is no standard way.
                XmlElement valueElement = null;
                try {
                    valueElement = XMLUtil.deepClone((XmlElement) value);
                } catch (AiravataException e) {
                    log.error(e.getMessage(), e);
                }
                XmlElement annotation = element.element(null, WSConstants.ANNOTATION_TAG, true);
                XmlElement defaultElement = annotation.addElement(WSComponentPort.DEFAULT);
                defaultElement.addElement(valueElement);
            }
        }

        return element;
    }

    private XmlElement addParameter(Node node, SystemDataPort port, XmlElement sequence, XmlElement schema) {
        XmlElement element = sequence.addElement(WSConstants.ELEMENT_TAG);
        /*element.setAttributeValue(WSConstants.NAME_ATTRIBUTE, node.getID());

        //
        // type
        //
        QName type = port.getType();
        WSComponentPort componentPort = port.getWSComponentPort();
        WsdlDefinitions wsdl = null;
        if (componentPort != null) {
        	//FIXME
//            wsdl = componentPort.getComponent().getWSDL();
//            type = declareTypeIfNecessary(wsdl, type);
        }
        int arrayDimension = port.getArrayDimension();
        if (arrayDimension == 1) {
            String typeName = declareArrayType(schema, type, wsdl);
            type = new QName(this.typesNamespace.getName(), typeName);
        } else if (arrayDimension > 1) {
            // TODO
            throw new WorkflowRuntimeException("multi-dimentional arrays are not supported yet.");
        }

        if (WSConstants.XSD_ANY_TYPE.equals(type) && componentPort != null) {
            XmlElement elementElement = componentPort.getElement();
            if (elementElement == null) {
                // Types are not defined anywhare. Leave it as xsd:any.
                setTypeAttribute(element, type);
            } else {
                // Copy the embedded type defition.
                XmlElement clonedElementElement = null;
                try {
                    clonedElementElement = XMLUtil.deepClone(elementElement);
                } catch (UtilsException e) {
                    e.printStackTrace();
                }
                String typeString = clonedElementElement.attributeValue(WSConstants.TYPE_ATTRIBUTE);
                if (typeString == null) {
                    for (Object child : clonedElementElement.children()) {
                        if (child instanceof XmlElement) {
                            ((XmlElement) child).setParent(null);
                        }
                        element.addChild(child);
                    }
                } else {
                    // The case when type is really xsd:any
                    setTypeAttribute(element, type);
                }
            }
        } else {
            // The normal case.
            setTypeAttribute(element, type);
        }*/
        return element;
    }

    private void setTypeAttribute(XmlElement element, QName type) {
        String namespaceURI = type.getNamespaceURI();
        XmlNamespace namespace = element.lookupNamespaceByName(namespaceURI);
        element.setAttributeValue(WSConstants.TYPE_ATTRIBUTE, namespace.getPrefix() + ":" + type.getLocalPart());

    }

    /**
     * @param serviceWSDL
     * @param paramType
     * @return The QName of the type. This QName always has prefix.
     */
//    private QName declareTypeIfNecessary(WsdlDefinitions serviceWSDL, QName paramType) {
//        if (WSConstants.XSD_NS_URI.equals(paramType.getNamespaceURI())) {
//            // No need to define
//            return new QName(WSConstants.XSD_NS_URI, paramType.getLocalPart(), WSConstants.XSD_NS_PREFIX);
//        }
//
//        // check if this type already exists in the workflow WSDL.
//        XmlElement typeDefinition = null;
//        try {
//            typeDefinition = WSDLUtil.getTypeDefinition(this.definitions, paramType);
//
//            if (typeDefinition == null) {
//
//                // now lets check whether there is an import in the service wsdl schema
//                // that would import this type,
//                // if so we would be done by just importing that schema
//
//                typeDefinition = WSDLUtil.findTypeDefinitionInImports(serviceWSDL, paramType);
//                if (typeDefinition != null) {
//                    XmlElement importEle = WSDLUtil.getImportContainingTypeDefinition(serviceWSDL, paramType);
//                    addImportIfNecessary(importEle);
//                    String prefix = declareNamespaceIfNecessary(paramType.getPrefix(), paramType.getNamespaceURI(),
//                            false);
//                    return new QName(paramType.getNamespaceURI(), paramType.getLocalPart(), prefix);
//                }
//
//                // copy the type defition and use it.
//
//                // Need to copy the whole schema because it might have different
//                // targetNamespace.
//                XmlElement newSchema = WSDLUtil.getSchema(serviceWSDL, paramType);
//                if (newSchema == null) {
//                    // This should have been caught in WSComponent
//                    throw new WorkflowRuntimeException("could not find definition for type " + paramType + " in "
//                            + WSDLUtil.getWSDLQName(serviceWSDL));
//                }
//                this.definitions.getTypes().addChild(XMLUtil.deepClone(newSchema));
//
//                String prefix = declareNamespaceIfNecessary(paramType.getPrefix(), paramType.getNamespaceURI(), false);
//                return new QName(paramType.getNamespaceURI(), paramType.getLocalPart(), prefix);
//            } else {
//                XmlNamespace namespace = this.definitions.xml().lookupNamespaceByName(paramType.getNamespaceURI());
//                return new QName(paramType.getNamespaceURI(), paramType.getLocalPart(), namespace.getPrefix());
//            }
//        } catch (UtilsException e) {
//            log.error(e.getMessage(), e);
//        }
//        return null;
//    }

//    private void addImportIfNecessary(XmlElement importEle) {
//        XmlElement schema = this.definitions.getTypes().element(WSConstants.SCHEMA_TAG);
//        Iterable<XmlElement> imports = schema.elements(null, WSConstants.IMPORT_TAG);
//        for (XmlElement importElement : imports) {
//            if (importElement.attributeValue("namespace").equals(importEle.attributeValue("namespace"))
//                    && importElement.attributeValue("schemaLocation")
//                            .equals(importEle.attributeValue("schemaLocation"))) {
//                return;
//            }
//        }
//        schema.addChild(0, importEle);
//    }

//    private String declareArrayType(XmlElement schema, QName valueType, WsdlDefinitions serviceWSDL) {
//        XmlElement complexType = schema.addElement(WSConstants.COMPLEX_TYPE_TAG);
//        String typeName = valueType.getLocalPart() + "ArrayType";
//        // TODO check if this typeName is already used.
//        complexType.setAttributeValue(WSConstants.NAME_ATTRIBUTE, typeName);
//        XmlElement sequence = complexType.addElement(WSConstants.SEQUENCE_TAG);
//        XmlElement element = sequence.addElement(WSConstants.ELEMENT_TAG);
//        element.setAttributeValue(WSConstants.MIN_OCCURS_ATTRIBUTE, "0");
//        element.setAttributeValue(WSConstants.MAX_OCCURS_ATTRIBUTE, WSConstants.UNBOUNDED_VALUE);
//        element.setAttributeValue(WSConstants.NAME_ATTRIBUTE, "value");
//        valueType = declareTypeIfNecessary(serviceWSDL, valueType);
//        element.setAttributeValue(WSConstants.TYPE_ATTRIBUTE, valueType.getPrefix() + ":" + valueType.getLocalPart());
//        return typeName;
//    }

//    /**
//     * Creates the input message.
//     * 
//     * @return The input message
//     */
//    private WsdlMessage createInputMessage() {
//        return createMessage(workflowInputMessageName, INPUT_PART_NAME, workflowInputMessageElelmentName);
//    }
//
//    /**
//     * Creates the output message.
//     * 
//     * @return The output message
//     */
//    private WsdlMessage createOutputMessage() {
//        return createMessage(workflowOutputMessageName, OUTPUT_PART_NAME, workflowOutputMessageElementName);
//    }
//
//    private WsdlMessage createMessage(String messageName, String partName, String messageElementName) {
//        WsdlMessage outMessage = this.definitions.addMessage(messageName);
//        outMessage.addPartWithElement(partName, new QName(this.typesNamespace.getName(), messageElementName,
//                this.typesNamespace.getPrefix()));
//        return outMessage;
//    }
//
//    /**
//     * Creates the portType.
//     * 
//     * @param inputMessage
//     * @param outputMessage
//     * @return The portType
//     */
//    private WsdlPortType createPortType(WsdlMessage inputMessage, WsdlMessage outputMessage) {
//        String portTypeName = this.graph.getID();
//        this.portTypeQName = new QName(this.targetNamespace.getName(), portTypeName);
//        return createPortType(this.graph.getID(), workflowOperationName, inputMessage, outputMessage);
//    }
//
//    private WsdlPortType createPortType(String portTypeName, String operationName, WsdlMessage inputMessage,
//            WsdlMessage outputMessage) {
//        WsdlPortType portType = this.definitions.addPortType(portTypeName);
//        WsdlPortTypeOperation operation = portType.addOperation(operationName);
//        if (inputMessage != null) {
//            operation.setInput(inputMessage);
//        }
//        if (outputMessage != null) {
//            operation.setOutput(outputMessage);
//        }
//        return portType;
//    }
//
//    private String declareNamespaceIfNecessary(String prefixCandidate, String uri, boolean alwaysUseSuffix) {
//        XmlNamespace namespace = this.definitions.xml().lookupNamespaceByName(uri);
//        if (namespace == null) {
//            return declareNamespace(prefixCandidate, uri, alwaysUseSuffix);
//        } else {
//            return namespace.getPrefix();
//        }
//    }
//
//    /**
//     * @param prefixCandidate
//     * @param uri
//     * @param alwaysUseSuffix
//     * @return The prefix actually used.
//     */
//    private String declareNamespace(String prefixCandidate, String uri, boolean alwaysUseSuffix) {
//        if (prefixCandidate == null || prefixCandidate.length() == 0) {
//            prefixCandidate = "a";
//        }
//        String prefix = prefixCandidate;
//        if (alwaysUseSuffix) {
//            prefix += "0";
//        }
//        if (this.definitions.xml().lookupNamespaceByPrefix(prefix) != null) {
//            int i = 1;
//            prefix = prefixCandidate + i;
//            while (this.definitions.xml().lookupNamespaceByPrefix(prefix) != null) {
//                i++;
//            }
//        }
//        this.definitions.xml().declareNamespace(prefix, uri);
//        return prefix;
//    }

    /**
     * Returns the workflowOperationName.
     * 
     * @return The workflowOperationName
     */
    public String getWorkflowOperationName() {
        return this.workflowOperationName;
    }

    /**
     * Sets workflowOperationName.
     * 
     * @param workflowOperationName
     *            The workflowOperationName to set.
     */
    public void setWorkflowOperationName(String workflowOperationName) {
        this.workflowOperationName = workflowOperationName;
    }

    /**
     * Returns the workflowInputMessageName.
     * 
     * @return The workflowInputMessageName
     */
    public String getWorkflowInputMessageName() {
        return this.workflowInputMessageName;
    }

    /**
     * Sets workflowInputMessageName.
     * 
     * @param workflowInputMessageName
     *            The workflowInputMessageName to set.
     */
    public void setWorkflowInputMessageName(String workflowInputMessageName) {
        this.workflowInputMessageName = workflowInputMessageName;
    }

    /**
     * Returns the workflowOutputMessageName.
     * 
     * @return The workflowOutputMessageName
     */
    public String getWorkflowOutputMessageName() {
        return this.workflowOutputMessageName;
    }

    /**
     * Sets workflowOutputMessageName.
     * 
     * @param workflowOutputMessageName
     *            The workflowOutputMessageName to set.
     */
    public void setWorkflowOutputMessageName(String workflowOutputMessageName) {
        this.workflowOutputMessageName = workflowOutputMessageName;
    }

    /**
     * Returns the workflowInputMessageElelmentName.
     * 
     * @return The workflowInputMessageElelmentName
     */
    public String getWorkflowInputMessageElelmentName() {
        return this.workflowInputMessageElelmentName;
    }

    /**
     * Sets workflowInputMessageElelmentName.
     * 
     * @param workflowInputMessageElelmentName
     *            The workflowInputMessageElelmentName to set.
     */
    public void setWorkflowInputMessageElelmentName(String workflowInputMessageElelmentName) {
        this.workflowInputMessageElelmentName = workflowInputMessageElelmentName;
    }

    /**
     * Returns the workflowOutputMessageElementName.
     * 
     * @return The workflowOutputMessageElementName
     */
    public String getWorkflowOutputMessageElementName() {
        return this.workflowOutputMessageElementName;
    }

    /**
     * Sets workflowOutputMessageElementName.
     * 
     * @param workflowOutputMessageElementName
     *            The workflowOutputMessageElementName to set.
     */
    public void setWorkflowOutputMessageElementName(String workflowOutputMessageElementName) {
        this.workflowOutputMessageElementName = workflowOutputMessageElementName;
    }

}