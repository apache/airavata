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
package org.apache.airavata.xbaya.ui.widgets.amazon;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class S3TreeModel extends DefaultTreeModel {

    private static S3TreeModel instance;
    private static DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("S3 Contents");

    private boolean connected;

    /**
     * Constructs a S3TreeModel.
     * 
     * @param root
     */
    private S3TreeModel() {
        super(rootNode);
    }

    public static S3TreeModel getInstance() {
        if (instance == null) {
            instance = new S3TreeModel();
        }
        return instance;
    }

    public S3TreeModel clean() {
        rootNode.removeAllChildren();
        setRoot(rootNode);
        connected = false;
        return instance;
    }

    public void connect() {
        this.connected = true;
    }

    public boolean isConnected() {
        return this.connected;
    }

}