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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.airavata.common.utils.IOUtil;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.gui.ComponentTreeNode;
import org.apache.airavata.xbaya.component.ws.WSComponent;
import org.apache.airavata.xbaya.component.ws.WSComponentFactory;

public class LocalComponentRegistry extends ComponentRegistry {

    private File directory;

    /**
     * Creates a LocalComponentRegistryClient
     * 
     * @param directory
     */
    public LocalComponentRegistry(String directory) {
        this(new File(directory));
    }

    /**
     * Creates a WebComponentRegistryClient
     * 
     * @param directory
     *            The path of the directory that contains the component files.
     */
    public LocalComponentRegistry(File directory) {
        this.directory = directory;
    }

    /**
     * @see org.apache.airavata.xbaya.component.registry.ComponentRegistry#getName()
     */
    @Override
    public String getName() {
        return this.directory.toString();
    }

    /**
     * Returns a ComponentTree.
     * 
     * @return The ComponentTree
     * @throws ComponentRegistryException
     */
    @Override
    public ComponentTreeNode getComponentTree() throws ComponentRegistryException {
        try {
            ComponentTreeNode componentTree = getComponentTree(this.directory);
            componentTree.setComponentRegistry(this);
            return componentTree;
        } catch (RuntimeException e) {
            throw new ComponentRegistryException(e);
        }
    }

    /**
     * Returns a component of a specified name.
     * 
     * @param name
     *            The name of the component. The name here is a file path relative to the directory.
     * @return The component of a specified name
     * @throws ComponentException
     * @throws ComponentRegistryException
     */
    public WSComponent getComponent(String name) throws ComponentException, ComponentRegistryException {
        // This method is used only by unit tests.
        File file = new File(this.directory, name);
        return getComponents(file).get(0);
    }

    /**
     * @param file
     * @return The list of components defined in the specified file.
     * @throws ComponentException
     * @throws ComponentRegistryException
     */
    public List<WSComponent> getComponents(File file) throws ComponentException, ComponentRegistryException {
        try {
            String compString = IOUtil.readFileToString(file);
            List<WSComponent> components = WSComponentFactory.createComponents(compString);
            return components;
        } catch (IOException e) {
            throw new ComponentRegistryException(e);
        }
    }

    private ComponentTreeNode getComponentTree(File dir) {
        if (!dir.isDirectory()) {
            throw new XBayaRuntimeException(dir + "is not a directory.");
        }

        boolean found = false;
        ComponentTreeNode tree = new ComponentTreeNode(dir.getName());
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            if (file.isDirectory()) {
                ComponentTreeNode subTree = getComponentTree(file);
                if (subTree != null) {
                    found = true;
                    tree.add(subTree);
                }
            } else if (fileName.endsWith(XBayaConstants.XML_SUFFIX) || fileName.endsWith(XBayaConstants.WSDL_SUFFIX)
                    || fileName.endsWith(XBayaConstants.WSDL_SUFFIX2)) {
                found = true;
                LocalComponentReference componentReference = new LocalComponentReference(file.getName(), file, this);
                ComponentTreeNode treeLeaf = new ComponentTreeNode(componentReference);
                treeLeaf.setAllowsChildren(false);
                tree.add(treeLeaf);
            }
        }
        if (!found) {
            // Doesn't show a directory that doesn't have any components.
            tree = null;
        }
        return tree;
    }
}