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

package org.apache.airavata.gfac.impl;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.gfac.core.GFacEngine;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.task.Task;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.airavata.registry.cpi.RegistryException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GFacEngineImpl implements GFacEngine {

	public GFacEngineImpl() throws GFacException {

	}

	@Override
	public ProcessContext populateProcessContext(String processId, String gatewayId, String
			tokenId) throws GFacException {
		try {
			ProcessContext processContext = new ProcessContext(processId, gatewayId, tokenId);
			AppCatalog appCatalog = Factory.getDefaultAppCatalog();
			ExperimentCatalog expCatalog = Factory.getDefaultExpCatalog();
			processContext.setProcessModel((ProcessModel) expCatalog.get(ExperimentCatalogModelType.PROCESS, processId));
			GatewayResourceProfile gatewayProfile = appCatalog.getGatewayProfile().getGatewayProfile(gatewayId);
			processContext.setGatewayResourceProfile(gatewayProfile);
			processContext.setComputeResourcePreference(appCatalog.getGatewayProfile().getComputeResourcePreference
					(gatewayId, processContext.getProcessModel().getComputeResourceId()));
			processContext.setRemoteCluster(Factory.getRemoteCluster(processContext.getComputeResourcePreference()));
			//
			return processContext;
		} catch (AppCatalogException e) {
			throw new GFacException("App catalog access exception ", e);
		} catch (RegistryException e) {
			throw new GFacException("Registry access exception", e);
		} catch (AiravataException e) {
			throw new GFacException("Remote cluster initialization error", e);
		}
	}

	@Override
	public void createTaskChain(ProcessContext processContext) throws GFacException {
		List<InputDataObjectType> processInputs = processContext.getProcessModel().getProcessInputs();
		sortByInputOrder(processInputs);
		List<Task> taskChain = new ArrayList<>();
		if (processInputs != null) {
			for (InputDataObjectType processInput : processInputs) {
				DataType type = processInput.getType();
				switch (type) {
					case STDERR:
						//
						break;
					case STDOUT:
						//
						break;
					case URI:
						// TODO : provide data staging data model
						taskChain.add(Factory.getDataMovementTask(processContext.getDataMovementProtocol()));
						break;
					default:
						// nothing to do
						break;
				}
			}
		}
		taskChain.add(Factory.getJobSubmissionTask(processContext.getJobSubmissionProtocol()));
		List<OutputDataObjectType> processOutputs = processContext.getProcessModel().getProcessOutputs();
		for (OutputDataObjectType processOutput : processOutputs) {
			DataType type = processOutput.getType();
			switch (type) {
				case STDERR:
					break;
				case STDOUT:
					break;
				case URI:
					// TODO : Provide data staging data model
					taskChain.add(Factory.getDataMovementTask(processContext.getDataMovementProtocol()));
					break;
			}
		}

		processContext.setTaskChain(taskChain);
	}


	@Override
	public void executeProcess(ProcessContext processContext) throws GFacException {

	}

	@Override
	public void recoverProcess(ProcessContext processContext) throws GFacException {

	}

	@Override
	public void runProcessOutflow(ProcessContext processContext) throws GFacException {

	}

	@Override
	public void recoverProcessOutflow(ProcessContext processContext) throws GFacException {

	}

	@Override
	public void cancelProcess() throws GFacException {

	}

	/**
	 * Sort input data type by input order.
	 * @param processInputs
	 */
	private void sortByInputOrder(List<InputDataObjectType> processInputs) {
		Collections.sort(processInputs, new Comparator<InputDataObjectType>() {
			@Override
			public int compare(InputDataObjectType inputDT_1, InputDataObjectType inputDT_2) {
				return inputDT_1.getInputOrder() - inputDT_2.getInputOrder();
			}
		});
	}





}
