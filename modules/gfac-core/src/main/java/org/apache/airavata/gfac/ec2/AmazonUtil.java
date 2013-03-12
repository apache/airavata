package org.apache.airavata.gfac.ec2;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AmazonUtil {

    /* Amazon EC2 instance type */
    public final static String[] INSTANCE_TYPE =
            { "t1.micro", "m1.small", "m1.large", "m1.xlarge", "m2.xlarge", "m2.2xlarge",
                    "m2.4xlarge", "c1.medium", "c1.xlarge" };

    private static AmazonEC2 getEC2Client() {
        // TODO Heshan : Fix this properly after adding UI components.
        String accessKey = "";
        String secretKey = "";
        AmazonEC2 ec2 = new AmazonEC2Client(new BasicAWSCredentials(accessKey, secretKey));
        return ec2;
    }

    /**
     * Launch a new EC2 instance
     *
     * @param amiId
     * @param type
     * @param number
     * @return list of newly launched instances
     */
    public static List<Instance> launchInstance(String amiId, String type, Integer number) {
        List<Instance> resultList = new ArrayList<Instance>();

        RunInstancesRequest request = new RunInstancesRequest(amiId, number, number);
        request.setInstanceType(type);

        RunInstancesResult result = getEC2Client().runInstances(request);
        resultList.addAll(result.getReservation().getInstances());
        return resultList;
    }

    /**
     * Launch a new EC2 instance
     *
     * @param amiId
     * @param type
     * @param number
     * @param keyname
     * @return list of newly launched instances
     */
    public static List<Instance> launchInstance(String amiId, String type, Integer number, String keyname) {
        List<Instance> resultList = new ArrayList<Instance>();

        RunInstancesRequest request = new RunInstancesRequest(amiId, number, number);
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
     * Load key pairs
     *
     * @return list of keypairs
     */
    public static List<String> loadKeypairs(){
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
     * @param instanceIds instance ids of the running instances.
     */
    public static void terminateInstances(List<String> instanceIds) {
        // terminate
        TerminateInstancesRequest request = new TerminateInstancesRequest(instanceIds);
        getEC2Client().terminateInstances(request);
    }

    /**
     * Terminate instances
     *
     * @param instanceIds  instance ids of the running instances.
     */
    public static void terminateInstances(String... instanceIds) {
        // terminate
        TerminateInstancesRequest request = new TerminateInstancesRequest();
        getEC2Client().terminateInstances(request.withInstanceIds(instanceIds));
    }

}
