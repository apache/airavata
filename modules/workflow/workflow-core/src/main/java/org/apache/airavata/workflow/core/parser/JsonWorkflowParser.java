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
package org.apache.airavata.workflow.core.parser;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.apache.airavata.model.ComponentState;
import org.apache.airavata.model.ComponentStatus;
import org.apache.airavata.model.EdgeModel;
import org.apache.airavata.model.NodeModel;
import org.apache.airavata.model.PortModel;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.workflow.core.WorkflowInfo;
import org.apache.airavata.workflow.core.dag.edge.DirectedEdge;
import org.apache.airavata.workflow.core.dag.edge.Edge;
import org.apache.airavata.workflow.core.dag.nodes.ApplicationNode;
import org.apache.airavata.workflow.core.dag.nodes.ApplicationNodeImpl;
import org.apache.airavata.workflow.core.dag.nodes.InputNode;
import org.apache.airavata.workflow.core.dag.nodes.InputNodeImpl;
import org.apache.airavata.workflow.core.dag.nodes.OutputNode;
import org.apache.airavata.workflow.core.dag.nodes.OutputNodeImpl;
import org.apache.airavata.workflow.core.dag.nodes.WorkflowNode;
import org.apache.airavata.workflow.core.dag.port.InPort;
import org.apache.airavata.workflow.core.dag.port.InputPortIml;
import org.apache.airavata.workflow.core.dag.port.OutPort;
import org.apache.airavata.workflow.core.dag.port.OutPortImpl;
import org.apache.airavata.workflow.core.dag.port.Port;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class JsonWorkflowParser implements WorkflowParser {

    private final JsonReader jsonReader;

    private List<InputNode> inputs;
    private List<OutputNode> outputs;
    private List<ApplicationNode> applications;
    private Map<String, WorkflowNode> nodeMap;
    private List<Port> ports;
    private List<Edge> edges;
    private List<Link> links;
    private WorkflowInfo workflowInfo;

    public JsonWorkflowParser(String jsonWorkflowString) {
        this(new ByteArrayInputStream(jsonWorkflowString.getBytes()));
    }

    public JsonWorkflowParser(InputStream inputStream) {
        this(new InputStreamReader(inputStream));
    }

    public JsonWorkflowParser(InputStreamReader inputStreamReader) {
        this(new JsonReader(inputStreamReader));
    }

    public JsonWorkflowParser(JsonReader jsonReader) {
        this.jsonReader = jsonReader;
        init();
    }

    private void init() {
        applications = new ArrayList<>();
        nodeMap = new HashMap<>();
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        links = new ArrayList<>();
        workflowInfo = new WorkflowInfo();

    }


    @Override
    public WorkflowInfo parse() throws Exception {
        // TODO parse json string and construct components
        if (jsonReader.peek() != JsonToken.BEGIN_OBJECT) {
            throw new Exception("Invalid Json data expected beginObject but found " + getTokenString(jsonReader.peek()));
        }
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName(); // workflow
            if (name.equals(WORKFLOW)) {
                readWorkflowInfo(jsonReader);
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();

        buildWorkflowGraph();
        workflowInfo.setInputs(inputs);
        workflowInfo.setApplications(applications);
        workflowInfo.setOutputs(outputs);
        return workflowInfo;
    }

    private void buildWorkflowGraph() throws Exception {
        // TODO construct runtime model
        Queue<WorkflowNode> queue = new LinkedList<>();
        queue.addAll(inputs);

        Map<String, List<Link>> linkMap = getEdgesMap(links);
        Map<String, InPort> nodeInportMap = getNodeInPortsMap(getApplicationNodes());
        nodeInportMap.putAll(getNodeInPortMap(getOutputNodes()));
        Set<String> processedNodes = new HashSet<>();

        while (!queue.isEmpty()) {
            WorkflowNode node = queue.poll();
            if (processedNodes.contains(node.getId())) {
                continue;
            }

            if (node instanceof InputNode) {
                InputNode input = ((InputNode) node);
                OutPort outPort = ((OutPort) node);
                Map<String,Edge> edgeMap = addEdges(outPort, linkMap.get(outPort.getNodeId() + "," + outPort.getId()));

                for (Map.Entry<String, Edge> entry : edgeMap.entrySet()) {
                    InPort inPort = nodeInportMap.get(entry.getKey());
                    if (inPort != null) {
//                        inPort.addEdge(entry.getValue());
                        entry.getValue().setToPort(inPort);

                        queue.add(inPort.getNode());
                    }
                }

            } else if (node instanceof ApplicationNode) {
                ApplicationNode appNode = ((ApplicationNode) node);
                for (OutPort outPort : appNode.getOutputPorts()) {
                    outPort.setNode(appNode);
                    Map<String, Edge> edgeMap = addEdges(outPort, linkMap.get(outPort.getNodeId() + "," + outPort.getId()));

                    for (Map.Entry<String, Edge> entry : edgeMap.entrySet()) {
                        InPort inPort = nodeInportMap.get(entry.getKey());
                        if (inPort != null) {
//                            inPort.addEdge(entry.getValue());
                            entry.getValue().setToPort(inPort);

                            queue.add(inPort.getNode());
                        }
                    }
                }
            } else if (node instanceof OutputNode) {
                OutputNode outputNode = ((OutputNode) node);
                InPort inPort = ((InPort) node);
                outputNode.setInputObject(inPort.getInputObject());

            }
            // marke node as precessed node, we don't need to process it again.
            processedNodes.add(node.getId());
        }

    }

    private Map<String, InPort> getNodeInPortMap(List<OutputNode> outputNodes) {
        Map<String, InPort> nodeInPortsMap = new HashMap<>();
        if (outputNodes != null) {
            for (OutputNode outputNode : outputNodes) {
                InPort inPort = outputNode.getInPort();
                inPort.setNode(outputNode);
                nodeInPortsMap.put(outputNode.getId() + "," + inPort.getId(), inPort);
            }
        }
        return nodeInPortsMap;
    }

    private Map<String, InPort> getNodeInPortsMap(List<ApplicationNode> applicationNodes) {
        Map<String, InPort> nodeInPortsMap = new HashMap<>();
        if (applicationNodes != null) {
            for (ApplicationNode applicationNode : applicationNodes) {
                for (InPort inPort : applicationNode.getInputPorts()) {
                    inPort.setNode(applicationNode);
                    nodeInPortsMap.put(applicationNode.getId() + "," + inPort.getId(), inPort);
                }
            }
        }

        return nodeInPortsMap;
    }

    /**
     *
     * @param outPort -
     * @param links  -
     * @return key: nodeId,inportId  value : link
     */
    private Map<String, Edge> addEdges(OutPort outPort, List<Link> links) {
        Map<String, Edge> inPortMap = new HashMap<>();
        if (links != null) {
            for (Link link : links) {
                EdgeModel edgeModel = new EdgeModel(link.getId());
                Edge edge = new DirectedEdge(edgeModel);
//                edge.setFromPort(outPort);
                outPort.addEdge(edge);
                inPortMap.put(link.getTo().getNodeId() + "," + link.getTo().getPortId(), edge);
            }
        }
        return inPortMap;
    }

    private Map<String, List<Link>> getEdgesMap(List<Link> links) {
        Map<String, List<Link>> map = new HashMap<>();
        List<Link> linkList;
        for (Link link : links) {
            linkList = map.get(link.from.getNodeId() + "," + link.from.getPortId());
            if (linkList == null) {
                linkList = new ArrayList<>();
            }

            linkList.add(link);
            map.put(link.from.getNodeId() + "," + link.from.getPortId(), linkList);
        }

        return map;
    }


    private void readWorkflowInfo(JsonReader jsonReader) throws IOException, ParserException {
        jsonReader.beginObject();
        String name;
        while (jsonReader.hasNext()) {
            name = jsonReader.nextName();
            if (name.equals(NAME)) {
                workflowInfo.setName(jsonReader.nextString());
            } else if (name.equals(ID)) {
                workflowInfo.setId(jsonReader.nextString());
            } else if (name.equals(DESCRIPTION)) {
                workflowInfo.setDescription(jsonReader.nextString());
            } else if (name.equals(VERSION)) {
                workflowInfo.setVersion(jsonReader.nextString());
            } else if (name.equals(APPLICATIONS)) {
                readApplications(jsonReader);
            } else if (name.equals(WORKFLOW_INPUTS)) {
                readWorkflowInputs(jsonReader);
            } else if (name.equals(WORKFLOW_OUTPUTS)) {
                readWorkflowOutputs(jsonReader);
            } else if (name.equals(LINKS)) {
                readWorkflowLinks(jsonReader);
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        //TODO: set count properties of workflow info object
    }

    private void readApplications(JsonReader jsonReader) throws IOException, ParserException {
        jsonReader.beginArray();
        ApplicationNode appNode = null;
        while (jsonReader.hasNext()) {
            appNode = readApplication(jsonReader);
            applications.add(appNode);
        }
        jsonReader.endArray();
    }

    private void readWorkflowInputs(JsonReader jsonReader) throws ParserException, IOException {
        JsonToken peek = jsonReader.peek();
        InputNode inputNode;
        NodeModel nodeModel;
        ComponentStatus status;
        String name;
        if (peek == JsonToken.NULL) {
            throw new ParserException("Error! workflow inputs can't be null");
        } else if (peek == JsonToken.BEGIN_ARRAY) {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                jsonReader.beginObject();
                nodeModel = new NodeModel();
                status = new ComponentStatus();
                status.setState(ComponentState.CREATED);
                status.setReason("Created");
                nodeModel.setStatus(status);
                inputNode = new InputNodeImpl(nodeModel);
                while (jsonReader.hasNext()) {
                    name = jsonReader.nextName();
                    if (name.equals(NAME)) {
                        nodeModel.setName(jsonReader.nextString());
                    } else if (name.equals(ID)) {
                        nodeModel.setNodeId(jsonReader.nextString());
                    } else if (name.equals(DATATYPE)) {
                        inputNode.setDataType(DataType.valueOf(jsonReader.nextString()));
                    } else if (name.equals(DESCRIPTION)) {
                        nodeModel.setDescription(jsonReader.nextString());
                    } else if (name.equals(POSITION)) {
                        readPosition(jsonReader);
                    } else if (name.equals(NODE_ID)) {
                        jsonReader.skipValue();
//                        nodeModel.setNodeId(jsonReader.nextString());
                    } else if (name.equals(DEFAULT_VALUE)) {
                        inputNode.setValue(jsonReader.nextString());
                    } else {
                        jsonReader.skipValue();
                    }
                }
                jsonReader.endObject();
                inputs.add(inputNode);
            }
            jsonReader.endArray();
        } else {
            throw new ParserException("Error! Unsupported value for Workflow Inputs, exptected " +
                    getTokenString(JsonToken.BEGIN_OBJECT) + " but found" + getTokenString(peek));
        }
    }

    private void readWorkflowOutputs(JsonReader jsonReader) throws IOException, ParserException {
        JsonToken peek = jsonReader.peek();
        OutputNode outputNode;
        NodeModel nodeModel;
        ComponentStatus status;
        String name;
        if (peek == JsonToken.NULL) {
            throw new ParserException("Error! workflow outputs can't be null");
        } else if (peek == JsonToken.BEGIN_ARRAY) {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                jsonReader.beginObject();
                nodeModel = new NodeModel();
                status = new ComponentStatus();
                status.setState(ComponentState.CREATED);
                status.setReason("Created");
                nodeModel.setStatus(status);
                outputNode = new OutputNodeImpl(nodeModel);
                while (jsonReader.hasNext()) {
                    name = jsonReader.nextName();
                    if (name.equals(NAME)) {
                        nodeModel.setName(jsonReader.nextString());
                    } else if (name.equals(ID)) {
                        nodeModel.setNodeId(jsonReader.nextString());
                    } else if (name.equals(DATATYPE)) {
                        jsonReader.skipValue();
                    } else if (name.equals(DESCRIPTION)) {
                        nodeModel.setDescription(jsonReader.nextString());
                    } else if (name.equals(POSITION)) {
                        readPosition(jsonReader);
                    } else if (name.equals(NODE_ID)) {
                        jsonReader.skipValue();
//                        nodeModel.setNodeId(jsonReader.nextString());
                    } else if (name.equals(DEFAULT_VALUE)) {
                        jsonReader.skipValue();
                    } else {
                        jsonReader.skipValue();
                    }

                }
                jsonReader.endObject();
                outputs.add(outputNode);
            }
            jsonReader.endArray();
        } else {
            throw new ParserException("Error! Unsupported value for Workflow Outputs, exptected " +
                    getTokenString(JsonToken.BEGIN_OBJECT) + " but found" + getTokenString(peek));
        }
    }

    private void readWorkflowLinks(JsonReader jsonReader) throws IOException, ParserException {
        JsonToken peek = jsonReader.peek();
        if (peek == JsonToken.NULL) {
            throw new ParserException("Error! Workflow should have connecting links, found " + getTokenString(peek));
        } else if (peek == JsonToken.BEGIN_ARRAY) {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                links.add(readLink(jsonReader));
            }
            jsonReader.endArray();
        } else {
            throw new ParserException("Error! Unsupported value for workflow links, expected " +
                    getTokenString(JsonToken.BEGIN_ARRAY) + " but found" + getTokenString(peek));

        }
    }

    private Link readLink(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        String name = null;
        Link link = new Link();
        while (jsonReader.hasNext()) {
            name = jsonReader.nextName();
            if (name.equals(DESCRIPTION)) {
                link.setDescription(jsonReader.nextString());
            } else if (name.equals(FROM)) {
                link.setFrom(readLinkHelper(jsonReader));
            } else if (name.equals(TO)) {
                link.setTo(readLinkHelper(jsonReader));
            } else if (name.equals(ID)) {
                link.setId(jsonReader.nextString());
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return link;
    }

    private LinkHelper readLinkHelper(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        String name;
        LinkHelper helper = new LinkHelper();
        while (jsonReader.hasNext()) {
            name = jsonReader.nextName();
            if (name.equals(NODE_ID)) {
                helper.setNodeId(jsonReader.nextString());
            } else if (name.equals(OUTPUT_ID) || name.equals(INPUT_ID)) {
                helper.setPortId(jsonReader.nextString());
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return helper;
    }

    private ApplicationNode readApplication(JsonReader jsonReader) throws IOException, ParserException {
        jsonReader.beginObject();
        NodeModel nodeModel = new NodeModel();
        ComponentStatus status = new ComponentStatus();
        status.setState(ComponentState.CREATED);
        status.setReason("Created");
        nodeModel.setStatus(status);
        ApplicationNode applicationNode = new ApplicationNodeImpl(nodeModel);
        String name;
        while (jsonReader.hasNext()) {
            name = jsonReader.nextName();
            if (name.equals(APPLICATION_ID)) {
                nodeModel.setApplicationId(jsonReader.nextString());
            } else if (name.equals(NAME)) {
                nodeModel.setName(jsonReader.nextString());
            } else if (name.equals(DESCRIPTION)) {
                nodeModel.setDescription(jsonReader.nextString());
            } else if (name.equals(APPTYPE)) {
                jsonReader.skipValue();
            } else if (name.equals(INPUTS)) {
                applicationNode.addInputPorts(readApplicationInputs(jsonReader));
            } else if (name.equals(OUTPUTS)) {
                applicationNode.addOutPorts(readApplicationOutputs(jsonReader));
            } else if (name.equals(POSITION)) {
                readPosition(jsonReader);
            } else if (name.equals(NODE_ID)) {
                nodeModel.setNodeId(jsonReader.nextString());
            } else if (name.equals(PARALLEL_EXECUTION)) {
                jsonReader.skipValue();
            } else if (name.equals(PROPERTIES)) {
                readProperties(jsonReader);
            }
        }
        jsonReader.endObject();
        return applicationNode;
    }

    private List<InPort> readApplicationInputs(JsonReader jsonReader) throws IOException, ParserException {
        List<InPort> inPorts = new ArrayList<>();
        JsonToken peek = jsonReader.peek();
        PortModel portModel;
        InPort inPort;
        String name;
        if (peek == JsonToken.NULL) {
            jsonReader.nextNull();
        } else if (peek == JsonToken.BEGIN_ARRAY) {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                portModel = new PortModel();
                inPort = new InputPortIml(portModel);
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    name = jsonReader.nextName();
                    if (name.equals(NAME)) {
                        portModel.setName(jsonReader.nextString());
                    } else if (name.equals(ID)) {
                        portModel.setPortId(jsonReader.nextString());
                    } else if (name.equals(DATATYPE)) {
                        jsonReader.skipValue();
                    } else if (name.equals(DEFAULT_VALUE)) {
                        inPort.setDefaultValue(jsonReader.nextString());
                    } else if (name.equals(DESCRIPTION)) {
                        portModel.setDescription(jsonReader.nextString());
                    } else {
                        jsonReader.skipValue();
                    }
                }
                jsonReader.endObject();
                inPorts.add(inPort);
            }
            jsonReader.endArray();
        } else {
            throw new ParserException("Error! reading application inputs, expected " + getTokenString(JsonToken.NULL) +
                    " or " + getTokenString(JsonToken.BEGIN_ARRAY) + " but found " + getTokenString(peek));
        }

        return inPorts;
    }

    private List<OutPort> readApplicationOutputs(JsonReader jsonReader) throws IOException, ParserException {
        List<OutPort> outPorts = new ArrayList<>();
        PortModel portModel;
        OutPort outPort;
        String name;
        JsonToken peek = jsonReader.peek();
        if (peek == JsonToken.NULL) {
            jsonReader.nextNull();
        } else if (peek == JsonToken.BEGIN_ARRAY) {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                portModel = new PortModel();
                outPort = new OutPortImpl(portModel);
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    name = jsonReader.nextName();
                    if (name.equals(NAME)) {
                        portModel.setName(jsonReader.nextString());
                    } else if (name.equals(ID)) {
                        portModel.setPortId(jsonReader.nextString());
                    } else if (name.equals(DATATYPE)) {
                        jsonReader.skipValue();
                    } else if (name.equals(DEFAULT_VALUE)) {
                        jsonReader.skipValue(); // can output has default values?
                    } else if (name.equals(DESCRIPTION)) {
                        portModel.setDescription(jsonReader.nextString());
                    } else {
                        jsonReader.skipValue();
                    }
                }
                jsonReader.endObject();
                outPorts.add(outPort);
            }
            jsonReader.endArray();
        } else {
            throw new ParserException("Error! reading application outputs, expected " + getTokenString(JsonToken.NULL) +
                    " or " + getTokenString(JsonToken.BEGIN_ARRAY) + " but found " + getTokenString(peek));

        }
        return outPorts;
    }

    private void readPosition(JsonReader jsonReader) throws IOException {
        JsonToken peek = jsonReader.peek();
        if (peek == JsonToken.NULL) {
            jsonReader.nextNull();
        } else if (peek == JsonToken.BEGIN_OBJECT) {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                // skip position data.
                jsonReader.nextName();
                jsonReader.skipValue();
            }
            jsonReader.endObject();
        } else {
            jsonReader.skipValue();
        }
    }

    private void readProperties(JsonReader jsonReader) throws IOException {
        JsonToken peek = jsonReader.peek();
        if (peek == JsonToken.NULL) {
            jsonReader.nextNull();
        } else if (peek == JsonToken.BEGIN_OBJECT) {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                // TODO: Read and use proprety values
                String name = jsonReader.nextName();
                jsonReader.skipValue();
            }
            jsonReader.endObject();
        } else {
            jsonReader.skipValue();
        }

    }

    private String getTokenString(JsonToken peek) {
        switch (peek) {
            case BEGIN_OBJECT:
                return "Begin Object";
            case BEGIN_ARRAY:
                return "Begin Array";
            case END_OBJECT:
                return "End Object";
            case END_ARRAY:
                return "End Array";
            case NAME:
                return "Name";
            case STRING:
                return "String";
            case NUMBER:
                return "Number";
            case BOOLEAN:
                return "Boolean";
            case NULL:
                return "Null";
            case END_DOCUMENT:
                return "End Document";
            default:
                return "<Coudn't find token type>";
        }
    }


    @Override
    public List<InputNode> getInputNodes() throws Exception {
        return inputs;
    }

    @Override
    public List<OutputNode> getOutputNodes() throws Exception {
        return outputs;
    }

    @Override
    public List<ApplicationNode> getApplicationNodes() throws Exception {
        return applications;
    }

    @Override
    public List<Port> getPorts() throws Exception {
        return ports;
    }

    @Override
    public List<Edge> getEdges() throws Exception {
        return edges;
    }


    private InputNode createInputNode(JsonObject jNode) {
        return null;
    }

    private OutputNode createOutputNode(JsonObject jNode) {
        return null;
    }

    private ApplicationNode createApplicationNode(JsonObject jNode) {
        return null;
    }

    private Port createPort(JsonObject jPort) {
        return null;
    }


    private Edge createEdge(JsonObject jEdge) {
        return null;
    }

    public static final String WORKFLOW = "workflow";
    private static final String NAME = "name";
    public static final String ID = "id";
    public static final String DESCRIPTION = "description";
    public static final String VERSION = "version";
    public static final String APPLICATIONS = "applications";
    public static final String APPLICATION_ID = "applicationId";
    public static final String APPTYPE = "appType";
    public static final String INPUTS = "inputs";
    public static final String DATATYPE = "dataType";
    public static final String DEFAULT_VALUE = "defaultValue";
    public static final String OUTPUTS = "outputs";
    public static final String POSITION = "position";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String NODE_ID = "nodeId";
    public static final String PARALLEL_EXECUTION = "parallelExecution";
    public static final String PROPERTIES = "properties";
    public static final String WORKFLOW_INPUTS = "workflowInputs";
    public static final String WORKFLOW_OUTPUTS = "workflowOutputs";
    public static final String LINKS = "links";
    public static final String FROM = "from";
    public static final String TO = "to";
    public static final String OUTPUT_ID = "outputId";
    public static final String INPUT_ID = "inputId";


    class Link {
        private LinkHelper from;
        private LinkHelper to;
        private String description;
        private String id;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setFrom(LinkHelper from) {
            this.from = from;
        }

        public void setTo(LinkHelper to) {
            this.to = to;
        }

        public LinkHelper getFrom() {
            return from;
        }

        public LinkHelper getTo() {
            return to;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    class LinkHelper {
        private String nodeId;
        private String portId;

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getPortId() {
            return portId;
        }

        public void setPortId(String portId) {
            this.portId = portId;
        }
    }
}

