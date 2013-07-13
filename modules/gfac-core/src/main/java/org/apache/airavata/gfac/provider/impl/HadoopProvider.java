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

package org.apache.airavata.gfac.provider.impl;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Map;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.gfac.provider.utils.HadoopUtils;
import org.apache.airavata.schemas.gfac.HadoopApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Executes hadoop job using the cluster configuration provided by handlers in
 * in-flow.
 */
public class HadoopProvider implements GFacProvider{
    private static final Logger logger = LoggerFactory.getLogger(HadoopProvider.class);

    private boolean isWhirrBasedDeployment = false;
    private File hadoopConfigDir;

    public void initialize(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        MessageContext inMessageContext = jobExecutionContext.getInMessageContext();
        if(inMessageContext.getParameter("HADOOP_DEPLOYMENT_TYPE").equals("WHIRR")){
            isWhirrBasedDeployment = true;
        } else {
            String hadoopConfigDirPath = (String)inMessageContext.getParameter("HADOOP_CONFIG_DIR");
            File hadoopConfigDir = new File(hadoopConfigDirPath);
            if (!hadoopConfigDir.exists()){
                throw new GFacProviderException("Specified hadoop configuration directory doesn't exist.");
            } else if (FileUtils.listFiles(hadoopConfigDir, null, null).size() <= 0){
                throw new GFacProviderException("Cannot find any hadoop configuration files inside specified directory.");
            }

            this.hadoopConfigDir = hadoopConfigDir;
        }
    }

    public void execute(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        HadoopApplicationDeploymentDescriptionType hadoopAppDesc =
                (HadoopApplicationDeploymentDescriptionType)jobExecutionContext
                        .getApplicationContext().getApplicationDeploymentDescription().getType();
        MessageContext inMessageContext = jobExecutionContext.getInMessageContext();
        HadoopApplicationDeploymentDescriptionType.HadoopJobConfiguration jobConf = hadoopAppDesc.getHadoopJobConfiguration();

        try{
            // Preparing Hadoop configuration
            Configuration hadoopConf = HadoopUtils.createHadoopConfiguration(
                    jobExecutionContext, isWhirrBasedDeployment, hadoopConfigDir);

            // Load jar containing map-reduce job implementation
            ArrayList<URL> mapRedJars = new ArrayList<URL>();
            mapRedJars.add(new File(jobConf.getJarLocation()).toURL());
            URLClassLoader childClassLoader = new URLClassLoader(mapRedJars.toArray(new URL[mapRedJars.size()]),
                    this.getClass().getClassLoader());

            Job job = new Job(hadoopConf);

            job.setJobName(jobConf.getJobName());

            job.setOutputKeyClass(Class.forName(jobConf.getOutputKeyClass(), true, childClassLoader));
            job.setOutputValueClass(Class.forName(jobConf.getOutputValueClass(), true, childClassLoader));

            job.setMapperClass((Class<? extends Mapper>)Class.forName(jobConf.getMapperClass(), true, childClassLoader));
            job.setCombinerClass((Class<? extends Reducer>) Class.forName(jobConf.getCombinerClass(), true, childClassLoader));
            job.setReducerClass((Class<? extends Reducer>) Class.forName(jobConf.getCombinerClass(), true, childClassLoader));

            job.setInputFormatClass((Class<? extends InputFormat>)Class.forName(jobConf.getInputFormatClass(), true, childClassLoader));
            job.setOutputFormatClass((Class<? extends OutputFormat>) Class.forName(jobConf.getOutputFormatClass(), true, childClassLoader));

            FileInputFormat.setInputPaths(job, new Path(hadoopAppDesc.getInputDataDirectory()));
            FileOutputFormat.setOutputPath(job, new Path(hadoopAppDesc.getOutputDataDirectory()));

            job.waitForCompletion(true);
            System.out.println(job.getTrackingURL());
            if(jobExecutionContext.getOutMessageContext() == null){
                jobExecutionContext.setOutMessageContext(new MessageContext());
            }

            OutputParameterType[] outputParametersArray = jobExecutionContext.getApplicationContext().
                    getServiceDescription().getType().getOutputParametersArray();
            for(OutputParameterType outparamType : outputParametersArray){
                String paramName = outparamType.getParameterName();
                if(paramName.equals("test-hadoop")){
                    ActualParameter outParam = new ActualParameter();
                    outParam.getType().changeType(StringParameterType.type);
                    ((StringParameterType) outParam.getType()).setValue(job.getTrackingURL());
                    jobExecutionContext.getOutMessageContext().addParameter("test-hadoop", outParam);
                }
            }
        } catch (Exception e) {
            String errMessage = "Error occurred during Map-Reduce job execution.";
            logger.error(errMessage, e);
            throw new GFacProviderException(errMessage, e);
        }
    }

    public void dispose(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        // TODO: How to handle cluster shutdown. Best way is to introduce inPath/outPath to handler.
    }

    @Override
    public void cancelJob(String experimentId, JobExecutionContext jobExecutionContext) throws GFacException {
        throw new NotImplementedException();
    }

    @Override
    public void cancelJob(String experimentId, String workflowId, JobExecutionContext jobExecutionContext) throws GFacException {
        throw new NotImplementedException();
    }

    @Override
    public void cancelJob(String experimentId, String workflowId, String nodeId, JobExecutionContext jobExecutionContext) throws GFacException {
        throw new NotImplementedException();
    }

    public void initProperties(Map<String, String> properties) throws GFacProviderException, GFacException {

    }
}
