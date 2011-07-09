/*
 * Copyright (c) 2006-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: WSDLUtil.java,v 1.16 2009/02/02 07:05:14 cherath Exp $
 */
package org.apache.airavata.xbaya.util;

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

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.mylead.MyLead;
import org.apache.airavata.xbaya.wf.Workflow;
import org.xmlpull.infoset.XmlAttribute;
import org.xmlpull.infoset.XmlBuilderException;
import org.xmlpull.infoset.XmlElement;
import org.xmlpull.infoset.XmlNamespace;

import xsul.XmlConstants;
import xsul.lead.LeadContextHeader;
import xsul.lead.LeadResourceMapping;
import xsul5.MLogger;
import xsul5.wsdl.WsdlBinding;
import xsul5.wsdl.WsdlDefinitions;
import xsul5.wsdl.WsdlPortType;
import xsul5.wsdl.WsdlPortTypeOperation;
import xsul5.wsdl.WsdlUtil;

/**
 * @author Satoshi Shirasuna
 */
public class WSDLUtil {

    private static final MLogger logger = MLogger.getLogger();

    /**
     * @param wsdlString
     * @return The WSDL
     * @throws ComponentException
     */
    public static WsdlDefinitions stringToWSDL(String wsdlString) throws ComponentException {
        try {
            XmlElement wsdlElement = XMLUtil.stringToXmlElement(wsdlString);
            WsdlDefinitions definitions = new WsdlDefinitions(wsdlElement);
            return definitions;
        } catch (RuntimeException e) {
            throw new ComponentException(e);
        }
    }

    /**
     * @param definitions3
     * @return The WsdlDefinitions (XSUL5)
     */
    public static xsul5.wsdl.WsdlDefinitions wsdlDefinitions3ToWsdlDefintions5(xsul.wsdl.WsdlDefinitions definitions3) {

        return new xsul5.wsdl.WsdlDefinitions(XMLUtil.xmlElement3ToXmlElement5(definitions3));
    }

    /**
     * @param definitions5
     * @return The WsdlDefinitions (XSUL3)
     */
    public static xsul.wsdl.WsdlDefinitions wsdlDefinitions5ToWsdlDefintions3(xsul5.wsdl.WsdlDefinitions definitions5) {

        return new xsul.wsdl.WsdlDefinitions(XMLUtil.xmlElement5ToXmlElement3(definitions5.xml()));
    }

    /**
     * @param definitions
     * @return The name of the WSDL.
     */
    public static String getWSDLName(WsdlDefinitions definitions) {
        String wsdlName = definitions.xml().attributeValue(WSConstants.NAME_ATTRIBUTE);
        if (wsdlName == null) {
            // name is optional.
            wsdlName = "";
        }
        return wsdlName;
    }

    /**
     * @param definitions
     * @return The QName of the WSDL.
     */
    public static QName getWSDLQName(WsdlDefinitions definitions) {
        String targetNamespace = definitions.getTargetNamespace();
        String wsdlName = getWSDLName(definitions);
        return new QName(targetNamespace, wsdlName);
    }

    /**
     * @param definitions
     * @return The first portType
     * @throws ComponentException
     */
    public static WsdlPortType getFirstPortType(WsdlDefinitions definitions) throws ComponentException {
        for (WsdlPortType portType : definitions.portTypes()) {
            return portType;
        }
        throw new ComponentException("No portType is defined in WSDL");
    }

    public static WsdlPortTypeOperation getFirstOperation(WsdlDefinitions definitions) throws ComponentException {
        for (WsdlPortTypeOperation operation : getFirstPortType(definitions).operations()) {
            return operation;
        }
        throw new ComponentException("No portType is defined in WSDL");
    }

    /**
     * @param definitions
     * @return The QName of the first portType.
     * @throws ComponentException
     */
    public static QName getFirstPortTypeQName(WsdlDefinitions definitions) throws ComponentException {
        String targetNamespace = definitions.getTargetNamespace();
        for (WsdlPortType portType : definitions.portTypes()) {
            String portTypeName = portType.getName();
            QName portTypeQName = new QName(targetNamespace, portTypeName);
            return portTypeQName;
        }
        throw new ComponentException("No portType is defined.");
    }

    /**
     * @param definitions
     * @param portTypeQName
     * @return The name of the first operation in a given portType.
     * @throws ComponentException
     */
    public static String getFirstOperationName(WsdlDefinitions definitions, QName portTypeQName)
            throws ComponentException {
        WsdlPortType portType = definitions.getPortType(portTypeQName.getLocalPart());
        for (WsdlPortTypeOperation operation : portType.operations()) {
            String operationName = operation.getOperationName();

            // XXX Temporary solution to skip some GFac specific operations.
            if ("Shutdown".equals(operationName)) {
                continue;
            } else if ("Kill".equals(operationName)) {
                continue;
            } else if ("Ping".equals(operationName)) {
                continue;
            }

            return operationName;
        }
        throw new ComponentException("No operation is defined");
    }

    /**
     * @param definitions
     * @return The cloned WsdlDefinitions
     */
    public static WsdlDefinitions deepClone(WsdlDefinitions definitions) {
        return new WsdlDefinitions(XMLUtil.deepClone(definitions.xml()));
    }

    /**
     * @param definitions
     * @param paramType
     * @return The schema that includes the type definition
     */
    public static XmlElement getSchema(WsdlDefinitions definitions, QName paramType) {
        XmlElement types = definitions.getTypes();

        Iterable<XmlElement> schemas = types.elements(WSConstants.XSD_NS, WSConstants.SCHEMA_TAG);
        for (XmlElement schema : schemas) {
            if (isTypeDefinedInSchema(paramType, schema)) {
                return schema;
            }
        }

        // ok we didnt find the type in the schema in first level
        // now we try try to see if it exist in schema imports.
        // we loop in two step because its better to avoid the network
        // connection if possible
        for (XmlElement schema : schemas) {
            Iterable<XmlElement> imports = schema.elements(WSConstants.XSD_NS, WSConstants.IMPORT_TAG);
            for (XmlElement importEle : imports) {
                String schemaLocation = importEle.attributeValue(WSConstants.SCHEMA_LOCATION_ATTRIBUTE);
                if (null != schemaLocation && !"".equals(schemaLocation)) {
                    try {
                        // connect using a url connection
                        URL url = new URL(schemaLocation);
                        URLConnection connection = url.openConnection();
                        connection.connect();
                        XmlElement importedSchema = xsul5.XmlConstants.BUILDER.parseFragmentFromInputStream(connection
                                .getInputStream());
                        if (isTypeDefinedInSchema(paramType, importedSchema)) {
                            // still return the parent schema
                            return schema;
                        }
                    } catch (MalformedURLException e) {
                        throw new XBayaRuntimeException(e);
                    } catch (XmlBuilderException e) {
                        throw new XBayaRuntimeException(e);
                    } catch (IOException e) {
                        throw new XBayaRuntimeException(e);
                    }
                }
            }
        }

        return null;
    }

    private static boolean isTypeDefinedInSchema(QName paramType, XmlElement schema) {
        String schemaTargetNamespace = schema.attributeValue(WSConstants.TARGET_NAMESPACE_ATTRIBUTE);
        if (schemaTargetNamespace.equals(paramType.getNamespaceURI())) {
            for (XmlElement complexType : schema.elements(WSConstants.XSD_NS, WSConstants.COMPLEX_TYPE_TAG)) {
                String complexTypeName = complexType.attributeValue(WSConstants.NAME_ATTRIBUTE);
                if (complexTypeName.equals(paramType.getLocalPart())) {
                    return true;
                }
            }
            for (XmlElement simpleType : schema.elements(WSConstants.XSD_NS, WSConstants.SIMPLE_TYPE_TAG)) {
                String simpleTypeName = simpleType.attributeValue(WSConstants.NAME_ATTRIBUTE);
                if (simpleTypeName.equals(paramType.getLocalPart())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param definitions
     * @param paramType
     * @return The type definition
     */
    public static XmlElement getTypeDefinition(WsdlDefinitions definitions, QName paramType) {
        XmlElement types = definitions.getTypes();
        XmlElement returnType = null;
        types.element(null, WSConstants.SCHEMA_TAG);
        Iterable<XmlElement> schemas = types.elements(null, WSConstants.SCHEMA_TAG);
        for (XmlElement schema : schemas) {

            returnType = findTypeInSchema(paramType, schema);
            if (returnType != null) {
                return returnType;
            }
        }
        // ok we didnt find the type in the schemas
        // try to find it in the schema imports.

        // if not found it will return null so we would return null
        return findTypeDefinitionInImports(definitions, paramType);

    }

    /**
     * @param serviceWSDL
     * @param paramType
     * @return
     */
    public static XmlElement getImportContainingTypeDefinition(WsdlDefinitions definitions, QName paramType) {
        XmlElement types = definitions.getTypes();
        XmlElement returnType = null;
        Iterable<XmlElement> schemas = types.elements(WSConstants.XSD_NS, WSConstants.SCHEMA_TAG);
        for (XmlElement schema : schemas) {
            Iterable<XmlElement> imports = schema.elements(WSConstants.XSD_NS, WSConstants.IMPORT_TAG);
            for (XmlElement importEle : imports) {
                String schemaLocation = importEle.attributeValue(WSConstants.SCHEMA_LOCATION_ATTRIBUTE);
                if (null != schemaLocation && !"".equals(schemaLocation)) {
                    try {
                        // connect using a url connection
                        URL url = new URL(schemaLocation);
                        URLConnection connection = url.openConnection();
                        connection.connect();
                        XmlElement importedSchema = xsul5.XmlConstants.BUILDER.parseFragmentFromInputStream(connection
                                .getInputStream());
                        returnType = findTypeInSchema(paramType, importedSchema);
                        if (returnType != null) {
                            return importEle;
                        }

                    } catch (MalformedURLException e) {
                        throw new XBayaRuntimeException(e);
                    } catch (XmlBuilderException e) {
                        throw new XBayaRuntimeException(e);
                    } catch (IOException e) {
                        throw new XBayaRuntimeException(e);
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param serviceWSDL
     * @param paramType
     */
    public static XmlElement findTypeDefinitionInImports(WsdlDefinitions definitions, QName paramType) {
        XmlElement types = definitions.getTypes();
        XmlElement returnType = null;
        Iterable<XmlElement> schemas = types.elements(null, WSConstants.SCHEMA_TAG);
        for (XmlElement schema : schemas) {
            Iterable<XmlElement> imports = schema.elements(WSConstants.XSD_NS, WSConstants.IMPORT_TAG);
            for (XmlElement importEle : imports) {
                String schemaLocation = importEle.attributeValue(WSConstants.SCHEMA_LOCATION_ATTRIBUTE);
                if (null != schemaLocation && !"".equals(schemaLocation)) {
                    try {
                        // connect using a url connection
                        URL url = new URL(schemaLocation);
                        URLConnection connection = url.openConnection();
                        connection.connect();
                        XmlElement importedSchema = xsul5.XmlConstants.BUILDER.parseFragmentFromInputStream(connection
                                .getInputStream());
                        returnType = findTypeInSchema(paramType, importedSchema);
                        if (returnType != null) {
                            return returnType;
                        }

                    } catch (MalformedURLException e) {
                        throw new XBayaRuntimeException(e);
                    } catch (XmlBuilderException e) {
                        throw new XBayaRuntimeException(e);
                    } catch (IOException e) {
                        throw new XBayaRuntimeException(e);
                    }
                }
            }
        }
        return null;

    }

    private static XmlElement findTypeInSchema(QName paramType, XmlElement schema) {
        String schemaTargetNamespace = schema.attributeValue(WSConstants.TARGET_NAMESPACE_ATTRIBUTE);
        if (null != schemaTargetNamespace && schemaTargetNamespace.equals(paramType.getNamespaceURI())) {
            for (XmlElement complexType : schema.elements(WSConstants.XSD_NS, WSConstants.COMPLEX_TYPE_TAG)) {
                String complexTypeName = complexType.attributeValue(WSConstants.NAME_ATTRIBUTE);
                if (complexTypeName.equals(paramType.getLocalPart())) {
                    return complexType;

                }
            }
            for (XmlElement simpleType : schema.elements(WSConstants.XSD_NS, WSConstants.SIMPLE_TYPE_TAG)) {
                String simpleTypeName = simpleType.attributeValue(WSConstants.NAME_ATTRIBUTE);
                if (simpleTypeName.equals(paramType.getLocalPart())) {
                    return simpleType;
                }
            }
        }
        return null;
    }

    /**
     * @param wsdl
     * @return true if the WSDL is AWSDL; false otherwise.
     */
    public static boolean isAWSDL(WsdlDefinitions wsdl) {
        if (wsdl.services().iterator().hasNext()) {
            return false;
        }
        return true;
    }

    /**
     * @param definitions
     * @return true if the service supports asynchronous invocation; false otherwise;
     */
    public static boolean isAsynchronousSupported(WsdlDefinitions definitions) {
        for (WsdlBinding binding : definitions.bindings()) {
            XmlElement element = binding.xml().element(WSConstants.USING_ADDRESSING_TAG);
            if (element != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a specified AWSDL to CWSDL using DSC URI.
     * 
     * @param definitions
     *            The specified AWSDL. This will be modified.
     * @param url
     *            The URL of the service
     * @return The CWSDL converted.
     */
    public static WsdlDefinitions convertToCWSDL(WsdlDefinitions definitions, URI url) {
        for (WsdlPortType portType : definitions.portTypes()) {
            WsdlUtil.createCWSDL(definitions, portType, url);
        }
        return definitions;
    }

    /**
     * @param uri
     * @return The URI with "?wsdl" at the end.
     */
    public static String appendWSDLQuary(String uri) {
        URI wsdlURI = appendWSDLQuary(URI.create(uri));
        return wsdlURI.toString();
    }

    public static List<XmlNamespace> getNamespaces(XmlElement element) {
        LinkedList<XmlNamespace> namespaces = new LinkedList<XmlNamespace>();
        namespaces.add(element.getNamespace());
        Iterable<XmlAttribute> attributes = element.attributes();
        for (XmlAttribute xmlAttribute : attributes) {
            if (xmlAttribute.getNamespace() != null && !namespaces.contains(xmlAttribute.getNamespace())) {
                namespaces.add(xmlAttribute.getNamespace());
            }
            int index = xmlAttribute.getValue().indexOf(':');
            if (-1 != index) {
                String prefix = xmlAttribute.getValue().substring(0, index);
                if (element.lookupNamespaceByPrefix(prefix) != null) {
                    namespaces.add(element.lookupNamespaceByPrefix(prefix));
                }
            }
        }
        Iterable children = element.children();
        for (Object object : children) {
            if (object instanceof XmlElement) {
                List<XmlNamespace> newNSs = getNamespaces((XmlElement) object);
                for (XmlNamespace xmlNamespace : newNSs) {
                    if (!namespaces.contains(xmlNamespace)) {
                        namespaces.add(xmlNamespace);
                    }
                }
            }
        }
        return namespaces;
    }

    /**
     * @param uri
     * @return The URI with "?wsdl" at the end.
     */
    public static URI appendWSDLQuary(URI uri) {
        if (uri.toString().endsWith("?wsdl")) {
            logger.warning("URL already has ?wsdl at the end: " + uri.toString());
            // Don't throw exception to be more error tolerant.
            return uri;
        }
        String path = uri.getPath();
        if (path == null || path.length() == 0) {
            uri = uri.resolve("/");
        }
        uri = URI.create(uri.toString() + "?wsdl");
        return uri;
    }

    public static LeadContextHeader buildLeadContextHeader(final XBayaEngine engine,
            MonitorConfiguration monitorConfiguration, String nodeId, LeadResourceMapping resourceMapping)
            throws URISyntaxException {

        XBayaConfiguration configuration = engine.getConfiguration();
        Workflow workflow = engine.getWorkflow();

        LeadContextHeader leadContext = buildLeadContextHeader(workflow, configuration, engine.getMyLead(),
                monitorConfiguration, nodeId, resourceMapping);

        return leadContext;

    }

    /**
     * @param engine
     * @param monitorConfiguration
     * @param nodeId
     * @param resourceMapping
     * @param configuration
     * @param workflow
     * @param myLead
     * @return
     * @throws URISyntaxException
     */
    public static LeadContextHeader buildLeadContextHeader(Workflow workflow, XBayaConfiguration configuration,
            MyLead myLead, MonitorConfiguration monitorConfiguration, String nodeId, LeadResourceMapping resourceMapping)
            throws URISyntaxException {
        LeadContextHeaderHelper leadContextHelper = new LeadContextHeaderHelper();
        leadContextHelper.setXBayaConfiguration(configuration);
        leadContextHelper.setMyLeadConfiguration(myLead.getConfiguration());

        leadContextHelper.setWorkflowInstanceID(workflow.getGPELInstanceID());
        leadContextHelper.setWorkflowTemplateID(workflow.getUniqueWorkflowName());

        leadContextHelper.setMonitorConfiguration(monitorConfiguration);

        LeadContextHeader leadContext = leadContextHelper.getLeadContextHeader();

        leadContext.setNodeId(nodeId);

        leadContext.setTimeStep("1");
        leadContext.setXRegistryUrl(new URI(configuration.getXRegistryURL().toString() + "?wsdl"));

        if (resourceMapping != null) {
            leadContext.setResourceMapping(resourceMapping);
        }
        return leadContext;
    }

    /**
     * @param valueElement
     * @return
     */
    public static org.xmlpull.v1.builder.XmlElement xmlElement5ToXmlElementv1(XmlElement valueElement) {

        return XmlConstants.BUILDER.parseFragmentFromReader(new StringReader(xsul5.XmlConstants.BUILDER
                .serializeToStringPretty(valueElement)));
    }

    /**
     * @param values
     */
    public static <T extends Object> T getfirst(Iterable<T> vals) {
        for (T class1 : vals) {
            return class1;
        }
        throw new RuntimeException("Iterator empty");

    }

    /**
     * @param serviceSchema
     */
    public static void print(XmlElement serviceSchema) {
        System.out.println(xsul5.XmlConstants.BUILDER.serializeToStringPretty(serviceSchema));
    }

    /**
     * @param workflowID
     * @return
     */
    public static String findWorkflowName(URI workflowID) {
        String[] splits = workflowID.toString().split("/");
        return splits[splits.length - 1];

    }

    /**
     * 
     * @param element
     * @param name
     * @param oldValue
     * @param newValue
     */
    public static void replaceAttributeValue(XmlElement element, String name, String oldValue, String newValue) {
        XmlAttribute attribute = element.attribute(name);
        if (null != attribute && oldValue.equals(attribute.getValue())) {
            element.removeAttribute(attribute);
            element.setAttributeValue(name, newValue);
        }
        Iterable iterator = element.children();
        for (Object object : iterator) {
            if (object instanceof XmlElement) {
                replaceAttributeValue((XmlElement) object, name, oldValue, newValue);
            }
        }

    }

    public static boolean attributeExist(XmlElement element, String name, String value) {
        System.out.println(xsul5.XmlConstants.BUILDER.serializeToStringPretty(element));
        XmlAttribute attribute = element.attribute(name);
        if (null != attribute && value.equals(attribute.getValue())) {
            return true;
        }
        Iterable iterator = element.children();
        boolean ret = false;
        for (Object object : iterator) {
            if (object instanceof XmlElement) {
                ret = ret || attributeExist((XmlElement) object, name, value);
            }
        }
        return ret;

    }

}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2006-2007 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */
