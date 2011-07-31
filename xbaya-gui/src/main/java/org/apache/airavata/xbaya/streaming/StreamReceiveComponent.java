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

package org.apache.airavata.xbaya.streaming;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.ws.WSComponent;
import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.Node;

import xsul5.wsdl.WsdlDefinitions;

public class StreamReceiveComponent extends WSComponent {

    public static final String NAME = "StreamReceiver";

    public StreamReceiveComponent(WsdlDefinitions wsdl) throws ComponentException {
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
     * @throws ComponentException
     */
    public StreamReceiveComponent(WsdlDefinitions wsdl, QName portTypeQName, String operationName)
            throws ComponentException {
        super(wsdl, portTypeQName, operationName);
    }

    public Node createNode(Graph graph) {
        return createNode(graph, new StreamReceiveNode(graph));
    }

}