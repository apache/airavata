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

package org.apache.airavata.xbaya.graph.system;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.common.exception.UtilsException;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.graph.DataEdge;
import org.apache.airavata.xbaya.graph.DataPort;
import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.GraphSchema;
import org.apache.airavata.xbaya.graph.Port;
import org.xmlpull.infoset.XmlElement;
import org.xmlpull.infoset.XmlNamespace;

abstract public class ParameterNode extends SystemNode {

    private static final String NAME_TAG = "name";

    private static final String DESCRIPTION_TAG = "description";

    private static final String DATA_TYPE_QNAME_TAG = "dataType";

    private static final String METADATA_TAG = "metadata";

    private boolean configured;

    /**
     * Type of the parameter (e.g. xsd:string, xsd:int)
     */
    private QName parameterType;

    private String configuredName;

    private String description;

    private XmlElement metadata;

    /**
     * Constructs a ParameterNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public ParameterNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
    }

    /**
     * Constructs a ParameterNode.
     * 
     * @param graph
     * 
     */
    public ParameterNode(Graph graph) {
        super(graph);
    }

    /**
     * @see org.apache.airavata.xbaya.graph.impl.NodeImpl#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        super.setName(name);
        // Creates the ID to the new one based on the new name. This is
        // for readability of workflow scripts.
        createID();
    }

    /**
     * @param parameterType
     */
    public void setParameterType(QName parameterType) {
        this.parameterType = parameterType;
    }

    /**
     * Returns the type of the parameter
     * 
     * @return The type of the parameter (e.g. string, int)
     */
    public QName getParameterType() {
        return this.parameterType;
    }

    /**
     * Returns the configuredName.
     * 
     * @return The configuredName
     */
    public String getConfiguredName() {
        return this.configuredName;
    }

    /**
     * Sets configuredName.
     * 
     * @param configuredName
     *            The configuredName to set.
     */
    public void setConfiguredName(String configuredName) {
        this.configuredName = configuredName;
        setName(configuredName);
    }

    /**
     * Returns the description.
     * 
     * @return The description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets description.
     * 
     * @param description
     *            The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param metadata
     */
    public void setMetadata(XmlElement metadata) {
        if (metadata == null) {
            this.metadata = null;
            return;
        }

        // clone and detach from the parent
        try {
            this.metadata = XMLUtil.deepClone(metadata);
        } catch (UtilsException e) {
            e.printStackTrace();
        }

        // Reformat
        List<String> emptyTexts = new ArrayList<String>();
        for (Object child : this.metadata.children()) {
            if (child instanceof XmlElement) {
                XmlElement element = (XmlElement) child;
                for (XmlNamespace ns : element.namespaces()) {
                    // move the namespace declaration up if possible.
                    if (this.metadata.lookupNamespaceByPrefix(ns.getPrefix()) == null) {
                        // If this prefix is not used yet, copy to the root.
                        this.metadata.declareNamespace(ns);
                    }
                }
            } else if (child instanceof String) {
                String text = (String) child;
                // Remove the white spaces.
                if (text.trim().length() == 0) {
                    // We cannot remove it in the iterator.
                    emptyTexts.add(text);
                }
            }
        }
        for (String text : emptyTexts) {
            this.metadata.removeChild(text);
        }
    }

    /**
     * @return The metadata
     */
    public XmlElement getMetadata() {
        return this.metadata;
    }

    /**
     * @param configured
     */
    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    /**
     * @return true if configured; false otherwise.
     */
    public boolean isConfigured() {
        return this.configured;
    }

    /**
     * Checks if this InputNode is connected.
     * 
     * @return true if this InputNode is connected; false otherwise;
     */
    public boolean isConnected() {
        if (getEdges().size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the port of this ParameterNode.
     * 
     * Note that a ParameterNode always has only one port.
     * 
     * @return The port
     */
    abstract public SystemDataPort getPort();

    /**
     * Returns the first port that this node is connected to/from.
     * 
     * @return The first port that this node is connected to/from
     */
    abstract public Port getConnectedPort();

    @Override
    protected void parseConfiguration(XmlElement configElement) {
        XmlElement nameElement = configElement.element(null, NAME_TAG);
        if (nameElement != null) {
            // If the name is set here, this node has been configured.
            this.configured = true;
            this.configuredName = nameElement.requiredText();
        }
        XmlElement descElement = configElement.element(null, DESCRIPTION_TAG);
        if (descElement != null) {
            this.description = descElement.requiredText();
        }
        XmlElement typeElement = configElement.element(null, DATA_TYPE_QNAME_TAG);
        if (typeElement != null) {
            String qnameText = typeElement.requiredText();
            if (qnameText != null && !qnameText.equals("")) {
                this.parameterType = QName.valueOf(qnameText);
            }
        }
        XmlElement metadataElement = configElement.element(null, METADATA_TAG);
        if (metadataElement != null) {
            for (XmlElement appinfo : metadataElement.requiredElementContent()) {
                // Call setMetadata to clone and reformat.
                setMetadata(appinfo);
                // It should have only one element.
                break;
            }
        }
    }

    @Override
    protected XmlElement addConfigurationElement(XmlElement nodeElement) {

        XmlElement configElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_CONFIG_TAG);

        if (this.configured) {
            // Don't save the name here if this node has not been configured.
            XmlElement nameElement = configElement.addElement(GraphSchema.NS, NAME_TAG);
            nameElement.addChild(this.configuredName);
        }
        if (this.description != null) {
            XmlElement descriptionElement = configElement.addElement(GraphSchema.NS, DESCRIPTION_TAG);
            descriptionElement.addChild(this.description);
        }
        if (this.parameterType != null) {
            XmlElement qnameElement = configElement.addElement(GraphSchema.NS, DATA_TYPE_QNAME_TAG);
            qnameElement.addChild(this.parameterType.toString());
        }
        if (this.metadata != null) {
            XmlElement metadataElement = configElement.addElement(GraphSchema.NS, METADATA_TAG);
            // Clone the metadata to avoid parent problem because this can be
            // called multiple times.
            try {
                metadataElement.addChild(XMLUtil.deepClone(this.metadata));
            } catch (UtilsException e) {
                e.printStackTrace();
            }
        }

        return configElement;
    }

    protected List<DataEdge> getEdges() {
        DataPort port = getPort();
        List<DataEdge> edges = port.getEdges();
        return edges;
    }
}