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

package org.apache.airavata.xbaya.graph.ws;

public class InteractionNode /* extends SystemNode */{

    // private String operationName;
    //
    // private InteractionNodeGUI gui;
    //
    // /**
    // * Constructs a WsdlNode.
    // *
    // * @param nodeElement
    // * @throws GraphException
    // */
    // public InteractionNode(XmlElement nodeElement) throws GraphException {
    // super(nodeElement);
    // }
    //
    // /**
    // * Constructs a WSNode.
    // *
    // * @param graph
    // */
    // public InteractionNode(Graph graph) {
    // super(graph);
    // }
    //
    // /**
    // * @see org.apache.airavata.xbaya.graph.Node#getGUI()
    // */
    // public NodeGUI getGUI() {
    // if (this.gui == null) {
    // this.gui = new InteractionNodeGUI(this);
    // }
    // return this.gui;
    // }
    //
    // /**
    // * @see org.apache.airavata.xbaya.graph.Node#getComponent()
    // */
    // @Override
    // public InteractionComponent getComponent() {
    // return (InteractionComponent) super.getComponent();
    // }
    //
    //
    // public DataPort getTheFreePort(){
    // List<DataPort> inputPorts = this.getInputPorts();
    // for (DataPort dataPort : inputPorts) {
    // if(null == dataPort.getFromNode()){
    // return dataPort;
    // }
    // }
    // //none found, so make a new one.
    // WSPort port = new WSPort();
    // addInputPort(port);
    // getComponent().addInputPort(port);
    //
    // return port;
    // }
    //
    // /**
    // * @throws GraphException
    // * @see org.apache.airavata.xbaya.graph.impl.NodeImpl#edgeWasAdded(org.apache.airavata.xbaya.graph.Edge)
    // */
    // @Override
    // protected void edgeWasAdded(Edge edge) throws GraphException {
    // // GraphUtil.validateConnection(edge);
    // }
    //
    // /**
    // * @return the node xml
    // */
    // @Override
    // protected XmlElement toXML() {
    // throw new UnsupportedOperationException();
    // }
    //
    // /**
    // * @see org.apache.airavata.xbaya.graph.impl.NodeImpl#parse(org.xmlpull.infoset.XmlElement)
    // */
    // @Override
    // protected void parse(XmlElement nodeElement) throws GraphException {
    // super.parse(nodeElement);
    //
    // throw new UnsupportedOperationException();
    // }
    //
    //
    //
    //
    // @Override
    // @Deprecated
    // protected void parseComponent(XmlElement componentElement)
    // throws GraphException {
    // throw new UnsupportedOperationException();
    // // try {
    // // String componentString = componentElement.requiredText();
    // // WSComponent wsdlComponent = WSComponentFactory
    // // .createComponent(componentString);
    // // setComponent(wsdlComponent);
    // // } catch (ComponentException e) {
    // // throw new GraphException(ErrorMessages.COMPONENT_FORMAT_ERROR, e);
    // // }
    // }
    //

}