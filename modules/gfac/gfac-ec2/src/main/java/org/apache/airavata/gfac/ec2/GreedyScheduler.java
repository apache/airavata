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

package org.apache.airavata.gfac.ec2;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GreedyScheduler implements SchedulingAlgorithm {

    /**
     * Returns the amazon instance id of the amazon instance which is having the minimum
     * CPU utilization (out of the already running instances). If the instance which
     * is having the minimum CPU utilization exceeds 80%, ami-id will be returned
     * instead of a an instance id. If a particular running instance's uptime is
     * greater than 55 minutes, that instance will be shut down.
     *
     * @return instance id
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.spec.InvalidKeySpecException
     * @throws java.io.IOException
     */
    public String getScheduledAmazonInstance(AmazonEC2Client ec2client, String imageId, AWSCredentials credential)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {

        Map<String, Double> instanceUtilMap = new HashMap<String, Double>();
        List<Instance> instanceList = AmazonInstanceScheduler.loadInstances(ec2client);
        // If there are no instances created at this point return the imageId
        if(instanceList.isEmpty()){
            return imageId;
        }

        for (Instance instance : instanceList) {
            String instanceImageId = instance.getImageId();
            String instanceId = instance.getInstanceId();
            double avgCPUUtilization = AmazonInstanceScheduler.monitorInstance(credential, instanceId);

            System.out.println("Image id         : " + instanceImageId);
            System.out.println("Instance id      : " + instanceId);
            System.out.println("CPU Utilization  : " + avgCPUUtilization);

            //Storing the instance id, if that particular instance was created by the given AMI(imageId)
            if(imageId.equalsIgnoreCase(instanceImageId)) {
                instanceUtilMap.put(instanceId, avgCPUUtilization);
            }
        }

        // Selects the instance with minimum CPU utilization
        Map.Entry<String, Double> min = null;
        for (Map.Entry<String, Double> entry : instanceUtilMap.entrySet()) {
            if (min == null || min.getValue() > entry.getValue()) {
                min = entry;
            }
        }

        if((min!=null) && (min.getValue()<80)) {
            System.out.println("Use the existing instance " + min.getKey() + " with CPU Utilization : " + min.getValue());
            return min.getKey();
        } else {
            System.out.println("Create a new instance using AMI : " + imageId);
            return imageId;
        }
    }

}

