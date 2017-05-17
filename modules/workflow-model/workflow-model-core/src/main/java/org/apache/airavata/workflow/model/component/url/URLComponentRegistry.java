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
package org.apache.airavata.workflow.model.component.url;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.common.utils.WSDLUtil;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.component.ComponentReference;
import org.apache.airavata.workflow.model.component.ComponentRegistry;
import org.apache.airavata.workflow.model.component.ComponentRegistryException;
import org.apache.airavata.workflow.model.component.ws.WSComponent;

//import xsul.wsdl.WsdlDefinitions;
//import xsul.wsdl.WsdlResolver;

public class URLComponentRegistry extends ComponentRegistry {

    private URI url;

    /**
     * Creates a URLComponentRegistry
     * 
     * @param url
     */
    public URLComponentRegistry(URI url) {
        this.url = url;
    }
    
    /**
     * @see org.apache.airavata.workflow.model.component.registry.ComponentRegistry#getName()
     */
    @Override
    public String getName() {
        return "" + this.url;
    }

    /**
     * @throws ComponentRegistryException
     * @see org.apache.airavata.workflow.model.component.registry.ComponentRegistry#getComponentReferenceList()
     */
    @Override
    public List<ComponentReference> getComponentReferenceList() throws ComponentRegistryException {
        List<ComponentReference> tree = new ArrayList<ComponentReference>();
        try {
            loadComponents(tree);
            return tree;
        } catch (ComponentException e) {
            throw new ComponentRegistryException(e);
        }
    }

    /**
     * @throws ComponentException
     */
    private void loadComponents(List<ComponentReference> tree) throws ComponentException {
        // XXX need to use wsdlResolver from xsul, not xsul5, to handle
        // security.
//        WsdlResolver wsdlResolver = WsdlResolver.getInstance();
//        WsdlDefinitions definitions = wsdlResolver.loadWsdl(this.url);
        //FIXME: to load WSDL
        List<WSComponent> components = null; //WSComponentFactory.createComponents(WSDLUtil.wsdlDefinitions3ToWsdlDefintions5(definitions));
        String urlString = this.url.toString();
        String name = urlString.substring(urlString.lastIndexOf('/') + 1);
        URLComponentReference componentReference = new URLComponentReference(name, components);
	        tree.add(componentReference);
    }
}