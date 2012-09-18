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
package org.apache.airavata.provenance.test;

import static org.junit.Assert.assertTrue;

public class JpaTest {
//	private static final String PERSISTENCE_UNIT_NAME = "airavata_provenance";
//	private EntityManagerFactory factory;
//
//	@Before
//	public void setUp() throws Exception {
//		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
//		EntityManager em = factory.createEntityManager();
//
//		// Begin a new local transaction so that we can persist a new entity
//		em.getTransaction().begin();
//
//		// Read the existing entries
//		Query q = em.createQuery("select m from Experiment_Data m");
//		// Persons should be empty
//
//		// Do we have entries?
//		boolean createNewEntries = (q.getResultList().size() == 0);
//
//		// No, so lets create new entries
//		if (createNewEntries) {
//			assertTrue(q.getResultList().size() == 0);
//			Experiment_Data data = new Experiment_Data();
//			data.setExperiment_ID("Experiement_ID1");
//			data.setName("Name1");
//			em.persist(data);
//			for (int i = 0; i < 5; i++) {
//				Workflow_Data wData = new Workflow_Data();
//				wData.setWorkflow_instanceID("instance_ID" + i);
//				wData.setExperiment_Data(data);
//				em.persist(wData);
//				for (int j = 0; j < 5; j++) {
//					Node_Data nData = new Node_Data();
//					nData.setNode_id("node_ID" + j);
//					nData.setWorkflow_Data(wData);
//					em.persist(nData);
//
//					Gram_Data gData = new Gram_Data();
//					gData.setNode_id("node_ID" + j);
//					gData.setWorkflow_Data(wData);
//					em.persist(gData);
//				}
//				// Now persists the family person relationship
//				// data.getWorkflows().add(wData);
//				// em.persist(wData);
//				// em.persist(data);
//			}
//		}
//
//		// Commit the transaction, which will cause the entity to
//		// be stored in the database
//		em.getTransaction().commit();
//
//		// It is always good practice to close the EntityManager so that
//		// resources are conserved.
//		em.close();
//
//	}
//
//	@Test
//	public void checkInsertedWorkflow() {
//
//		// Now lets check the database and see if the created entries are there
//		// Create a fresh, new EntityManager
//		EntityManager em = factory.createEntityManager();
//
//		// Perform a simple query for all the Message entities
//		Query q = em.createQuery("select m from Workflow_Data m");
//
//		// We should have 5 Persons in the database
//		assertTrue(q.getResultList().size() == 5);
//
//		em.close();
//	}
//
//	@Test
//	public void checkInsertedNode() {
//		EntityManager em = factory.createEntityManager();
//
//		Query q = em.createQuery("select m from Node_Data m");
//
//		assertTrue(q.getResultList().size() == 25);
//
//		em.close();
//	}
//
//	@Test (expected = javax.persistence.NoResultException.class)
//	public void deleteNode_Data() throws InterruptedException {
//		Thread.sleep(1000);
//		EntityManager em = factory.createEntityManager();
//		// Begin a new local transaction so that we can persist a new entity
//		em.getTransaction().begin();
//		Query q = em.createQuery("SELECT p FROM Experiment_Data p WHERE p.experiment_ID = :firstName");
//		q.setParameter("firstName", "Experiement_ID1");
//		Experiment_Data eData = (Experiment_Data) q.getSingleResult();
//
//		q = em.createQuery("SELECT p FROM Workflow_Data p WHERE p.experiment_Data = :firstName AND p.workflow_instanceID = :lastName");
//		q.setParameter("firstName", eData);
//		q.setParameter("lastName", "instance_ID4");
//		Workflow_Data wData = (Workflow_Data) q.getSingleResult();
//
//		q = em.createQuery("SELECT p FROM Node_Data p WHERE p.workflow_Data = :firstName AND p.node_id = :lastName");
//		q.setParameter("firstName", wData);
//		q.setParameter("lastName", "node_ID4");
//		Node_Data nData = (Node_Data) q.getSingleResult();
//
//		//System.out.println(nData.getStart_time());
//		em.remove(nData);
//
//		q = em.createQuery("SELECT p FROM Node_Data p WHERE p.workflow_Data = :firstName AND p.node_id = :lastName");
//		q.setParameter("firstName", wData);
//		q.setParameter("lastName", "node_ID3");
//		nData = (Node_Data) q.getSingleResult();
//		nData.setStatus("finished");
//		Thread.sleep(5000);
//		em.getTransaction().commit();
//
//		q = em.createQuery("SELECT p FROM Node_Data p WHERE p.workflow_Data = :firstName AND p.node_id = :lastName");
//		q.setParameter("firstName", wData);
//		q.setParameter("lastName", "node_ID4");
//		Node_Data person = (Node_Data) q.getSingleResult();
//		person.getInputs();
//		// Begin a new local transaction so that we can persist a new entity
//
//		em.close();
//	}
}
