/*
 * Copyright (c) 2006-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: ResourceNotifierTestCase.java,v 1.8 2008/04/01 21:44:24 echintha Exp $
 */
package org.apache.airavata.xbaya.test;

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

import java.net.URI;

import org.apache.airavata.xbaya.util.XMLUtil;

import wsmg.WseClientAPI;
import xsul.ws_addressing.WsaEndpointReference;
import edu.indiana.extreme.lead.workflow_tracking.Notifier;
import edu.indiana.extreme.lead.workflow_tracking.NotifierFactory;
import edu.indiana.extreme.lead.workflow_tracking.common.ConstructorConsts;
import edu.indiana.extreme.lead.workflow_tracking.common.ConstructorProps;
import edu.indiana.extreme.lead.workflow_tracking.common.InvocationEntity;

/**
 * @author Satoshi Shirasuna
 */
public class ResourceNotifierTestCase extends XBayaTestCase {

    /**
     * 
     */
    public void test() {
        WsaEndpointReference brokerEPR = WseClientAPI.createEndpointReference(this.configuration.getBrokerURL()
                .toString(), this.configuration.getTopic());

        ConstructorProps props = ConstructorProps.newProps();
        props.set(ConstructorConsts.BROKER_EPR, XMLUtil.xmlElementToString(brokerEPR));
        Notifier notifier = NotifierFactory.createNotifier(props);

        URI initiatorWorkflowID = URI.create("Workflow");
        URI initiatorServiceID = URI.create("Adder_add");
        String initiatorWorkflowNodeID1 = "Adder_add";
        Integer workflowTimeStep = new Integer(0);
        InvocationEntity entity1 = notifier.createEntity(initiatorWorkflowID, initiatorServiceID,
                initiatorWorkflowNodeID1, workflowTimeStep);
        notifier.resourceMapping(entity1, "resource1.example.com", 1);
        notifier.resourceMapping(entity1, "resource2.example.com", 2);
        notifier.resourceMapping(entity1, "resource3.example.com", 3);

        String initiatorWorkflowNodeID2 = "Adder_add_2";
        InvocationEntity entity2 = notifier.createEntity(initiatorWorkflowID, initiatorServiceID,
                initiatorWorkflowNodeID2, workflowTimeStep);
        notifier.resourceMapping(entity2, "resource.example.com", 0);
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2006-2007 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */
