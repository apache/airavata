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

package org.apache.airavata.xbaya.experiment.gui;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.namespace.QName;

import org.apache.airavata.commons.gfac.type.util.SchemaUtil;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.gui.TableRenderable;

public class OGCEXRegistrySearchResult implements TableRenderable {

    private static String[] columnName = { "Name", "Description" };

    private QName qname;

    private QName resourceID;

    private String resourceName;

    private String description;

    private Node data;

    /**
     * Constructs a OGCEXRegistrySearchResult.
     *
     * @param node
     */

    public OGCEXRegistrySearchResult(Node node) {
        try {
            String property = node.getProperty("Type").getString();
            if (property.equals(XBayaConstants.REGISTRY_TYPE_HOST_DESC)) {
                //todo
            } else if (property.equals(XBayaConstants.REGISTRY_TYPE_APPLICATION_DESC)) {
                //todo
            } else if (property.equals(XBayaConstants.REGISTRY_TYPE_SERVICE_DESC)) {
                //todo
            } else if (property.equals(XBayaConstants.REGISTRY_TYPE_WORKFLOW)) {
//                this.qname = new ;
                this.resourceID = new QName(node.getProperty("NamespaceURI").getString(),node.getProperty("LocalPart").getString(),node.getProperty("Prefix").getString());
                this.description = node.getProperty("Description").getString();
                this.resourceName = node.getName();
            }


        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the qname.
     * 
     * @return The resourceID
     */
    public QName getQname() {
        return this.qname;
    }

    /**
     * Returns the resourceId.
     * 
     * @return The resourceID
     */
    public QName getResourceId() {
        return this.resourceID;
    }

    /**
     * Returns the description.
     * 
     * @return The description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the resourceName.
     * 
     * @return The resourceName
     */
    public String getResourceName() {
        return this.resourceName;
    }

    /**
     * Returns the data.
     *
     * @return The data
     */
    public Node getData() {
        return this.data;
    }

    /**
     * @see org.apache.airavata.xbaya.gui.TableRenderable#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return 2;
    }

    /**
     * @see org.apache.airavata.xbaya.gui.TableRenderable#getColumnTitle(int)
     */
    @Override
    public String getColumnTitle(int index) {
        return columnName[index];
    }

    /**
     * @see org.apache.airavata.xbaya.gui.TableRenderable#getValue(int)
     */
    @Override
    public Object getValue(int index) {
        switch (index) {
        case 0:
            return getResourceName();
        case 1:
            return getDescription();
        default:
            return null;
        }
    }

}