/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of4 the License at
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

package org.apache.airavata.workflow.model.component.system;

import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.system.DifferedInputNode;

public class DifferedInputComponent extends SystemComponent {

    /**
     * The name of the input component
     */
    public static final String NAME = "DifferedInput";

    private static final String DESCRIPTION = "A system component that represents an differred input parameter of a workflow.";

    private static final String PORT_NAME = "Parameter";

    /**
     * The description.
     */
    private static final String PORT_DESCRIPTION = "This port can be connected to any type.";

    /**
     * Creates an InputComponent.
     */
    public DifferedInputComponent() {
        super();
        setName(NAME);
        setDescription(DESCRIPTION);

        SystemComponentDataPort port = new SystemComponentDataPort(PORT_NAME);
        port.setDescription(PORT_DESCRIPTION);
        this.outputs.add(port);
    }

    /**
     * @see edu.indiana.extreme.xbaya.component.Component#createNode(edu.indiana.extreme.xbaya.graph.Graph)
     */
    @Override
    public DifferedInputNode createNode(Graph graph) {
    	DifferedInputNode node = new DifferedInputNode(graph);
        node.setName(NAME);
        node.setComponent(this);

        // Creates a unique ID for the node. This has to be after setName().
        node.createID();

        // Creates a output port
        createPorts(node);

        return node;
    }

}


/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2012 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice,
 * the list of authors in the original source code, this list of conditions and
 * the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the disclaimer listed in this license in
 * the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the
 * following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme!
 * Lab. For further information please visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and
 * wherever such third-party acknowledgments normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall
 * not be used to endorse or promote products derived from this software without
 * prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name
 * nor may "Indiana University" appear in their name, without prior written
 * permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided
 * does not infringe the patent or any other intellectual property rights of any
 * other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual
 * property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO
 * WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA UNIVERSITY GIVES
 * NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF
 * INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS.
 * INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS FREE FROM "BUGS",
 * "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE.
 * LICENSEE ASSUMES THE ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR
 * ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION
 * GENERATED USING SOFTWARE.
 */
