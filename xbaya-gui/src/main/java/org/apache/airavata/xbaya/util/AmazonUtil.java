/*
 * Copyright (c) 2011 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: $
 */
package org.apache.airavata.xbaya.util;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.airavata.xbaya.amazonEC2.gui.AmazonCredential;

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

/**
 * @author Patanachai Tangchasiin
 */
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

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2011 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */
