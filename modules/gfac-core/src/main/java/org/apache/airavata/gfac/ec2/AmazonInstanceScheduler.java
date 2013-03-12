package org.apache.airavata.gfac.ec2;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AmazonInstanceScheduler {
    private static final Logger log = LoggerFactory.getLogger(AmazonInstanceScheduler.class);

    /* Maximum number of instances that the Scheduler will create*/
    //private static final int MAX_INSTANCE_COUNT = 3;

    /* Maximum number of minutes an instance should be kept alive*/
    public static final int INSTANCE_UP_TIME_THRESHOLD = 60;

    private static volatile AmazonInstanceScheduler scheduler = null;

    private static String imageId = null;

    private static AWSCredentials credential = null;

    private static AmazonEC2Client ec2client = null;

    /* The time interval(minutes) in which the instances will be checked whether they have timed-out*/
    public static final long TERMINATE_THREAD_UPDATE_INTERVAL = 5;

    public static AmazonInstanceScheduler getInstance(String imageId, String accessKey, String secretKey)
            throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {

        if(scheduler == null) {
            synchronized (AmazonInstanceScheduler.class) {
                if(scheduler == null) {
                    new Thread() {
                        @Override
                        public void run() {
                            //noinspection InfiniteLoopStatement
                            while(true) {
                                try {
                                    Thread.sleep(TERMINATE_THREAD_UPDATE_INTERVAL * 60 * 1000);
                                } catch (InterruptedException e ) {
                                    // do-nothing
                                }

                                try {
                                    terminateTimedOutAmazonInstances();
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                    }.start();

                    scheduler = new AmazonInstanceScheduler();
                }
            }
        }

        AmazonInstanceScheduler.imageId = imageId;
        AmazonInstanceScheduler.credential = new BasicAWSCredentials(accessKey, secretKey);
        AmazonInstanceScheduler.ec2client = new AmazonEC2Client(credential);

        return scheduler;
    }


    /**
     * Returns the amazon instance id of the amazon instance which is having the minimum
     * CPU utilization (out of the already running instances). If the instance which
     * is having the minimum CPU utilization exceeds 80%, ami-id will be returned
     * instead of a an instance id. If a particular running instance's uptime is
     * greater than 55 minutes, that instance will be shut down.
     *
     * @return instance id
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IOException
     */
    public String getScheduledAmazonInstance()
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {

        SchedulingAlgorithm greedyAglo = new GreedyScheduler();
        return greedyAglo.getScheduledAmazonInstance(ec2client,imageId, credential);
    }

    /**
     * Terminates the Amazon instances that are timed out. Timed out refers to the
     * instances which have been running for more than the INSTANCE_UP_TIME_THRESHOLD.
     */
    private static void terminateTimedOutAmazonInstances(){
        System.out.println("Checking for timed-out instances");
        List<Instance> instanceList = loadInstances(ec2client);
        for (Instance instance : instanceList) {
            String instanceId = instance.getInstanceId();

            long upTime = getInstanceUptime(instance);
            // if the instance up time is greater than the threshold, terminate the instance
            if (upTime > INSTANCE_UP_TIME_THRESHOLD) {
                List<String> requestIds = new ArrayList<String>();
                requestIds.add(instanceId);
                // terminate instance
                System.out.println("Terminating the instance " + instanceId +
                        " as the up time threshold is exceeded");
                AmazonUtil.terminateInstances(requestIds);
            }
        }

    }

    /**
     * Calculates the instance up time in minutes.
     *
     * @param instance instance to be monitored.
     * @return up time of the instance.
     */
    private static long getInstanceUptime(Instance instance) {
        Date startTime = instance.getLaunchTime();
        Date today = new Date();
        long diff = (today.getTime() - startTime.getTime()) / (1000 * 60);
        System.out.println("Instance launch time   : " + startTime);
        System.out.println("Instance up time (mins): " + diff);
        return diff;
    }

    /**
     * Monitors the CPU Utilization using Amazon Cloud Watch. In order to monitor the instance, Cloud Watch Monitoring
     * should be enabled for the running instance.
     *
     * @param credential EC2 credentials
     * @param instanceId instance id
     * @return average CPU utilization of the instance
     */
    public static double monitorInstance(AWSCredentials credential, String instanceId) {
        try {
            AmazonCloudWatchClient cw = new AmazonCloudWatchClient(credential) ;

            long offsetInMilliseconds = 1000 * 60 * 60 * 24;
            GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
                    .withStartTime(new Date(new Date().getTime() - offsetInMilliseconds))
                    .withNamespace("AWS/EC2")
                    .withPeriod(60 * 60)
                    .withDimensions(new Dimension().withName("InstanceId").withValue(instanceId))
                    .withMetricName("CPUUtilization")
                    .withStatistics("Average", "Maximum")
                    .withEndTime(new Date());
            GetMetricStatisticsResult getMetricStatisticsResult = cw.getMetricStatistics(request);

            double avgCPUUtilization = 0;
            List dataPoint = getMetricStatisticsResult.getDatapoints();
            for (Object aDataPoint : dataPoint) {
                Datapoint dp = (Datapoint) aDataPoint;
                avgCPUUtilization = dp.getAverage();
                log.info(instanceId + " instance's average CPU utilization : " + dp.getAverage());
            }

            return avgCPUUtilization;

        } catch (AmazonServiceException ase) {
            log.error("Caught an AmazonServiceException, which means the request was made  "
                    + "to Amazon EC2, but was rejected with an error response for some reason.");
            log.error("Error Message:    " + ase.getMessage());
            log.error("HTTP Status Code: " + ase.getStatusCode());
            log.error("AWS Error Code:   " + ase.getErrorCode());
            log.error("Error Type:       " + ase.getErrorType());
            log.error("Request ID:       " + ase.getRequestId());

        }
        return 0;
    }

    /**
     * Load instances associated with the given ec2 client
     *
     * @param ec2client ec2 client
     * @return list of instances
     */
    public static List<Instance> loadInstances(AmazonEC2Client ec2client) {
        List<Instance> resultList = new ArrayList<Instance>();
        DescribeInstancesResult describeInstancesResult = ec2client.describeInstances();
        List<Reservation> reservations = describeInstancesResult.getReservations();
        for (Reservation reservation : reservations) {
            for (Instance instance : reservation.getInstances()) {
                System.out.println("instance       : " + instance);
                if ("running".equalsIgnoreCase(instance.getState().getName())) {
                    resultList.add(instance);
                }
            }
        }
        return resultList;
    }

}

