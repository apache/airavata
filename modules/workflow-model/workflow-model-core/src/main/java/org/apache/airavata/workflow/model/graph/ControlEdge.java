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
package org.apache.airavata.workflow.model.graph;

import com.google.gson.JsonObject;
import org.apache.airavata.workflow.model.graph.impl.EdgeImpl;
import org.xmlpull.infoset.XmlElement;

public class ControlEdge extends EdgeImpl {

    /**
     * Constructs a ControlEdge.
     * 
     */
    public ControlEdge() {
        super();
    }

    /**
     * Constructs a ControlEdge.
     * 
     * @param edgeXml
     */
    public ControlEdge(XmlElement edgeXml) {
        super(edgeXml);
    }

    public ControlEdge(JsonObject edgeObject) {
        super(edgeObject);
    }
    /**
     * @see org.apache.airavata.workflow.model.graph.impl.EdgeImpl#toXML()
     */
    @Override
    protected XmlElement toXML() {
        XmlElement edgeElement = super.toXML();
        edgeElement.setAttributeValue(GraphSchema.NS, GraphSchema.EDGE_TYPE_ATTRIBUTE, GraphSchema.EDGE_TYPE_CONTROL);
        return edgeElement;
    }

    protected JsonObject toJSON() {
        JsonObject edgeObject = super.toJSON();
        edgeObject.addProperty(GraphSchema.EDGE_TYPE_ATTRIBUTE, GraphSchema.EDGE_TYPE_CONTROL);
        return edgeObject;
    }
}