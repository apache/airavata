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

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.utils.WorkflowConstants;
import org.xmlpull.infoset.XmlNamespace;

public class GraphSchema {

    /**
     * Namespace prefix
     */
    public static final String NS_PREFIX_XGR = "xgr";

    /**
     * Namespace URI
     */
    public static final String NS_URI_XGR = WorkflowConstants.NS_URI_XBAYA + "graph";

    /**
     * Namespace
     */
    public static final XmlNamespace NS = XMLUtil.BUILDER.newNamespace(GraphSchema.NS_PREFIX_XGR,
            GraphSchema.NS_URI_XGR);

    /**
     * The attribute for the version of the XBaya that creates the workflow.
     */
    public static final String XBAYA_VERSION_ATTRIBUTE = "version";

    /**
     * GRAPH_TAG
     */
    public static final String GRAPH_TAG = "graph";

    /**
     * GRAPH_TYPE_ATTRIBUTE
     */
    public static final String GRAPH_TYPE_ATTRIBUTE = "type";

    /**
     * GRAPH_TYPE_WS
     */
    public static final String GRAPH_TYPE_WS = "ws";

    /**
     * The tag for the ID of a graph.
     */
    public static final String GRAPH_ID_TAG = "id";

    /**
     * The tag for the name of a graph.
     */
    public static final String GRAPH_NAME_TAG = "name";

    /**
     * The tag for the description of a graph.
     */
    public static final String GRAPH_DESCRIPTION_TAG = "description";

    /**
     * GRAPH_METADATA_TAG
     */
    public static final String GRAPH_METADATA_TAG = "metadata";

    /**
     * GRAPH_INPUT_METADATA_TAG
     */
    public static final String GRAPH_INPUT_METADATA_TAG = "inputMetadata";

    /**
     * GRAPH_OUTPUT_METADATA_TAG
     */
    public static final String GRAPH_OUTPUT_METADATA_TAG = "outputMetadata";

    // Tags for Node

    /**
     * The tag for a node.
     */
    public static final String NODE_TAG = "node";

    /**
     * type
     */
    public static final String NODE_TYPE_ATTRIBUTE = "type";

    /**
     * ws
     */
    public static final String NODE_TYPE_WS = "ws";

    /**
     * StreamSource
     */
    public static final String NODE_TYPE_STREAM_SOURCE = "streamsource";

    /**
     * CEP
     */
    public static final String NODE_TYPE_CEP = "cep";

    /**
     * workflow
     */
    public static final String NODE_TYPE_WORKFLOW = "workflow";

    /**
     * Input parameter node
     */
    public static final String NODE_TYPE_INPUT = "input";

    /**
     * Output parameter node
     */
    public static final String NODE_TYPE_OUTPUT = "output";

    /**
     * constant
     */
    public static final String NODE_TYPE_CONSTANT = "constant";

    /**
     * split
     */
    public static final String NODE_TYPE_SPLIT = "split";

    /**
     * merge
     */
    public static final String NODE_TYPE_MERGE = "merge";

    /**
     * if
     */
    public static final String NODE_TYPE_IF = "if";

    /**
     * endif
     */
    public static final String NODE_TYPE_ENDIF = "endif";

    /**
     * block
     */
    public static final String NODE_TYPE_BLOCK = "block";

    /**
     * endBlock
     */
    public static final String NODE_TYPE_EXIT = "exit";

    /**
     * endBlock
     */
    public static final String NODE_TYPE_ENDBLOCK = "endBlock";

    /**
     * DoWhile
     */
    public static final String NODE_TYPE_DOWHILE = "doWhile";

    /**
     * EndDoWhile
     */
    public static final String NODE_TYPE_ENDDOWHILE = "enddoWhile";


    /**
     * receive
     */
    public static final String NODE_TYPE_RECEIVE = "receive";

    /**
     * memo
     */
    public static final String NODE_TYPE_MEMO = "memo";

    /**
     * Instance
     */
    public static final String NODE_TYPE_INSTANCE = "instance";

    /**
     * Terminate Instance
     */
    public static final String NODE_TYPE_TERMINATE = "terminate";

    /**
     * Tag for the ID of a node ID. It is unique among a graph.
     */
    public static final String NODE_ID_TAG = "id";

    /**
     * Tag for the name of a node name. A node name is same as the component name.
     */
    public static final String NODE_NAME_TAG = "name";

    /**
     * Tag for wsdl QName.
     */
    public static final String NODE_WSDL_QNAME_TAG = "wsdl";

    /**
     * portType
     */
    public static final String NODE_WSDL_PORT_TYPE_TAG = "portType";

    /**
     * operationType
     */
    public static final String NODE_WSDL_OPERATION_TAG = "operation";

    /**
     * templateID
     */
    public static final String NODE_TEMPLATE_ID_TAG = "templateID";

    /**
     * Tag for a component (Not used since the introduction of .xwf)
     */
    public static final String NODE_COMPONENT_TAG = "component";

    /**
     * The tag used for the configuration of a node.
     */
    public static final String NODE_CONFIG_TAG = "config";

    /**
     * memo
     */
    public static final String NODE_MEMO_TAG = "memo";

    /**
     * Tag for input port
     */
    public static final String NODE_INPUT_PORT_TAG = "inputPort";

    /**
     * Tag for output port
     */
    public static final String NODE_OUTPUT_PORT_TAG = "outputPort";

    /**
     * controlInPort
     */
    public static final String NODE_CONTROL_IN_PORT_TAG = "controlInPort";

    /**
     * controlOutPort
     */
    public static final String NODE_CONTROL_OUT_PORT_TAG = "controlOutPort";

    /**
     * eprPort
     */
    public static final String NODE_EPR_PORT_TAG = "eprPort";

    /**
     * Tag for x-coordinate
     */
    public static final String NODE_X_LOCATION_TAG = "x";

    /**
     * Tag for y-coordinate
     */
    public static final String NODE_Y_LOCATION_TAG = "y";

    // Tags for Port

    /**
     * The tag for a port.
     */
    public static final String PORT_TAG = "port";

    /**
     * type
     */
    public static final String PORT_TYPE_ATTRIBUTE = "type";

    /**
     * ws
     */
    public static final String PORT_TYPE_WS_DATA = "ws";

    /**
     * cep
     */
    public static final String PORT_TYPE_CEP = "cep";

    /**
     * dynamicData
     */
    public static final String PORT_TYPE_SYSTEM_DATA = "systemData";

    /**
     * control
     */
    public static final String PORT_TYPE_CONTROL = "control";

    /**
     * epr
     */
    public static final String PORT_TYPE_EPR = "epr";

    /**
     * Instance
     */
    public static final String PORT_TYPE_INSTANCE = "instanceData";

    /**
     * Tag for the ID of a port
     */
    public static final String PORT_ID_TAG = "id";

    /**
     * Tag for the name of a port
     */
    public static final String PORT_NAME_TAG = "name";

    /**
     * Tag for the data type of a port
     */
    public static final String PORT_DATA_TYPE_TAG = "dataType";

    /**
     * Tag for a node that a port belongs to.
     */
    public static final String PORT_NODE_TAG = "node";

    // Tags for Edge

    /**
     * The tag for an edge
     */
    public static final String EDGE_TAG = "edge";

    /**
     * Tag for the ID of from port of an edge.
     */
    public static final String EDGE_FROM_PORT_TAG = "fromPort";

    /**
     * Tag for the ID of from port of an edge.
     */
    public static final String EDGE_TO_PORT_TAG = "toPort";

    /**
     * type
     */
    public static final String EDGE_TYPE_ATTRIBUTE = "type";

    /**
     * data
     */
    public static final String EDGE_TYPE_DATA = "data";

    /**
     * control
     */
    public static final String EDGE_TYPE_CONTROL = "control";

    public static final String PORT_TYPE_UUID = "uuid";

    public static final String NODE_STREAM_LABEL_TAG = "streamlabel";

	public static final String NODE_TYPE_DIFFERED_INPUT = "Differed Input";

    public static final String PORT_INPUT_ORDER = "inputOrder";
}