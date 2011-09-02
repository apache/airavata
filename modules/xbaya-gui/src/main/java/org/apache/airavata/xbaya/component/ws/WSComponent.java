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

package org.apache.airavata.xbaya.component.ws;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.component.Component;
import org.apache.airavata.xbaya.component.ComponentControlPort;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.gpel.DSCUtil;
import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.ws.WSNode;
import org.apache.airavata.xbaya.util.WSConstants;
import org.apache.airavata.xbaya.util.WSDLUtil;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.xmlpull.infoset.XmlCharacters;
import org.xmlpull.infoset.XmlElement;

import xsul5.MLogger;
import xsul5.wsdl.WsdlDefinitions;
import xsul5.wsdl.WsdlDocumentation;
import xsul5.wsdl.WsdlMessage;
import xsul5.wsdl.WsdlMessagePart;
import xsul5.wsdl.WsdlPortType;
import xsul5.wsdl.WsdlPortTypeInput;
import xsul5.wsdl.WsdlPortTypeOperation;
import xsul5.wsdl.WsdlPortTypeOutput;

public class WSComponent extends Component {

    private static final MLogger logger = MLogger.getLogger();

    protected WsdlDefinitions wsdl;

    protected QName wsdlQName;

    /**
     * The list of output component ports.
     */
    protected List<WSComponentPort> inputs;

    /**
     * The list of input component ports.
     */
    protected List<WSComponentPort> outputs;

    private String description;

    private String operationName;

    private String targetNamespace;

    private QName portTypeQName;

    private String inputPartName;

    private String outputPartName;

    private String inputTypeName;

    private String outputTypeName;

    protected WSComponent() {

    }


    /**
     * Constructs a WSComponent.
     * 
     * @param componentPath
     * @param wsdl
     * @throws ComponentException
     */
    public WSComponent(WsdlDefinitions wsdl) throws ComponentException {
        // This constructor is called only from WorkflowComponent where we know
        // that there is only one operation in WSDL.
        this(wsdl, null, null);
    }

    /**
     * Constructs a WSComponent.
     * 
     * @param wsdl
     * @param portTypeQName
     * @param operationName
     * @throws ComponentException
     */
    public WSComponent(WsdlDefinitions wsdl, QName portTypeQName, String operationName) throws ComponentException {
        this.inputs = new ArrayList<WSComponentPort>();
        this.outputs = new ArrayList<WSComponentPort>();

        this.wsdl = wsdl;
        if (portTypeQName == null) {
            portTypeQName = WSDLUtil.getFirstPortTypeQName(wsdl);
        }
        this.portTypeQName = portTypeQName;
        if (operationName == null) {
            operationName = WSDLUtil.getFirstOperationName(wsdl, this.portTypeQName);
        }
        this.operationName = operationName;
        this.description = ""; // To prevent to show null

        setName(this.portTypeQName.getLocalPart() + ":" + this.operationName);

        parse();

        this.controlInPort = new ComponentControlPort();
        this.controlOutPorts.add(new ComponentControlPort());
    }

    /**
     * @return The WSDL
     */
    public WsdlDefinitions getWSDL() {
        return this.wsdl;
    }

    public WsdlDefinitions getConcreteWSDL(URI dscUri) {
        if (WSDLUtil.isAWSDL(this.wsdl)) {
            return DSCUtil.convertToCWSDL(this.wsdl, dscUri);
        } else {
            return this.wsdl;
        }
    }

    /**
     * Returns the QName of the WSDL.
     * 
     * @return The QName of the WSDL.
     */
    public QName getWSDLQName() {
        return this.wsdlQName;
    }

    /**
     * Returns the QName of the portType.
     * 
     * @return The QName of the portType
     */
    public QName getPortTypeQName() {
        return this.portTypeQName;
    }

    /**
     * Returns the operation name.
     * 
     * @return The operation name
     */
    public String getOperationName() {
        return this.operationName;
    }

    /**
     * Returns the inputPartName.
     * 
     * @return The inputPartName
     */
    public String getInputPartName() {
        return this.inputPartName;
    }

    /**
     * Returns the outputPartName.
     * 
     * @return The outputPartName
     */
    public String getOutputPartName() {
        return this.outputPartName;
    }

    /**
     * Returns the inputTypeName.
     * 
     * @return The inputTypeName
     */
    public String getInputTypeName() {
        return this.inputTypeName;
    }

    /**
     * Returns the outputTypeName.
     * 
     * @return The outputTypeName
     */
    public String getOutputTypeName() {
        return this.outputTypeName;
    }

    // TODO inputAppinfo, outputAppinfo

    /**
     * @return The list of input WSComponentPorts
     */
    @Override
    public List<WSComponentPort> getInputPorts() {
        return this.inputs;
    }

    /**
     * @return The list of output WSComponentPorts
     */
    @Override
    public List<WSComponentPort> getOutputPorts() {
        return this.outputs;
    }

    /**
     * @see org.apache.airavata.xbaya.component.Component#createNode(org.apache.airavata.xbaya.graph.Graph)
     */
    @Override
    public Node createNode(Graph graph) {
        return createNode(graph, new WSNode(graph));
    }

    protected Node createNode(Graph graph, WSNode node) {

        // Copy some infomation from the component

        node.setName(getName());
        node.setComponent(this);
        // node.setWSDLQName(this.wsdlQName);

        // Creates a unique ID for the node. This has to be after setName().
        node.createID();

        // Creat ports
        createPorts(node);

        return node;
    }

    /**
     * @see org.apache.airavata.xbaya.component.Component#toHTML()
     */
    @Override
    public String toHTML() {

        StringBuffer buf = new StringBuffer();
        buf.append("<html>\n");
        buf.append("<h1>Service: " + getName() + "</h1>\n");

        buf.append("<h2>Description:</h2>\n");
        buf.append(this.description);

        buf.append("<h2>Operation: " + this.operationName + "</h2>\n");

        buf.append("<h3>Input parameter(s)</h3>\n");
        messageToHtml(getInputPorts(), buf);

        buf.append("<h3>Output parameter(s)</h3>\n");
        messageToHtml(getOutputPorts(), buf);

        buf.append("</html>\n");
        return buf.toString();
    }

    /**
     * @return The XML Element.
     */
    public XmlElement toXML() {
        return this.wsdl.xml();
    }

    /**
     * @return The WSDL in String.
     */
    public String toXMLText() {
        return this.wsdl.xmlStringPretty();
    }

    private void messageToHtml(List<WSComponentPort> ports, StringBuffer buf) {
        buf.append("<dl>\n");
        for (WSComponentPort port : ports) {
            buf.append("<dt><strong>" + port.getName() + "</strong></dt>\n");
            buf.append("<dd>Type: " + port.getType().getLocalPart() + "</dd>\n");
            buf.append("<dd>Description: " + port.getDescription() + "</dd>\n");
        }
        buf.append("</dl>\n");
    }

    private void parse() throws ComponentException {

        // The name of WSDL changes even with the same portType.
        // this.name = this.definitions.getAttributeValue(null, "name");

        this.targetNamespace = this.wsdl.getTargetNamespace();

        String wsdlName = this.wsdl.xml().attributeValue(WSConstants.NAME_ATTRIBUTE);
        if (wsdlName == null) {
            wsdlName = "NoName"; // TODO
        }
        this.wsdlQName = new QName(this.targetNamespace, wsdlName);

        WsdlDocumentation documentation = this.wsdl.getDocumentation();
        if (documentation != null) {
            StringBuffer buf = new StringBuffer();
            for (Object child : documentation.xml().children()) {
                if (child instanceof String) {
                    buf.append(child.toString());
                } else if (child instanceof XmlCharacters) {
                    buf.append(((XmlCharacters) child).getText());
                }
            }
            this.description = buf.toString();
        }

        WsdlPortType portType = this.wsdl.getPortType(this.portTypeQName.getLocalPart());
        if (portType == null) {
            throw new ComponentException("portType, " + this.portTypeQName + " is not defined.");
        }
        parsePortType(portType);
    }

    private void parsePortType(WsdlPortType portType) throws ComponentException {
        WsdlPortTypeOperation operation = portType.getOperation(this.operationName);
        if (operation == null) {
            throw new ComponentException("Operation, " + this.operationName + " is not defined.");
        }
        parseOperation(operation);
    }

    private void parseOperation(WsdlPortTypeOperation operation) throws ComponentException {

        WsdlPortTypeInput input = operation.getInput();
        // No input is possible.
        if (input != null) {
            WsdlMessage inputMessage = input.lookupMessage();
            this.inputs = parseMessage(inputMessage, true);
        }

        WsdlPortTypeOutput output = operation.getOutput();
        // No output is possible.
        if (output != null) {
            WsdlMessage outputMessage = output.lookupMessage();
            this.outputs = parseMessage(outputMessage, false);
        }
    }

    private List<WSComponentPort> parseMessage(WsdlMessage message, boolean input) throws ComponentException {
        List<WSComponentPort> parts = new ArrayList<WSComponentPort>();
        for (WsdlMessagePart part : message.parts()) {
            String partName = part.getName();
            if (input) {
                this.inputPartName = partName;
            } else {
                this.outputPartName = partName;
            }

            QName partElement = part.getElement();
            if (partElement == null) {
                // In case type is used directly. This is an old way.
                QName partType = part.getType();
                if (partType != null) {
                    parts.add(new WSComponentPort(partName, partType, this));
                }

            } else {
                String typeName = partElement.getLocalPart();
                if (input) {
                    this.inputTypeName = typeName;
                } else {
                    this.outputTypeName = typeName;
                }
                parseType(typeName, parts);
            }
        }
        return parts;
    }

    private void parseType(String typeName, List<WSComponentPort> parts) throws ComponentException {

        XmlElement typesElement = this.wsdl.getTypes();
        if (typesElement == null) {
            throw new ComponentException("No types is defined.");
        }

        if (typesElement.element(null, WSConstants.SCHEMA_TAG) == null) {
            throw new ComponentException("No schema is defined.");
        }

        XmlElement elementElement = null;
        XmlElement schemaElement = null;

        Iterable<XmlElement> schemaElements = typesElement.elements(null, WSConstants.SCHEMA_TAG);
        for (XmlElement elemt : schemaElements) {
            schemaElement = elemt;
            elementElement = findElementElement(typeName, elemt);
            if (null != elementElement) {
                break;
            }
        }

        if (elementElement == null) {
            throw new ComponentException("No element is defined for " + typeName);
        }
        String typesTargetNamespace = schemaElement.attributeValue(WSConstants.TARGET_NAMESPACE_ATTRIBUTE);
        String elementType = elementElement.attributeValue(WSConstants.TYPE_ATTRIBUTE);

        XmlElement sequenceElement;
        if (elementType == null) {
            // anonymous type
            XmlElement complexElement = elementElement.element(null, WSConstants.COMPLEX_TYPE_TAG);
            if (complexElement == null) {
                throw new ComponentException("We only support complexType as annonymous type: "
                        + XMLUtil.xmlElementToString(elementElement));
            }
            sequenceElement = complexElement.element(null, WSConstants.SEQUENCE_TAG);
            // TODO Check if there is any other defined.
        } else {
            // named complexType
            String elementTypeName = XMLUtil.getLocalPartOfQName(elementType);
            XmlElement typeElement = findTypeElement(elementTypeName, schemaElement);
            sequenceElement = typeElement.element(null, WSConstants.SEQUENCE_TAG);
            // TODO Check if there is any other defined.
        }

        if (sequenceElement == null) {
            // Assume that there is no input/output.
            logger.info("There is no sequence defined.");
        } else {
            // Only supports elements in the sequence now.
            for (XmlElement element : sequenceElement.elements(null, WSConstants.ELEMENT_TAG)) {
                WSComponentPort componentPort = new WSComponentPort(element, typesTargetNamespace, this);
                // Check if the type is defined in types
                QName paramType = componentPort.getType();
                if (!(WSConstants.XSD_NS_URI.equalsIgnoreCase(paramType.getNamespaceURI()))) {
                    XmlElement typeDefinition = WSDLUtil.getTypeDefinition(this.wsdl, paramType);
                    if (typeDefinition == null) {
                        throw new ComponentException("could not find definition for type " + paramType + " in "
                                + this.wsdlQName);
                    }
                }
                parts.add(componentPort);
            }
        }
    }

    /**
     * @param typeName
     * @param schemaElement
     * @return The XmlElement
     */
    private XmlElement findElementElement(String typeName, XmlElement schemaElement) {
        for (XmlElement element : schemaElement.elements(null, WSConstants.ELEMENT_TAG)) {
            String elementName = element.attributeValue(WSConstants.NAME_ATTRIBUTE);
            if (typeName.equals(elementName)) {
                return element;
            }
        }
        return null;
    }

    /**
     * @param typeName
     * @param schemaElement
     * @return The XmlElement
     */
    private XmlElement findTypeElement(String typeName, XmlElement schemaElement) {
        // Only supports complexType now
        for (XmlElement complexTypeElement : schemaElement.elements(null, WSConstants.COMPLEX_TYPE_TAG)) {
            String elementName = complexTypeElement.attributeValue(WSConstants.NAME_ATTRIBUTE);
            if (typeName.equals(elementName)) {
                return complexTypeElement;
            }
        }
        return null;
    }
}