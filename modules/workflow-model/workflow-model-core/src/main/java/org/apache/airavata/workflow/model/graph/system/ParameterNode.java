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
package org.apache.airavata.workflow.model.graph.system;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.graph.DataEdge;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.Port;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;
import org.xmlpull.infoset.XmlNamespace;

abstract public class ParameterNode extends SystemNode {

    private static final String NAME_TAG = "name";

    private static final String DESCRIPTION_TAG = "description";

    private static final String DATA_TYPE_QNAME_TAG = "dataType";

    private static final String METADATA_TAG = "metadata";

    private boolean configured;

    private static final Logger log = LoggerFactory.getLogger(ParameterNode.class);

    /**
     * Type of the parameter (e.g. xsd:string, xsd:int)
     */
    private DataType parameterType;

    private String configuredName;

    private String description;

    private XmlElement metadata;
    private JsonObject metadataJson;

    /**
     * Constructs a ParameterNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public ParameterNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
    }

    public ParameterNode(JsonObject nodeObject) throws GraphException {
        super(nodeObject);
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
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#setName(java.lang.String)
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
    public void setParameterType(DataType parameterType) {
        this.parameterType = parameterType;
    }

    /**
     * Returns the type of the parameter
     * 
     * @return The type of the parameter (e.g. string, int)
     */
    public DataType getParameterType() {
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
        } catch (AiravataException e) {
            log.error(e.getMessage(), e);
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

    public void setMetadataJson(JsonObject metadata1) {
        this.metadataJson = metadata1;
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
                this.parameterType = DataType.valueOf(qnameText);
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

    protected void parseConfiguration(JsonObject configObject) {
        JsonElement nameElement = configObject.get(NAME_TAG);
        if (nameElement != null) {
            this.configured = true;
            this.configuredName = nameElement.getAsString();
        }
        JsonElement descriptionElement = configObject.get(DESCRIPTION_TAG);
        if (descriptionElement != null) {
            this.description = descriptionElement.getAsString();
        }
        JsonElement typeElement = configObject.get(DATA_TYPE_QNAME_TAG);
        if (typeElement != null) {
            this.parameterType = DataType.valueOf(typeElement.getAsString());
        }
        JsonElement metadataElement = configObject.get(METADATA_TAG);
        if (metadataElement != null) {
            JsonObject metaObject = (JsonObject) metadataElement;
            JsonElement appInfoElement = metaObject.get("appinfo");
            if (appInfoElement != null) {
                setMetadataJson((JsonObject) appInfoElement);
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
            } catch (AiravataException e) {
                log.error(e.getMessage(), e);
            }
        }

        return configElement;
    }

    @Override
    protected JsonObject addConfigurationElement(JsonObject nodeObject) {
        JsonObject configObject = new JsonObject();

        if (this.configured) {
            // Don't save the name here if this node has not been configured.
            configObject.addProperty(NAME_TAG, this.configuredName);
        }
        if (this.description != null) {
            configObject.addProperty(DESCRIPTION_TAG, this.description);
        }
        if (this.parameterType != null) {
            configObject.addProperty(DATA_TYPE_QNAME_TAG, this.parameterType.toString());
        }
        if (this.metadata != null) {
            configObject.add(METADATA_TAG, this.metadataJson);
        }
        nodeObject.add(GraphSchema.NODE_CONFIG_TAG, configObject);

        return configObject;
    }

    protected List<DataEdge> getEdges() {
        DataPort port = getPort();
        List<DataEdge> edges = port.getEdges();
        return edges;
    }
}