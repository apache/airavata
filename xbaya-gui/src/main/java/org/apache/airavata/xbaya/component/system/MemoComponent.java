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

package org.apache.airavata.xbaya.component.system;

import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.system.MemoNode;

public class MemoComponent extends SystemComponent {

    /**
     * The name of the input component
     */
    public static final String NAME = "Memo";

    private static final String DESCRIPTION = "A system component that can be used to take memo";

    /**
     * Creates an InputComponent.
     */
    public MemoComponent() {
        setName(NAME);
        setDescription(DESCRIPTION);
    }

    /**
     * @see org.apache.airavata.xbaya.component.Component#createNode(org.apache.airavata.xbaya.graph.Graph)
     */
    @Override
    public Node createNode(Graph graph) {
        MemoNode node = new MemoNode(graph);

        node.setName(NAME);
        node.setComponent(this);

        // Creates a unique ID for the node. This has to be after setName().
        node.createID();

        return node;
    }

    // /**
    // * @see org.apache.airavata.xbaya.component.Component#toHTML()
    // */
    // @Override
    // public String toHTML() {
    // StringBuffer buf = new StringBuffer();
    // buf.append("<html> <h1>" + NAME + " Component</h1>");
    // buf.append("<h2>Description:</h2> " + DESCRIPTION);
    //
    // buf.append("</html>");
    //
    // return buf.toString();
    // }
}