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
package org.apache.airavata.xbaya.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.airavata.xbaya.core.amazon.AmazonCredential;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class AmazonUtil {

    /**
     * Amazon EC2 instance type
     */
    public final static String[] INSTANCE_TYPE = { "t1.micro", "m1.small", "m1.large", "m1.xlarge", "m2.xlarge",
            "m2.2xlarge", "m2.4xlarge", "c1.medium", "c1.xlarge" };

    private static AmazonEC2 getEC2Client() {
        AmazonEC2 ec2 = new AmazonEC2Client(new BasicAWSCredentials(AmazonCredential.getInstance().getAwsAccessKeyId(),
                AmazonCredential.getInstance().getAwsSecretAccessKey()));
        return ec2;
    }

    /**
     * Launch a new EC2 instance
     * 
     * @param AMI_ID
     * @param type
     * @param number
     * @return list of newly launched instances
     */
    public static List<Instance> launchInstance(String AMI_ID, String type, Integer number) {
        List<Instance> resultList = new ArrayList<Instance>();

        RunInstancesRequest request = new RunInstancesRequest(AMI_ID, number, number);
        request.setInstanceType(type);

        RunInstancesResult result = getEC2Client().runInstances(request);
        resultList.addAll(result.getReservation().getInstances());
        return resultList;
    }

    /**
     * Launch a new EC2 instance
     * 
     * @param AMI_ID
     * @param type
     * @param number
     * @param keyname
     * @return list of newly launched instances
     */
    public static List<Instance> launchInstance(String AMI_ID, String type, Integer number, String keyname) {
        List<Instance> resultList = new ArrayList<Instance>();

        RunInstancesRequest request = new RunInstancesRequest(AMI_ID, number, number);
        request.setInstanceType(type);
        request.setKeyName(keyname);

        RunInstancesResult result = getEC2Client().runInstances(request);
        resultList.addAll(result.getReservation().getInstances());
        return resultList;
    }

    /**
     * Load instances
     * 
     * @return list of instances
     */
    public static List<Instance> loadInstances() {
        List<Instance> resultList = new ArrayList<Instance>();
        DescribeInstancesResult describeInstancesResult = getEC2Client().describeInstances();
        List<Reservation> reservations = describeInstancesResult.getReservations();
        for (Iterator<Reservation> iterator = reservations.iterator(); iterator.hasNext();) {
            Reservation reservation = iterator.next();
            for (Instance instance : reservation.getInstances()) {
                resultList.add(instance);
            }
        }
        return resultList;
    }

    /**
     * Load keypairs
     * 
     * @return list of keypairs
     */
    public static List<String> loadKeypairs() {
        List<String> resultList = new ArrayList<String>();
        DescribeKeyPairsResult results = getEC2Client().describeKeyPairs();
        for (KeyPairInfo key : results.getKeyPairs()) {
            resultList.add(key.getKeyName());
        }
        return resultList;
    }

    /**
     * Terminate instances
     * 
     * @param instanceIds
     */
    public static void terminateInstances(List<String> instanceIds) {
        // terminate
        TerminateInstancesRequest request = new TerminateInstancesRequest(instanceIds);
        getEC2Client().terminateInstances(request);
    }

    /**
     * Terminate instances
     * 
     * @param instanceIds
     */
    public static void terminateInstances(String... instanceIds) {
        // terminate
        TerminateInstancesRequest request = new TerminateInstancesRequest();
        getEC2Client().terminateInstances(request.withInstanceIds(instanceIds));
    }

}