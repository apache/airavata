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
package org.apache.airavata.integration;

import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.api.Airavata.Client;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.integration.tools.DocumentCreatorNew;
import org.apache.airavata.model.error.*;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.util.ProjectModelUtil;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.experiment.*;
import org.apache.thrift.TException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataRetrievalIT extends AbstractIntegrationTest {
    private final static Logger log = LoggerFactory.getLogger(DataRetrievalIT.class);
    
    //this will keep a list of experiment that was executed. each element will contain {experiemntId, user, project}
    private List<String[]> experimentDataList;
    
    private String[] users={"admin"};
    private String[] projects={"project1","project2","project3"};
    private List<String> projectIds = new ArrayList<String>();
    private AuthzToken authzToken;

    private static final int NUM_OF_EXPERIMENTS=10;
    
    public DataRetrievalIT() {
    }

    @BeforeTest
    public void setUp() throws Exception {
        init();
        authzToken = new AuthzToken("empty token");
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
            String projectId = getClient().createProject(authzToken, "default", project);
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
			List<ExperimentModel> listUserExperiments = listUserExperiments(user);
			List<String> data = getData(1, user, 0);
        	log.info("\t"+user+" : "+data.size()+" experiments");
			Assert.assertEquals(listUserExperiments.size(), data.size());
			for (ExperimentModel experiment : listUserExperiments) {
				Assert.assertThat(experiment.getExperimentId(), isIn(data));
			}
		}
    }
    
	@Test
    public void listingExperimentsByProject() throws Exception {
		log.info("Testing project experiments");
		log.info("===========================");
        for (String project : projectIds) {
			List<ExperimentModel> listProjectExperiments = listProjectExperiments(project);
			List<String> data = getData(2, project, 0);
        	log.info("\t"+project+" : "+data.size()+" experiments");
			Assert.assertEquals(listProjectExperiments.size(), data.size());
			for (ExperimentModel experiment : listProjectExperiments) {
				Assert.assertThat(experiment.getExperimentId(), isIn(data));
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
			public void describeTo(Description d) {
			}
        };
    }
    
    
	public List<ExperimentModel> listUserExperiments(String user) throws ApplicationSettingsException,
			AiravataClientException, InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		return getClient().getUserExperiments(authzToken, "default", user, 10, 0);
	}

	public List<ExperimentModel> listProjectExperiments(String projectID) throws ApplicationSettingsException,
			AiravataClientException, InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		return getClient().getExperimentsInProject(authzToken, projectID, 10, 0);
	}
	
	public List<Project> listUserProjects(String user) throws ApplicationSettingsException,
			AiravataClientException, InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		return getClient().getUserProjects(authzToken, "default", user, 10, 0);
	}

	public String runExperiment(String user, String project) throws ApplicationSettingsException, AiravataClientException,
			InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException, ExperimentNotFoundException {
		List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
        InputDataObjectType input = new InputDataObjectType();
        input.setName("echo_input");
		input.setType(DataType.STRING);
		input.setValue("echo_output=Hello World");
		exInputs.add(input);

		List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
        OutputDataObjectType output = new OutputDataObjectType();
		output.setName("echo_output");
		output.setType(DataType.STRING);
		output.setValue("");
		exOut.add(output);

		ExperimentModel simpleExperiment = ExperimentModelUtil
				.createSimpleExperiment("default", project, user, "echoExperiment",
						"SimpleEcho0", "SimpleEcho0", exInputs);
		simpleExperiment.setExperimentOutputs(exOut);

		ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil
				.createComputationResourceScheduling("localhost", 1, 1, 1,
						"normal", 0, 0);
		scheduling.setResourceHostId("localhost");
		UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
		userConfigurationData.setAiravataAutoSchedule(false);
		userConfigurationData.setOverrideManualScheduledParams(false);
		userConfigurationData.setComputationalResourceScheduling(scheduling);
		simpleExperiment.setUserConfigurationData(userConfigurationData);

		Client client = getClient();
		final String expId = client.createExperiment(authzToken, "default", simpleExperiment);

		client.launchExperiment(authzToken, expId, "testToken");
		return expId;
	}

	private void addApplications() throws AppCatalogException, TException {
		DocumentCreatorNew documentCreator = new DocumentCreatorNew(airavataClient);
		documentCreator.createLocalHostDocs();
	}
	

}
