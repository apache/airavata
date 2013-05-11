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

package org.apache.airavata.gfac.provider.utils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.notification.events.EC2ProviderEvent;

import java.util.ArrayList;
import java.util.List;

/* This class holds the commonly used methods to communicate with Amazon EC2 environment*/
public class AmazonEC2Util {

    public static final int SLEEP_TIME_SECOND = 120;

    /**
     * Starts an Amazon instance with the given information.
     *
     * @param ec2 Amazon ec2 client
     * @param amiId Amazon Machine Image (AMI) id
     * @param insType Instance type
     * @param jobExecutionContext Job Execution context
     * @param keyPairName Key pair name
     * @return list of instances
     * @throws AmazonServiceException AmazonServiceException
     */
    public static List<Instance> startInstances(AmazonEC2Client ec2, String amiId, String insType,
                                          JobExecutionContext jobExecutionContext, String keyPairName)
            throws AmazonServiceException {
        // start only 1 instance
        RunInstancesRequest request = new RunInstancesRequest(amiId, 1, 1);
        request.setKeyName(keyPairName);
        request.setInstanceType(insType);

        RunInstancesResult result = ec2.runInstances(request);

        List<Instance> instances = result.getReservation().getInstances();

        while (!allInstancesStateEqual(instances, InstanceStateName.Running)) {

            // instance status should not be Terminated
            if (anyInstancesStateEqual(instances, InstanceStateName.Terminated)) {
                throw new AmazonClientException("Some Instance is terminated before running a job");
            }

            // notify the status
            for (Instance ins: instances) {
                jobExecutionContext.getNotificationService().publish(new EC2ProviderEvent("EC2 Instance " +
                        ins.getInstanceId() + " is " + ins.getState().getName()));
            }

            try {
                Thread.sleep(SLEEP_TIME_SECOND * 1000l);
            } catch (Exception ex) {
                // no op
            }

            DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
            describeInstancesRequest.setInstanceIds(getInstanceIDs(instances));

            DescribeInstancesResult describeInstancesResult = ec2.describeInstances(describeInstancesRequest);
            instances = describeInstancesResult.getReservations().get(0).getInstances();
        }

        return instances;
    }

    public static boolean anyInstancesStateEqual(List<Instance> instances, InstanceStateName name) {
        for (Instance instance : instances) {
            // if one of instance is not running, return false
            if (InstanceStateName.fromValue(instance.getState().getName()) == name) {
                return true;
            }
        }
        return false;
    }

    public static boolean allInstancesStateEqual(List<Instance> instances, InstanceStateName name) {
        for (Instance instance : instances) {
            // if one of instance is not running, return false
            if (InstanceStateName.fromValue(instance.getState().getName()) != name) {
                return false;
            }
        }
        return true;
    }

    public static List<String> getInstanceIDs(List<Instance> instances) {
        List<String> ret = new ArrayList<String>();
        for (Instance instance : instances) {
            ret.add(instance.getInstanceId());
        }
        return ret;
    }
}
