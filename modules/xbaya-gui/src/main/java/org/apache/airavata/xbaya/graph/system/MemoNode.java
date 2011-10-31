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

import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.GraphSchema;
import org.apache.airavata.xbaya.graph.gui.NodeGUI;
import org.apache.airavata.xbaya.graph.system.gui.MemoNodeGUI;
import org.xmlpull.infoset.XmlElement;

public class MemoNode extends SystemNode {

    private MemoNodeGUI gui;

    private String memo;

    /**
     * Constructs a ParameterNode.
     * 
     * @param graph
     * 
     */
    public MemoNode(Graph graph) {
        super(graph);
        this.memo = "Double click here\nto take memo.";
    }

    /**
     * Constructs a MemoNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public MemoNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
    }

    /**
     * Returns the memo.
     * 
     * @return The memo
     */
    public String getMemo() {
        return this.memo;
    }

    /**
     * Sets memo.
     * 
     * @param memo
     *            The memo to set.
     */
    public void setMemo(String memo) {
        this.memo = memo;
    }

    /**
     * @see org.apache.airavata.xbaya.graph.Node#getGUI()
     */
    @Override
    public NodeGUI getGUI() {
        if (this.gui == null) {
            this.gui = new MemoNodeGUI(this);
        }
        return this.gui;
    }

    @Override
    protected XmlElement toXML() {
        XmlElement nodeElement = super.toXML();
        nodeElement.setAttributeValue(GraphSchema.NS, GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_MEMO);
        XmlElement memoElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_MEMO_TAG);
        memoElement.addChild(this.memo);
        return nodeElement;
    }

    /**
     * @see org.apache.airavata.xbaya.graph.impl.NodeImpl#parse(org.xmlpull.infoset.XmlElement)
     */
    @Override
    protected void parse(XmlElement nodeElement) throws GraphException {
        super.parse(nodeElement);
        XmlElement memoElement = nodeElement.element(GraphSchema.NODE_MEMO_TAG);
        this.memo = memoElement.requiredText();
    }

}