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
package org.apache.airavata.workflow.model.component.ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import com.google.gson.JsonObject;
import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.ComponentControlPort;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.ws.WSNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;

public class WSComponent extends Component {

    private static final Logger logger = LoggerFactory.getLogger(WSComponent.class);

    private WSComponentApplication application;
    
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

    private String inputPartName;

    private String outputPartName;

    private String inputTypeName;

    private String outputTypeName;

    protected WSComponent() {

    }

    /**
     * Constructs a WSComponent.
     * 
     * @param wsdl
     * @param portTypeQName
     * @param operationName
     * @throws ComponentException
     */
    public WSComponent(WSComponentApplication application) throws ComponentException {
        this.setApplication(application);
        this.operationName = application.getName();
        this.description = application.getDescription();
        setName(application.getName());
        
        this.inputs = new ArrayList<WSComponentPort>();
        this.outputs = new ArrayList<WSComponentPort>();
        
        List<WSComponentApplicationParameter> applicationInputs = application.getInputParameters();
        for (WSComponentApplicationParameter inputDataObjectType : applicationInputs) {
            WSComponentPort port = new WSComponentPort(inputDataObjectType.getName(),inputDataObjectType.getType() , this);
            port.setDescription(inputDataObjectType.getDescription());
            port.setDefaultValue(inputDataObjectType.getDefaultValue());
            port.setApplicationArgument(inputDataObjectType.getApplicationArgument());
            port.setInputOrder(inputDataObjectType.getInputOrder());
            port.setType(inputDataObjectType.getType());
			inputs.add(port);
		}

        List<WSComponentApplicationParameter> applicationOutputs = application.getOutputParameters();
        for (WSComponentApplicationParameter outputDataObjectType : applicationOutputs) {
            WSComponentPort port = new WSComponentPort(outputDataObjectType.getName(),outputDataObjectType.getType() , this);
            port.setDescription(outputDataObjectType.getDescription());
            port.setDefaultValue(outputDataObjectType.getDefaultValue());
            port.setType(outputDataObjectType.getType());
			outputs.add(port);
		}

        this.controlInPort = new ComponentControlPort();
        this.controlOutPorts.add(new ComponentControlPort());
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
     * @see org.apache.airavata.workflow.model.component.Component#createNode(org.apache.airavata.workflow.model.graph.Graph)
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
     * @see org.apache.airavata.workflow.model.component.Component#toHTML()
     */
    @Override
    public String toHTML() {

        StringBuffer buf = new StringBuffer();
        buf.append("<html>\n");
        buf.append("<h1>Application: " + getName() + "</h1>\n");

        buf.append("<h2>Description:</h2>\n");
        buf.append(application.getApplicationId()+"<br />"+(this.description==null?"":this.description));

        if (getInputPorts().size()>0) {
			buf.append("<h3>Input parameter(s)</h3>\n");
			messageToHtml(getInputPorts(), buf);
		}
		if (getOutputPorts().size()>0) {
			buf.append("<h3>Output parameter(s)</h3>\n");
			messageToHtml(getOutputPorts(), buf);
		}
		buf.append("</html>\n");
        return buf.toString();
    }

    private void messageToHtml(List<WSComponentPort> ports, StringBuffer buf) {
        buf.append("<dl>\n");
        for (WSComponentPort port : ports) {
            buf.append("<dt><strong>" + port.getName() + "</strong></dt>\n");
            buf.append("<dd>Type: " + port.getType().toString()+ "</dd>\n");
            if (port.getDescription() != null && !port.getDescription().equals("")) {
                buf.append("<dd>Description: " + port.getDescription() + "</dd>\n");
            }
        }
        buf.append("</dl>\n");
    }

	public XmlElement toXML() {
		return getApplication().toXml();
	}

    public JsonObject toJSON(){
        return getApplication().toJSON();
    }

	public WSComponentApplication getApplication() {
		return application;
	}

	public void setApplication(WSComponentApplication application) {
		this.application = application;
	}

	public QName getPortTypeQName() {
		return null;
	}

}