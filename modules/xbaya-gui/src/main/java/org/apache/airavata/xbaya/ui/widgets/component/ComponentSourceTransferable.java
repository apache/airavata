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
package org.apache.airavata.xbaya.ui.widgets.component;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import org.apache.airavata.workflow.model.component.ComponentReference;

/**
 * To transfer ComponentSource by drag-and-drop. This is really over-spec, but this is the only way.
 * 
 */
public class ComponentSourceTransferable implements Transferable {

    /**
     * FLAVOR
     */
    public static final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType,
            ComponentReference.class.toString());

    private ComponentReference componentReference;

    /**
     * Constructs a ComponentSourceTransferable.
     * 
     * @param componentReference
     */
    public ComponentSourceTransferable(ComponentReference componentReference) {
        this.componentReference = componentReference;
    }

    /**
     * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
     */
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { FLAVOR };
    }

    /**
     * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(FLAVOR);
    }

    /**
     * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
     */
    public ComponentReference getTransferData(DataFlavor flavor) {
        return this.componentReference;
    }

}