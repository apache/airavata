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
package org.apache.airavata.workflow.model.component.registry;

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.workflow.model.component.ComponentReference;
import org.apache.airavata.workflow.model.component.ComponentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCRComponentRegistry extends ComponentRegistry {

    private static final Logger log = LoggerFactory.getLogger(JCRComponentRegistry.class);
    private static final String NAME = "Applications";
    private final String gatewayId;
    private Airavata.Client client;
    
    public JCRComponentRegistry(String gatewayId, Airavata.Client client) {
    	setClient(client);
        this.gatewayId = gatewayId;
    }

    /**
     * @see org.apache.airavata.workflow.model.component.registry.ComponentRegistry#getComponentReferenceList()
     */
    @Override
    public List<ComponentReference> getComponentReferenceList() {
        List<ComponentReference> tree = new ArrayList<ComponentReference>();
        try {
            if (client.isGatewayExist(gatewayId)) {
                List<ApplicationInterfaceDescription> allApplicationInterfaces = client.getAllApplicationInterfaces(gatewayId);
                for (ApplicationInterfaceDescription interfaceDescription : allApplicationInterfaces) {
                    JCRComponentReference jcr = new JCRComponentReference(interfaceDescription.getApplicationName(), interfaceDescription);
                    tree.add(jcr);
                }
            } else {
                log.error("Gateway {} Id is not exist", gatewayId);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
		}

        return tree;
    }

    /**
     * @see org.apache.airavata.workflow.model.component.registry.ComponentRegistry#getName()
     */
    @Override
    public String getName() {
        return NAME;
    }

	public Airavata.Client getClient() {
		return client;
	}

	public void setClient(Airavata.Client client) {
		this.client = client;
	}
}