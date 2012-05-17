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

package org.apache.airavata.workflow.model.component.ws;

import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.WSConstants;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.component.ComponentDataPort;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.graph.ws.WSPort;
import org.xmlpull.infoset.XmlElement;
import org.xmlpull.infoset.XmlNamespace;

import xsul5.XmlConstants;

public class WSComponentPort extends ComponentDataPort {

    /**
     * default
     */
    public static final String DEFAULT = "default";

    private WSComponent component;

    private XmlElement elementElement;

    private Object value;

    private String defaultValue;

    private String targetNamespace;

    private boolean schemaUsed;

    private XmlElement annotation;

    private XmlElement appinfo;

    private boolean optional;

    /**
     * Creates WSComponentPort
     * 
     * @param name
     *            The name
     * @param type
     *            The type
     * @param component
     */
    public WSComponentPort(String name, QName type, WSComponent component) {
        super(name);
        this.component = component;
        this.type = type;
        this.schemaUsed = false;
    }

    protected WSComponentPort(XmlElement element, String targetNamespace, WSComponent component)
            throws ComponentException {
        this.component = component;
        this.elementElement = element;
        this.targetNamespace = targetNamespace;
        this.schemaUsed = true;
        parse(element);
    }

    /**
     * @return The component
     */
    public WSComponent getComponent() {
        return this.component;
    }

    /**
     * @return The port created.
     */
    @Override
    public WSPort createPort() {
        WSPort port = new WSPort();
        port.setName(this.name);
        // port.setTypeQName(this.type);
        port.setComponentPort(this);
        return port;
    }

    /**
     * @return true if schema is used; false otherwise.
     */
    public boolean isSchemaUsed() {
        return this.schemaUsed;
    }

    /**
     * @return The targetNamespace
     */
    public String getTargetNamespace() {
        return this.targetNamespace;
    }

    /**
     * @return The element.
     */
    public XmlElement getElement() {
        return this.elementElement;
    }

    /**
     * Returns the defaultValue.
     * 
     * @return The defaultValue
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Sets defaultValue.
     * 
     * @param defaultValue
     *            The defaultValue to set.
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the value.
     * 
     * @return The value. The value is either String (or any other object possibly) or XmlElement.
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Sets value.
     * 
     * @param value
     *            The value to set. The value is either String (or any other object possibly) or XmlElement.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * @return The appinfo element.
     */
    public XmlElement getAppinfo() {
        return this.appinfo;
    }

    /**
     * @return true if it's optional; false otherwise.
     */
    public boolean isOptional() {
        return this.optional;
    }

    /**
     * @param element
     * @throws ComponentException
     */
    private void parse(XmlElement element) throws ComponentException {
        this.name = element.attributeValue(WSConstants.NAME_ATTRIBUTE);

        // type
        String typeQNameString = element.attributeValue(WSConstants.TYPE_ATTRIBUTE);
        if (typeQNameString == null) {
            // Type might be defined inline.
            // TODO fix this.
            this.type = WSConstants.XSD_ANY_TYPE;
        } else {
            String typeName = XMLUtil.getLocalPartOfQName(typeQNameString);
            String prefix = XMLUtil.getPrefixOfQName(typeQNameString);
            XmlNamespace namespace = null;
            if (prefix == null) {
                if ("string".equals(typeName) || "int".equals(typeName)) {
                    namespace = XmlConstants.BUILDER.newNamespace("xsd", WSConstants.XSD_NS_URI);
                    prefix = "xsd";
                } else {
                    throw new ComponentException("Namespace prefix, " + prefix + ", is not defined");
                }
            } else {
                namespace = element.lookupNamespaceByPrefix(prefix);
            }
            this.type = new QName(namespace.getName(), typeName, prefix);
        }

        // annotation
        this.annotation = element.element(null, WSConstants.ANNOTATION_TAG);
        if (this.annotation != null) {
            XmlElement documentationElement = this.annotation.element(null, WSConstants.DOCUMENTATION_TAG);
            if (documentationElement != null) {
                // Sets the documentation.
                this.description = documentationElement.requiredText();
            }
            this.appinfo = this.annotation.element(null, WSConstants.APPINFO_TAG);
        }

        // defaut value
        this.defaultValue = element.attributeValue(WSConstants.DEFAULT_ATTRIBUTE);
        if (this.defaultValue == null && this.annotation != null) {
            XmlElement defaultElement = this.annotation.element(null, DEFAULT);
            if (defaultElement != null) {
                for (Object child : defaultElement.children()) {
                    if (child instanceof XmlElement) {
                        this.defaultValue = XMLUtil.xmlElementToString((XmlElement) child);
                    }
                }
            }
        }

        // minOccurs
        String minOccurs = element.attributeValue(WSConstants.MIN_OCCURS_ATTRIBUTE);
        if ("0".equals(minOccurs)) {
            this.optional = true;
        } else {
            this.optional = false;
        }
    }
}