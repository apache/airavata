package org.apache.airavata.gfac.ec2;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface SchedulingAlgorithm {

    String getScheduledAmazonInstance(AmazonEC2Client ec2client, String imageId, AWSCredentials credential)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException;
}

