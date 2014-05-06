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

package org.apache.airavata.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.airavata.api.Airavata.Client;
import org.apache.airavata.api.error.AiravataClientConnectException;
import org.apache.airavata.api.error.AiravataClientException;
import org.apache.airavata.api.error.AiravataSystemException;
import org.apache.airavata.api.error.ExperimentNotFoundException;
import org.apache.airavata.api.error.InvalidRequestException;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.tools.DocumentCreator;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.util.ProjectModelUtil;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.thrift.TException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class DataRetrievalIT extends AbstractIntegrationTest {
    private final static Logger log = LoggerFactory.getLogger(DataRetrievalIT.class);
    
    //this will keep a list of experiment that was executed. each element will contain {experiemntId, user, project}
    private List<String[]> experimentDataList;
    
    private String[] users={"admin"};
    private String[] projects={"project1","project2","project3"};
    private List<String> projectIds = new ArrayList<String>();

    private static final int NUM_OF_EXPERIMENTS=10;
    
    public DataRetrievalIT() {
    }

    @BeforeTest
    public void setUp() throws Exception {
        init();
        experimentDataList=new ArrayList<String[]>();
		addApplications();
        addProjects();
		log.info("Setup Experiments");
		log.info("=================");
		for(int i=1; i<=NUM_OF_EXPERIMENTS;i++){
			//we are using the last user or project to test data empty scenarios 
			String user=users[(new Random()).nextInt(users.length)];
			String project=projectIds.get((new Random()).nextInt(projectIds.size()-1));
			String experimentId = runExperiment(user, project);
			experimentDataList.add(new String[]{experimentId,user,project});
			log.info("Running experiment "+i+" of "+NUM_OF_EXPERIMENTS+" - "+experimentId);
		}
    }

    private void addProjects() throws TException {
        for (int i = 0; i < projects.length; i++){
            Project project = ProjectModelUtil.createProject(projects[i], "admin", "test project");
            String projectId = getClient().createProject(project, "admin");
            projectIds.add(projectId);
        }
    }
    
    private List<String> getData(int searchIndex, String searchString, int returnIndexData){
    	List<String> results=new ArrayList<String>();
    	for (String[] record : experimentDataList) {
			if (record[searchIndex].equals(searchString)){
				if (!results.contains(record[returnIndexData])) {
					results.add(record[returnIndexData]);
				}
			}
		}
    	return results;
    }

	@Test
    public void listingExperimentsByUser() throws Exception {
		log.info("Testing user experiments");
		log.info("========================");
        for (String user : users) {
			List<Experiment> listUserExperiments = listUserExperiments(user);
			List<String> data = getData(1, user, 0);
        	log.info("\t"+user+" : "+data.size()+" experiments");
			Assert.assertEquals(listUserExperiments.size(), data.size());
			for (Experiment experiment : listUserExperiments) {
				Assert.assertThat(experiment.getExperimentID(), isIn(data)); 
			}
		}
    }
    
	@Test
    public void listingExperimentsByProject() throws Exception {
		log.info("Testing project experiments");
		log.info("===========================");
        for (String project : projectIds) {
			List<Experiment> listProjectExperiments = listProjectExperiments(project);
			List<String> data = getData(2, project, 0);
        	log.info("\t"+project+" : "+data.size()+" experiments");
			Assert.assertEquals(listProjectExperiments.size(), data.size());
			for (Experiment experiment : listProjectExperiments) {
				Assert.assertThat(experiment.getExperimentID(), isIn(data)); 
			}
		}
    }
	
	@Test
    public void listingUserProjects() throws Exception {
		log.info("Testing user projects");
		log.info("=====================");
        for (String user : users) {
			List<Project> listUserProjects = listUserProjects(user);
			List<String> data = getData(1, user, 2);
			data.add("default");
            System.out.println(data.size());
            log.info("\t"+user+" : "+data.size()+" projects");
			Assert.assertEquals(listUserProjects.size(), 4);
//			for (Project project : listUserProjects) {
//				Assert.assertThat(project.getProjectID(), isIn(data));
//			}
		}
    }
	
    private static Matcher<String> isIn(final List<String> expected){
        return new BaseMatcher<String>() {
            protected List<String> theExpected = expected;
            public boolean matches(Object o) {
                return theExpected.contains((String)o);
            }
			@Override
			public void describeTo(Description d) {
			}
        };
    }
    
    
	public List<Experiment> listUserExperiments(String user) throws ApplicationSettingsException,
			AiravataClientConnectException, InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		return getClient().getAllUserExperiments(user);
	}

	public List<Experiment> listProjectExperiments(String projectID) throws ApplicationSettingsException,
			AiravataClientConnectException, InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		return getClient().getAllExperimentsInProject(projectID);
	}
	
	public List<Project> listUserProjects(String user) throws ApplicationSettingsException,
			AiravataClientConnectException, InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		return getClient().getAllUserProjects(user);
	}

	public String runExperiment(String user, String project) throws AiravataAPIInvocationException,
			ApplicationSettingsException, AiravataClientConnectException,
			InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException, ExperimentNotFoundException {
		List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
		DataObjectType input = new DataObjectType();
		input.setKey("echo_input");
		// input.setType(DataType.STRING.toString());
		input.setValue("echo_output=Hello World");
		exInputs.add(input);

		List<DataObjectType> exOut = new ArrayList<DataObjectType>();
		DataObjectType output = new DataObjectType();
		output.setKey("echo_output");
		output.setType(DataType.STRING);
		output.setValue("");
		exOut.add(output);

		Experiment simpleExperiment = ExperimentModelUtil
				.createSimpleExperiment(project, user, "echoExperiment",
						"SimpleEcho0", "SimpleEcho0", exInputs);
		simpleExperiment.setExperimentOutputs(exOut);

		ComputationalResourceScheduling scheduling = ExperimentModelUtil
				.createComputationResourceScheduling("localhost", 1, 1, 1,
						"normal", 0, 0, 1, "sds128");
		scheduling.setResourceHostId("localhost");
		UserConfigurationData userConfigurationData = new UserConfigurationData();
		userConfigurationData.setAiravataAutoSchedule(false);
		userConfigurationData.setOverrideManualScheduledParams(false);
		userConfigurationData.setComputationalResourceScheduling(scheduling);
		simpleExperiment.setUserConfigurationData(userConfigurationData);

		Client client = getClient();
		final String expId = client.createExperiment(simpleExperiment);

		client.launchExperiment(expId, "testToken");
		return expId;
	}

	private void addApplications() throws AiravataAPIInvocationException {
		AiravataAPI airavataAPI = getAiravataAPI();
		DocumentCreator documentCreator = new DocumentCreator(airavataAPI);
		documentCreator.createLocalHostDocs();
	}
	

}
