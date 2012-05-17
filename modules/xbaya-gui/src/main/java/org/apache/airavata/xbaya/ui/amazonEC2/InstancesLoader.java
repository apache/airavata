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

package org.apache.airavata.xbaya.ui.amazonEC2;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.Cancelable;
import org.apache.airavata.xbaya.ui.WaitDialog;
import org.apache.airavata.xbaya.ui.XbayaEnhancedList;
import org.apache.airavata.xbaya.util.AmazonUtil;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.Instance;

public class InstancesLoader implements Cancelable {

    private XBayaEngine engine;
    private JDialog parent;

    private boolean canceled;

    private WaitDialog loadingDialog;

    /**
     * 
     * Constructs a InstancesLoader.
     * 
     * @param engine
     * @param parent
     */
    public InstancesLoader(XBayaEngine engine, JDialog parent) {
        this.engine = engine;
        this.parent = parent;
        this.loadingDialog = new WaitDialog(this, "Loading EC2 Instances.", "Loading EC2 Instances.\n"
                + "Please wait for a moment.", this.engine);
    }

    /**
     * @see org.apache.airavata.xbaya.ui.Cancelable#cancel()
     */
    @Override
    public void cancel() {
        this.canceled = true;
    }

    /**
     * 
     * @param list
     */
    public void load(final XbayaEnhancedList<EC2InstanceResult> list) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    List<EC2InstanceResult> tmpList = new ArrayList<EC2InstanceResult>();

                    List<Instance> instances = AmazonUtil.loadInstances();
                    for (Instance instance : instances) {
                        tmpList.add(new EC2InstanceResult(instance));
                    }

                    if (!InstancesLoader.this.canceled) {
                        list.setListData(tmpList);
                    }

                } catch (AmazonServiceException ex) {
                    InstancesLoader.this.engine.getErrorWindow().error(InstancesLoader.this.parent,
                            "Cannot load EC2 instances", ex);
                } catch (AmazonClientException ex) {
                    InstancesLoader.this.engine.getErrorWindow().error(InstancesLoader.this.parent,
                            "Cannot load EC2 instances", ex);
                } finally {
                    InstancesLoader.this.loadingDialog.hide();
                }
            }
        }).start();

        this.loadingDialog.show();
    }
}