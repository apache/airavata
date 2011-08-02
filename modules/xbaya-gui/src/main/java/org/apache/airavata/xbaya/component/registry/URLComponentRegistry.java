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

package org.apache.airavata.xbaya.component.registry;

import java.net.URI;
import java.util.List;

import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.gui.ComponentTreeNode;
import org.apache.airavata.xbaya.component.ws.WSComponent;
import org.apache.airavata.xbaya.component.ws.WSComponentFactory;
import org.apache.airavata.xbaya.util.WSDLUtil;

import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlResolver;

public class URLComponentRegistry extends ComponentRegistry {

    // private static final MLogger logger = MLogger.getLogger();

    private ComponentTreeNode tree;

    private URI url;

    /**
     * Creates a URLComponentRegistry
     * 
     * @param url
     */
    public URLComponentRegistry(URI url) {
        this.url = url;
        this.tree = new ComponentTreeNode(this);
    }

    /**
     * @see org.apache.airavata.xbaya.component.registry.ComponentRegistry#getName()
     */
    @Override
    public String getName() {
        return "" + this.url;
    }

    /**
     * @throws ComponentRegistryException
     * @see org.apache.airavata.xbaya.component.registry.ComponentRegistry#getComponentTree()
     */
    @Override
    public ComponentTreeNode getComponentTree() throws ComponentRegistryException {
        this.tree = new ComponentTreeNode(this);
        try {
            loadComponents();
            return this.tree;
        } catch (ComponentException e) {
            throw new ComponentRegistryException(e);
        }
    }

    /**
     * @return The tree.
     */
    public ComponentTreeNode getComponentTreeWithoutRefresh() {
        return this.tree;
    }

    /**
     * @throws ComponentException
     */
    private void loadComponents() throws ComponentException {
        // XXX need to use wsdlResolver from xsul, not xsul5, to handle
        // security.
        WsdlResolver wsdlResolver = WsdlResolver.getInstance();
        WsdlDefinitions definitions = wsdlResolver.loadWsdl(this.url);
        List<WSComponent> components = WSComponentFactory.createComponents(WSDLUtil
                .wsdlDefinitions3ToWsdlDefintions5(definitions));
        String urlString = this.url.toString();
        String name = urlString.substring(urlString.lastIndexOf('/') + 1);
        URLComponentReference componentReference = new URLComponentReference(name, components);
        ComponentTreeNode treeLeaf = new ComponentTreeNode(componentReference);
        this.tree.add(treeLeaf);
    }
}