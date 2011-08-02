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

package org.apache.airavata.xbaya.graph.dynamic;

import java.util.Collection;

import org.apache.airavata.xbaya.component.dynamic.CombineMultipleStreamComponent;
import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.Port;
import org.apache.airavata.xbaya.graph.dynamic.gui.CombineMultipleStreamNodeGUI;
import org.apache.airavata.xbaya.graph.impl.PortImpl;
import org.xmlpull.infoset.XmlElement;

public class CombineMultipleStreamNode extends CepNode {

    public CombineMultipleStreamNode(Graph graph) {
        super(graph);
        Collection<PortImpl> allPorts = this.getAllPorts();
        for (Port port : allPorts) {
            ((CepPort) port).setNode(this);
        }
    }

    /**
     * Constructs a CepNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public CombineMultipleStreamNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
        this.setComponent(new CombineMultipleStreamComponent());
    }

    /**
     * @see org.apache.airavata.xbaya.graph.Node#getGUI()
     */
    public synchronized CombineMultipleStreamNodeGUI getGUI() {
        if (this.gui == null) {
            this.gui = new CombineMultipleStreamNodeGUI(this);
        }
        return (CombineMultipleStreamNodeGUI) this.gui;
    }

}