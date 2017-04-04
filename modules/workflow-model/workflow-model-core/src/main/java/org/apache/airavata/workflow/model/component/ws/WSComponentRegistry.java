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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.airavata.common.utils.WSDLUtil;
import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.ComponentReference;
import org.apache.airavata.workflow.model.component.ComponentRegistry;
import org.apache.airavata.workflow.model.component.ComponentRegistryException;
import org.apache.airavata.workflow.model.component.system.SubWorkflowComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import xsul.wsdl.WsdlDefinitions;
//import xsul.wsdl.WsdlException;
//import xsul.wsdl.WsdlResolver;

public class WSComponentRegistry extends ComponentRegistry {

    private static final String NAME = "Web Service Components";

    private Map<String, Component> componentMap;

    private ComponentReference treeLeaf;
    private static final Logger log = LoggerFactory.getLogger(WSComponentRegistry.class);

    /**
     * Create a WSComponentRegistry
     */
    public WSComponentRegistry() {

        try {
            URI url = new URI("http://129.79.49.210:8080/axis2/services/AmazonEC2Webservice?wsdl");
//            WsdlResolver wsdlResolver = WsdlResolver.getInstance();
//            WsdlDefinitions definitions = wsdlResolver.loadWsdl(url);
//            List<WSComponent> components = WSComponentFactory.createComponents(WSDLUtil
//                    .wsdlDefinitions3ToWsdlDefintions5(definitions));
//
//            this.componentMap = new LinkedHashMap<String, Component>();
//
//            for (Component component : components) {
//                this.componentMap.put(component.getName(), component);
//            }

            String urlString = url.toString();
            String name = urlString.substring(urlString.lastIndexOf('/') + 1);
//            treeLeaf = new URLComponentReference(name, components);

//        } catch (ComponentException e) {
//            e.printStackTrace();
//        } catch (WsdlException e) {
//            log.error(e.getMessage(), e);
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
        }

    }

    /**
     * @see org.apache.airavata.workflow.model.component.registry.ComponentRegistry#getName()
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Returns a ComponentTree.
     * 
     * @return The ComponentTree
     */
    @Override
    public List<ComponentReference> getComponentReferenceList() throws ComponentRegistryException {
        List<ComponentReference> tree = new ArrayList<ComponentReference>();
    	tree.add(this.treeLeaf);
        return tree;
    }

    /**
     * @param name2
     * @param workflowComponent
     */
    public void addComponent(String name2, SubWorkflowComponent workflowComponent) {

        this.componentMap.put(name2, workflowComponent);
    }

}