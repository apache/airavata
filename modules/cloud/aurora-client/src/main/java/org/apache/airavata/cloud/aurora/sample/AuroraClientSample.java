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
package org.apache.airavata.cloud.aurora.sample;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.airavata.cloud.aurora.client.AuroraThriftClient;
import org.apache.airavata.cloud.aurora.client.bean.GetJobsResponseBean;
import org.apache.airavata.cloud.aurora.client.bean.IdentityBean;
import org.apache.airavata.cloud.aurora.client.bean.JobConfigBean;
import org.apache.airavata.cloud.aurora.client.bean.JobKeyBean;
import org.apache.airavata.cloud.aurora.client.bean.ProcessBean;
import org.apache.airavata.cloud.aurora.client.bean.ResourceBean;
import org.apache.airavata.cloud.aurora.client.bean.ResponseBean;
import org.apache.airavata.cloud.aurora.client.bean.TaskConfigBean;
import org.apache.airavata.cloud.aurora.util.AuroraThriftClientUtil;
import org.apache.thrift.TException;

/**
 * The Class AuroraClientSample.
 */
public class AuroraClientSample {
	
	/**
	 * Gets the job summary.
	 *
	 * @param client the client
	 * @return the job summary
	 * @throws Exception 
	 */
	public static void getRunningJobsList(String ownerRole) throws Exception {
		try {
			AuroraThriftClient client = AuroraThriftClient.getAuroraThriftClient();
			ResponseBean response = client.getJobList(ownerRole);
			System.out.println("Response status: " + response.getResponseCode().name());
			if(response instanceof GetJobsResponseBean) {
				GetJobsResponseBean result = (GetJobsResponseBean) response;
				System.out.println(result);
				
				Set<JobConfigBean> jobConfigs = result.getJobConfigs();
				for(JobConfigBean jobConfig : jobConfigs) {
					System.out.println(jobConfig);
					JobKeyBean jobKey = jobConfig.getJob();
					IdentityBean owner = jobConfig.getOwner();
					TaskConfigBean taskConfig = jobConfig.getTaskConfig();
					Set<ProcessBean> processes = taskConfig.getProcesses();
					
					System.out.println("\n**** JOB CONFIG ****");
						System.out.println("\t # cluster: " + jobConfig.getCluster());
						System.out.println("\t # instanceCount: " + jobConfig.getInstances());
						System.out.println("\t # isService: " + jobConfig.isService());
						System.out.println("\t\t # priority: " + jobConfig.getPriority());
						
						System.out.println("\t >> Job Key <<");
							System.out.println("\t\t # name: " + jobKey.getName());
							System.out.println("\t\t # role: " + jobKey.getRole());
							System.out.println("\t\t # environment: " + jobKey.getEnvironment());
							
						System.out.println("\t >> Identity <<");
							System.out.println("\t\t # owner: " + owner.getUser());
							
						System.out.println("\t >> Task Config <<");
							System.out.println("\t\t >> Resources <<");
								System.out.println("\t\t\t # numCPUs: " + taskConfig.getResources().getNumCpus());
								System.out.println("\t\t\t # diskMb: " + taskConfig.getResources().getDiskMb());
								System.out.println("\t\t\t # ramMb: " + taskConfig.getResources().getRamMb());
							
							System.out.println("\t\t >> Processes <<");
							for(ProcessBean process : processes) {
								System.out.println("\t\t\t ***** PROCESS *****");
								System.out.println("\t\t\t # name: " + process.getName());
								System.out.println("\t\t\t # cmdline: " + process.getCmdLine());
							}
				}
				
			}
		} catch (TException e) {
			e.printStackTrace();
		}
	}
	
	public static void createJob() throws Exception {
		JobKeyBean jobKey = new JobKeyBean("devel", "centos", "test_job");
		IdentityBean owner = new IdentityBean("centos");
		
		ProcessBean proc1 = new ProcessBean("process_1", "echo 'hello_world_1'", false);
		ProcessBean proc2 = new ProcessBean("process_2", "echo 'hello_world_2'", false);
		Set<ProcessBean> processes = new HashSet<>();
		processes.add(proc1);
		processes.add(proc2);
		
		ResourceBean resources = new ResourceBean(0.1, 8, 1);
		
		TaskConfigBean taskConfig = new TaskConfigBean("task_hello_world", processes, resources);
		JobConfigBean jobConfig = new JobConfigBean(jobKey, owner, taskConfig, "example");
		
		String executorConfigJson = AuroraThriftClientUtil.getExecutorConfigJson(jobConfig);
		System.out.println(executorConfigJson);
		
		AuroraThriftClient client = AuroraThriftClient.getAuroraThriftClient();
		ResponseBean response = client.createJob(jobConfig);
		System.out.println(response);
	}
	
	public static void createAutoDockJob() throws Exception {
		JobKeyBean jobKey = new JobKeyBean("devel", "centos", "test_autodock");
		IdentityBean owner = new IdentityBean("centos");
		
		String working_dir = "/home/centos/efs-mount-point/job_" + ThreadLocalRandom.current().nextInt(1, 101) + "/";
		String autodock_path = "/home/centos/efs-mount-point/autodock-vina";
		ProcessBean proc1 = new ProcessBean("process_1", "mkdir " + working_dir, false);
		ProcessBean proc2 = new ProcessBean("process_2", "cp " + autodock_path + "/vina_screenM.sh " + working_dir, false);
		ProcessBean proc3 = new ProcessBean("process_3", "cp " + autodock_path + "/ligand* " + working_dir, false);
		ProcessBean proc4 = new ProcessBean("process_4", "cd " + working_dir + " && sh vina_screenM.sh", false);
		
		Set<ProcessBean> processes = new LinkedHashSet<>();
		processes.add(proc1);		
		processes.add(proc2);
		processes.add(proc3);
		processes.add(proc4);
		
		ResourceBean resources = new ResourceBean(1.5, 125, 512);
		
		TaskConfigBean taskConfig = new TaskConfigBean("test_autodock", processes, resources);
		JobConfigBean jobConfig = new JobConfigBean(jobKey, owner, taskConfig, "example");
		
		String executorConfigJson = AuroraThriftClientUtil.getExecutorConfigJson(jobConfig);
		System.out.println(executorConfigJson);
		
		AuroraThriftClient client = AuroraThriftClient.getAuroraThriftClient();
		ResponseBean response = client.createJob(jobConfig);
		System.out.println(response);
	}
	
	public static void killTasks(String jobName) throws Exception {
		JobKeyBean jobKey = new JobKeyBean("devel", "centos", jobName);
		AuroraThriftClient client = AuroraThriftClient.getAuroraThriftClient();
		ResponseBean response = client.killTasks(jobKey, new HashSet<>());
		System.out.println(response);
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		 try {
			// create sample job
//			AuroraClientSample.createJob();
//			AuroraClientSample.createAutoDockJob();
			
			// kill pending job
//			AuroraClientSample.killTasks("test_autodock");
			
			// get jobs summary
			AuroraClientSample.getRunningJobsList("centos");
			
//			AuroraThriftClient client = AuroraThriftClient.getAuroraThriftClient(Constants.AURORA_SCHEDULER_PROP_FILE);
//			ResponseBean response = client.getPendingReasonForJob(new JobKeyBean("devel", "centos", "hello_pending"));
//			System.out.println(response);
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}

}
