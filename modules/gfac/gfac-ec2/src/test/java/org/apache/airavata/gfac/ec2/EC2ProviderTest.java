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

import org.apache.airavata.commons.gfac.type.*;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.ApplicationContext;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.context.MessageContext;
import org.apache.airavata.gfac.core.cpi.GFacImpl;
import org.apache.airavata.schemas.gfac.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Your Amazon instance should be in a running state before running this test.
 */
public class EC2ProviderTest {
    private JobExecutionContext jobExecutionContext;

    private static final String hostName = "ec2-host";

    private static final String hostAddress = "ec2-address";

    private static final String sequence1 = "RR042383.21413#CTGGCACGGAGTTAGCCGATCCTTATTCATAAAGTACATGCAAACGGGTATCCATA" +
            "CTCGACTTTATTCCTTTATAAAAGAAGTTTACAACCCATAGGGCAGTCATCCTTCACGCTACTTGGCTGGTTCAGGCCTGCGCCCATTGACCAATATTCCTCA" +
            "CTGCTGCCTCCCGTAGGAGTTTGGACCGTGTCTCAGTTCCAATGTGGGGGACCTTCCTCTCAGAACCCCTATCCATCGAAGACTAGGTGGGCCGTTACCCCGC" +
            "CTACTATCTAATGGAACGCATCCCCATCGTCTACCGGAATACCTTTAATCATGTGAACATGCGGACTCATGATGCCATCTTGTATTAATCTTCCTTTCAGAAG" +
            "GCTGTCCAAGAGTAGACGGCAGGTTGGATACGTGTTACTCACCGTGCCGCCGGTCGCCATCAGTCTTAGCAAGCTAAGACCATGCTGCCCCTGACTTGCATGT" +
            "GTTAAGCCTGTAGCTTAGCGTTC";

    private static final String sequence2 = "RR042383.31934#CTGGCACGGAGTTAGCCGATCCTTATTCATAAAGTACATGCAAACGGGTATCCATA" +
            "CCCGACTTTATTCCTTTATAAAAGAAGTTTACAACCCATAGGGCAGTCATCCTTCACGCTACTTGGCTGGTTCAGGCTCTCGCCCATTGACCAATATTCCTCA" +
            "CTGCTGCCTCCCGTAGGAGTTTGGACCGTGTCTCAGTTCCAATGTGGGGGACCTTCCTCTCAGAACCCCTATCCATCGAAGACTAGGTGGGCCGTTACCCCGC" +
            "CTACTATCTAATGGAACGCATCCCCATCGTCTACCGGAATACCTTTAATCATGTGAACATGCGGACTCATGATGCCATCTTGTATTAAATCTTCCTTTCAGAA" +
            "GGCTATCCAAGAGTAGACGGCAGGTTGGATACGTGTTACTCACCGTGCG";

    /* Following variables are needed to be set in-order to run the test. Since these are account specific information,
       I'm not adding the values here. It's the responsibility of the person who's running the test to update
       these variables accordingly.
       */

    /* Username used to log into your ec2 instance eg.ec2-user */
    private String userName = "";

    /* Secret key used to connect to the image */
    private String secretKey = "";

    /* Access key used to connect to the image */
    private String accessKey = "";

    /* Instance id of the running instance of your image */
    private String instanceId = "";

    @Before
    public void setUp() throws Exception {
        URL resource = EC2ProviderTest.class.getClassLoader().getResource(org.apache.airavata.common.utils.Constants.GFAC_CONFIG_XML);
        assert resource != null;
        System.out.println(resource.getFile());
        GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()), null, null);

        /* EC2 Host */
        HostDescription host = new HostDescription(Ec2HostType.type);
        host.getType().setHostName(hostName);
        host.getType().setHostAddress(hostAddress);

        /* App */
        ApplicationDescription ec2Desc = new ApplicationDescription(Ec2ApplicationDeploymentType.type);
        Ec2ApplicationDeploymentType ec2App = (Ec2ApplicationDeploymentType)ec2Desc.getType();

        String serviceName = "Gnome_distance_calculation_workflow";
        ec2Desc.getType().addNewApplicationName().setStringValue(serviceName);
        ec2App.setJobType(JobTypeType.EC_2);
        ec2App.setExecutable("/home/ec2-user/run.sh");
        ec2App.setExecutableType("sh");

        /* Service */
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName("GenomeEC2");

        List<InputParameterType> inputList = new ArrayList<InputParameterType>();

        InputParameterType input1 = InputParameterType.Factory.newInstance();
        input1.setParameterName("genome_input1");
        input1.setParameterType(StringParameterType.Factory.newInstance());
        inputList.add(input1);

        InputParameterType input2 = InputParameterType.Factory.newInstance();
        input2.setParameterName("genome_input2");
        input2.setParameterType(StringParameterType.Factory.newInstance());
        inputList.add(input2);

        InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList.size()]);

        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
        OutputParameterType output = OutputParameterType.Factory.newInstance();
        output.setParameterName("genome_output");
        output.setParameterType(StringParameterType.Factory.newInstance());
        outputList.add(output);

        OutputParameterType[] outputParamList = outputList
                .toArray(new OutputParameterType[outputList.size()]);

        serv.getType().setInputParametersArray(inputParamList);
        serv.getType().setOutputParametersArray(outputParamList);

        jobExecutionContext = new JobExecutionContext(gFacConfiguration,serv.getType().getName());
        ApplicationContext applicationContext = new ApplicationContext();
        jobExecutionContext.setApplicationContext(applicationContext);
        applicationContext.setServiceDescription(serv);
        applicationContext.setApplicationDeploymentDescription(ec2Desc);
        applicationContext.setHostDescription(host);

        AmazonSecurityContext amazonSecurityContext =
                new AmazonSecurityContext(userName, accessKey, secretKey, instanceId);
        jobExecutionContext.addSecurityContext(AmazonSecurityContext.AMAZON_SECURITY_CONTEXT, amazonSecurityContext);

        MessageContext inMessage = new MessageContext();
        ActualParameter genomeInput1 = new ActualParameter();
        ((StringParameterType)genomeInput1.getType()).setValue(sequence1);
        inMessage.addParameter("genome_input1", genomeInput1);

        ActualParameter genomeInput2 = new ActualParameter();
        ((StringParameterType)genomeInput2.getType()).setValue(sequence2);
        inMessage.addParameter("genome_input2", genomeInput2);

        MessageContext outMessage = new MessageContext();
        ActualParameter echo_out = new ActualParameter();
        outMessage.addParameter("distance", echo_out);

        jobExecutionContext.setInMessageContext(inMessage);
        jobExecutionContext.setOutMessageContext(outMessage);
    }

    @Test
    public void testGramProvider() throws GFacException {
        GFacImpl gFacAPI = new GFacImpl();
        gFacAPI.submitJob(jobExecutionContext.getExperimentID(), jobExecutionContext.getTaskData().getTaskID(), jobExecutionContext.getGatewayID());
        MessageContext outMessageContext = jobExecutionContext.getOutMessageContext();
        Assert.assertEquals(MappingFactory.
                toString((ActualParameter) outMessageContext.getParameter("genome_output")), "476");
    }
}


