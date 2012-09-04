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
package org.apache.airavata.provenance.impl.jpa;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.provenance.model.Experiment_Data;
import org.apache.airavata.provenance.model.Node_Data;
import org.apache.airavata.provenance.model.Workflow_Data;
import org.apache.airavata.registry.api.AiravataProvenanceRegistry;
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;

public class AiravataJPAProvenanceRegistry extends AiravataProvenanceRegistry{
	
	private static final String PERSISTENCE_UNIT_NAME = "airavata_provenance";
	private EntityManagerFactory factory;

	public AiravataJPAProvenanceRegistry(String user) {
		super(user);
		
		this.factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
	}

	public List<ActualParameter> loadOutput(String arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	public String saveOutput(String arg0, List<ActualParameter> arg1)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowExecution getWorkflowExecution(String arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowExecution> getWorkflowExecutionByUser(String arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowExecution> getWorkflowExecutionByUser(String arg0,
			int arg1, int arg2) throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getWorkflowExecutionIdByUser(String arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWorkflowExecutionMetadata(String arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWorkflowExecutionName(String arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowIOData> getWorkflowExecutionOutput(String arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowIOData getWorkflowExecutionOutput(String arg0, String arg1)
			throws RegistryException {

		return null;
	}

	@Override
	public String[] getWorkflowExecutionOutputNames(String arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowInstanceStatus getWorkflowExecutionStatus(String instanceID)
			throws RegistryException {
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
        Query q = em.createQuery("SELECT p FROM Workflow_Data p WHERE p.workflow_InstanceID = :workflow_InstanceID");
        q.setParameter("workflow_InstanceID", instanceID);
        Workflow_Data singleResult = (Workflow_Data) q.getSingleResult();
        WorkflowInstanceStatus workflowInstanceStatus = new
                WorkflowInstanceStatus(new WorkflowInstance(singleResult.getExperiment_Data().getExperiment_ID(),singleResult.getTemplate_name())
                ,ExecutionStatus.valueOf(singleResult.getStatus()),new Date(singleResult.getLast_update_time().getTime()));
        return workflowInstanceStatus;
	}

	@Override
	public String getWorkflowExecutionUser(String arg0)
			throws RegistryException {

		return null;
	}

	@Override
	public boolean saveWorkflowData(WorkflowRunTimeData arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		Query q = em.createQuery("SELECT p FROM Experiment_Data p WHERE p.experiment_ID = :exp_ID");
		q.setParameter("exp_ID", arg0.getExperimentID());
		Experiment_Data eData = (Experiment_Data) q.getSingleResult();
		
		Workflow_Data wData = new Workflow_Data();
		wData.setExperiment_Data(eData);
		wData.setExperiment_Data(eData);
		wData.setTemplate_name(arg0.getTemplateID());
		wData.setWorkflow_instanceID(arg0.getWorkflowInstanceID());
		wData.setStatus(arg0.getWorkflowStatus().toString());
		wData.setStart_time(arg0.getStartTime());
		
		em.persist(wData);
		
		em.getTransaction().commit();
		em.close();
		
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionMetadata(String arg0, String arg1)
			throws RegistryException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionName(String arg0, String arg1)
			throws RegistryException {
		// TODO Auto-generated method stub
		
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		
		Experiment_Data expData = new Experiment_Data();
		expData.setExperiment_ID(arg0);
		expData.setName(arg1);
		
		em.persist(expData);
		
		em.getTransaction().commit();
		em.close();
		
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionOutput(String arg0, WorkflowIOData arg1)
			throws RegistryException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionOutput(String arg0, String arg1,
			String arg2) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowExecutionServiceInput(WorkflowServiceIOData arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		
		Query q = em.createQuery("SELECT w FROM Workflow_Data w WHERE w.workflow_instanceID = :workflow_ID");
		q.setParameter("workflow_ID", arg0.getWorkflowInstanceId());
		Workflow_Data wData = (Workflow_Data) q.getSingleResult();
		
		Node_Data nData = new Node_Data();
		nData.setWorkflow_Data(wData);
		nData.setNode_id(arg0.getNodeId());
		nData.setInputs(arg0.getValue());
		nData.setNode_type((arg0.getNodeType().getNodeType().toString()));
		nData.setStatus(arg0.getNodeStatus().getExecutionStatus().toString());
		
		em.persist(nData);
	
		em.getTransaction().commit();
		em.close();
		
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionServiceOutput(WorkflowServiceIOData arg0)
			throws RegistryException {
		// TODO Auto-generated method stub

        EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		
		Query q = em.createQuery("SELECT w FROM Workflow_Data w WHERE w.workflow_instanceID = :workflow_ID");
		q.setParameter("workflow_ID", arg0.getWorkflowInstanceId());
		Workflow_Data wData = (Workflow_Data) q.getSingleResult();

		q = em.createQuery("SELECT p FROM Node_Data p WHERE p.workflow_Data = :workflow_data AND p.node_id = :node_ID");
		q.setParameter("workflow_data", wData);
		q.setParameter("node_ID", arg0.getNodeId());
		Node_Data nData = (Node_Data) q.getSingleResult();
		nData.setOutputs(arg0.getValue());

		em.getTransaction().commit();
		em.close();

		
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionStatus(String arg0, ExecutionStatus arg1)
			throws RegistryException {
		// TODO Auto-generated method stub
		
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		
		Query q = em.createQuery("SELECT w FROM Workflow_Data w WHERE w.workflow_instanceID = :workflow_ID");
		q.setParameter("workflow_ID", arg0);
		Workflow_Data wData = (Workflow_Data) q.getSingleResult();

		wData.setStatus(arg1.toString());
		em.persist(wData);
		em.getTransaction().commit();
		em.close();
		
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionUser(String arg0, String arg1)
			throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowLastUpdateTime(String arg0, Timestamp arg1)
			throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowNodeGramData(WorkflowNodeGramData arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		
		Query q = em.createQuery("SELECT w FROM Workflow_Data w WHERE w.workflow_instanceID = :workflow_ID");
		q.setParameter("workflow_ID", arg0);
		Workflow_Data wData = (Workflow_Data) q.getSingleResult();
		
		q = em.createQuery("SELECT p FROM Node_Data p WHERE p.workflow_Data = :workflow_data AND p.node_id = :node_ID");
		q.setParameter("workflow_data", wData);
		q.setParameter("node_ID", arg0.getNodeID());
		em.getTransaction().commit();
		em.close();
		
		return true;
	}

	@Override
	public boolean saveWorkflowNodeGramLocalJobID(String arg0, String arg1,
			String arg2) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowNodeLastUpdateTime(String arg0, String arg1,
			Timestamp arg2) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowNodeStatus(String arg0, String arg1,
			ExecutionStatus arg2) throws RegistryException {
		// TODO Auto-generated method stub
		
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		
		Query q = em.createQuery("SELECT w FROM Workflow_Data w WHERE w.workflow_instanceID = :workflow_ID");
		q.setParameter("workflow_ID", arg0);
		Workflow_Data wData = (Workflow_Data) q.getSingleResult();
		
		q = em.createQuery("SELECT p FROM Node_Data p WHERE p.workflow_Data = :workflow_data AND p.node_id = :node_ID");
		q.setParameter("workflow_data", wData);
		q.setParameter("node_ID", arg1);
		Node_Data nData = (Node_Data) q.getSingleResult();
		nData.setStatus(arg2.toString());
		
		em.getTransaction().commit();
		em.close();
		
		return true;
	}

	@Override
	public boolean saveWorkflowStatus(String arg0, WorkflowInstanceStatus arg1)
			throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<WorkflowServiceIOData> searchWorkflowExecutionServiceInput(
			String arg0, String arg1, String arg2) throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowServiceIOData> searchWorkflowExecutionServiceOutput(
			String arg0, String arg1, String arg2) throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

}
