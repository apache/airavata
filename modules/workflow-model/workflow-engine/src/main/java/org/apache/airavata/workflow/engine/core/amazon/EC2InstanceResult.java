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
package org.apache.airavata.workflow.engine.core.amazon;
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.xbaya.core.amazon;
//
//import org.apache.airavata.xbaya.ui.widgets.TableRenderable;
//
//import com.amazonaws.services.ec2.model.Instance;
//
//public class EC2InstanceResult implements TableRenderable {
//
//    private static String[] columnName = { "Instance", "AMI ID", "Root Device", "Type", "Status", "Key Pair Name",
//            "Monitoring", "Virtualization", "Placement Group" };
//
//    private Instance instance;
//
//    /**
//     * 
//     * Constructs a EC2InstancesResult.
//     * 
//     * @param ins
//     */
//    public EC2InstanceResult(Instance ins) {
//        this.instance = ins;
//    }
//
//    public Instance getInstance() {
//        return this.instance;
//    }
//
//    /**
//     * @see org.apache.airavata.xbaya.ui.widgets.TableRenderable#getColumnCount()
//     */
//    @Override
//    public int getColumnCount() {
//        return 8;
//    }
//
//    /**
//     * @see org.apache.airavata.xbaya.ui.widgets.TableRenderable#getColumnTitle(int)
//     */
//    @Override
//    public String getColumnTitle(int index) {
//        return columnName[index];
//    }
//
//    /**
//     * @see org.apache.airavata.xbaya.ui.widgets.TableRenderable#getValue(int)
//     */
//    @Override
//    public Object getValue(int index) {
//        switch (index) {
//        case 0:
//            return this.instance.getInstanceId();
//        case 1:
//            return this.instance.getImageId();
//        case 2:
//            return this.instance.getRootDeviceType();
//        case 3:
//            return this.instance.getInstanceType();
//        case 4:
//            return this.instance.getState().getName();
//        case 5:
//            return this.instance.getKeyName();
//        case 6:
//            return this.instance.getMonitoring().getState();
//        case 7:
//            return this.instance.getVirtualizationType();
//        case 8:
//            return this.instance.getPlacement().getGroupName();
//        default:
//            return null;
//        }
//    }
//
//}