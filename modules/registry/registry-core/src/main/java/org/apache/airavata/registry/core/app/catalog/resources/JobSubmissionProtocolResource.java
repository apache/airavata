///**
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//
//package org.apache.aiaravata.application.catalog.data.resources;
//
//import org.apache.airavata.registry.cpi.AppCatalogException;
//import org.apache.aiaravata.application.catalog.data.model.ComputeResource;
//import org.apache.aiaravata.application.catalog.data.model.JobSubmissionProtocol;
//import org.apache.aiaravata.application.catalog.data.model.JobSubmissionProtocolPK;
//import org.apache.aiaravata.application.catalog.data.util.AppCatalogJPAUtils;
//import org.apache.aiaravata.application.catalog.data.util.AppCatalogQueryGenerator;
//import org.apache.aiaravata.application.catalog.data.util.AppCatalogResourceType;
//import org.apache.airavata.common.exception.ApplicationSettingsException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.persistence.EntityManager;
//import javax.persistence.Query;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class JobSubmissionProtocolResource extends AbstractResource {
//
//    private final static Logger logger = LoggerFactory.getLogger(JobSubmissionProtocolResource.class);
//
//    private String resourceID;
//    private String submissionID;
//    private String jobType;
//    private ComputeResourceResource computeHostResource;
//
//    public void remove(Object identifier) throws AppCatalogException {
//        HashMap<String, String> ids;
//        if (identifier instanceof Map) {
//            ids = (HashMap) identifier;
//        } else {
//            logger.error("Identifier should be a map with the field name and it's value");
//            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
//        }
//
//        EntityManager em = null;
//        try {
//            em = AppCatalogJPAUtils.getEntityManager();
//            em.getTransaction().begin();
//            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(JOB_SUBMISSION_PROTOCOL);
//            generator.setParameter(JobSubmissionProtocolConstants.RESOURCE_ID, ids.get(JobSubmissionProtocolConstants.RESOURCE_ID));
//            generator.setParameter(JobSubmissionProtocolConstants.SUBMISSION_ID, ids.get(JobSubmissionProtocolConstants.SUBMISSION_ID));
//            generator.setParameter(JobSubmissionProtocolConstants.JOB_TYPE, ids.get(JobSubmissionProtocolConstants.JOB_TYPE));
//            Query q = generator.deleteQuery(em);
//            q.executeUpdate();
//            em.getTransaction().commit();
//            em.close();
//        } catch (ApplicationSettingsException e) {
//            logger.error(e.getMessage(), e);
//            throw new AppCatalogException(e);
//        } finally {
//            if (em != null && em.isOpen()) {
//                if (em.getTransaction().isActive()) {
//                    em.getTransaction().rollback();
//                }
//                em.close();
//            }
//        }
//    }
//
//    public Resource get(Object identifier) throws AppCatalogException {
//        HashMap<String, String> ids;
//        if (identifier instanceof Map) {
//            ids = (HashMap) identifier;
//        } else {
//            logger.error("Identifier should be a map with the field name and it's value");
//            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
//        }
//
//        EntityManager em = null;
//        try {
//            em = AppCatalogJPAUtils.getEntityManager();
//            em.getTransaction().begin();
//            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(JOB_SUBMISSION_PROTOCOL);
//            generator.setParameter(JobSubmissionProtocolConstants.RESOURCE_ID, ids.get(JobSubmissionProtocolConstants.RESOURCE_ID));
//            generator.setParameter(JobSubmissionProtocolConstants.SUBMISSION_ID, ids.get(JobSubmissionProtocolConstants.SUBMISSION_ID));
//            generator.setParameter(JobSubmissionProtocolConstants.JOB_TYPE, ids.get(JobSubmissionProtocolConstants.JOB_TYPE));
//            Query q = generator.selectQuery(em);
//            JobSubmissionProtocol jobSubmissionProtocol = (JobSubmissionProtocol) q.getSingleResult();
//            JobSubmissionProtocolResource jobSubmissionProtocolResource =
//                    (JobSubmissionProtocolResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.JOB_SUBMISSION_PROTOCOL, jobSubmissionProtocol);
//            em.getTransaction().commit();
//            em.close();
//            return jobSubmissionProtocolResource;
//        } catch (ApplicationSettingsException e) {
//            logger.error(e.getMessage(), e);
//            throw new AppCatalogException(e);
//        } finally {
//            if (em != null && em.isOpen()) {
//                if (em.getTransaction().isActive()) {
//                    em.getTransaction().rollback();
//                }
//                em.close();
//            }
//        }
//    }
//
//    public List<Resource> get(String fieldName, Object value) throws AppCatalogException {
//        List<Resource> jobSubmissionProtocolResourceList = new ArrayList<Resource>();
//        EntityManager em = null;
//        try {
//            em = AppCatalogJPAUtils.getEntityManager();
//            em.getTransaction().begin();
//            Query q;
//            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(JOB_SUBMISSION_PROTOCOL);
//            List results;
//            if (fieldName.equals(JobSubmissionProtocolConstants.SUBMISSION_ID)) {
//                generator.setParameter(JobSubmissionProtocolConstants.SUBMISSION_ID, value);
//                q = generator.selectQuery(em);
//                results = q.getResultList();
//                if (results.size() != 0) {
//                    for (Object result : results) {
//                        JobSubmissionProtocol jobSubmissionProtocol = (JobSubmissionProtocol) result;
//                        JobSubmissionProtocolResource jobSubmissionProtocolResource =
//                                (JobSubmissionProtocolResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.JOB_SUBMISSION_PROTOCOL, jobSubmissionProtocol);
//                        jobSubmissionProtocolResourceList.add(jobSubmissionProtocolResource);
//                    }
//                }
//            } else if (fieldName.equals(JobSubmissionProtocolConstants.JOB_TYPE)) {
//                generator.setParameter(JobSubmissionProtocolConstants.JOB_TYPE, value);
//                q = generator.selectQuery(em);
//                results = q.getResultList();
//                if (results.size() != 0) {
//                    for (Object result : results) {
//                        JobSubmissionProtocol jobSubmissionProtocol = (JobSubmissionProtocol) result;
//                        JobSubmissionProtocolResource jobSubmissionProtocolResource =
//                                (JobSubmissionProtocolResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.JOB_SUBMISSION_PROTOCOL, jobSubmissionProtocol);
//                        jobSubmissionProtocolResourceList.add(jobSubmissionProtocolResource);
//                    }
//                }
//            } else if (fieldName.equals(HostIPAddressConstants.RESOURCE_ID)) {
//                generator.setParameter(HostIPAddressConstants.RESOURCE_ID, value);
//                q = generator.selectQuery(em);
//                results = q.getResultList();
//                if (results.size() != 0) {
//                    for (Object result : results) {
//                        JobSubmissionProtocol jobSubmissionProtocol = (JobSubmissionProtocol) result;
//                        JobSubmissionProtocolResource jobSubmissionProtocolResource =
//                                (JobSubmissionProtocolResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.JOB_SUBMISSION_PROTOCOL, jobSubmissionProtocol);
//                        jobSubmissionProtocolResourceList.add(jobSubmissionProtocolResource);
//                    }
//                }
//            } else {
//                em.getTransaction().commit();
//                em.close();
//                logger.error("Unsupported field name for Job Submission Protocol Resource.", new IllegalArgumentException());
//                throw new IllegalArgumentException("Unsupported field name for Job Submission Protocol Resource.");
//            }
//            em.getTransaction().commit();
//            em.close();
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            throw new AppCatalogException(e);
//        } finally {
//            if (em != null && em.isOpen()) {
//                if (em.getTransaction().isActive()) {
//                    em.getTransaction().rollback();
//                }
//                em.close();
//            }
//        }
//        return jobSubmissionProtocolResourceList;
//    }
//
//    @Override
//    public List<Resource> getAll() throws AppCatalogException {
//        return null;
//    }
//
//    @Override
//    public List<String> getAllIds() throws AppCatalogException {
//        return null;
//    }
//
//    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
//        List<String> jobSubmissionProtocolIDs = new ArrayList<String>();
//        EntityManager em = null;
//        try {
//            em = AppCatalogJPAUtils.getEntityManager();
//            em.getTransaction().begin();
//            Query q;
//            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(JOB_SUBMISSION_PROTOCOL);
//            List results;
//            if (fieldName.equals(JobSubmissionProtocolConstants.SUBMISSION_ID)) {
//                generator.setParameter(JobSubmissionProtocolConstants.SUBMISSION_ID, value);
//                q = generator.selectQuery(em);
//                results = q.getResultList();
//                if (results.size() != 0) {
//                    for (Object result : results) {
//                        JobSubmissionProtocol jobSubmissionProtocol = (JobSubmissionProtocol) result;
//                        jobSubmissionProtocolIDs.add(jobSubmissionProtocol.getSubmissionID());
//                    }
//                }
//            } else if (fieldName.equals(JobSubmissionProtocolConstants.RESOURCE_ID)) {
//                generator.setParameter(JobSubmissionProtocolConstants.RESOURCE_ID, value);
//                q = generator.selectQuery(em);
//                results = q.getResultList();
//                if (results.size() != 0) {
//                    for (Object result : results) {
//                        JobSubmissionProtocol jobSubmissionProtocol = (JobSubmissionProtocol) result;
//                        jobSubmissionProtocolIDs.add(jobSubmissionProtocol.getSubmissionID());
//                    }
//                }
//            } else if (fieldName.equals(JobSubmissionProtocolConstants.JOB_TYPE)) {
//                generator.setParameter(JobSubmissionProtocolConstants.JOB_TYPE, value);
//                q = generator.selectQuery(em);
//                results = q.getResultList();
//                if (results.size() != 0) {
//                    for (Object result : results) {
//                        JobSubmissionProtocol jobSubmissionProtocol = (JobSubmissionProtocol) result;
//                        jobSubmissionProtocolIDs.add(jobSubmissionProtocol.getSubmissionID());
//                    }
//                }
//            } else {
//                em.getTransaction().commit();
//                em.close();
//                logger.error("Unsupported field name for Job Submission Protocol resource.", new IllegalArgumentException());
//                throw new IllegalArgumentException("Unsupported field name for Job Submission Protocol Resource.");
//            }
//            em.getTransaction().commit();
//            em.close();
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            throw new AppCatalogException(e);
//        } finally {
//            if (em != null && em.isOpen()) {
//                if (em.getTransaction().isActive()) {
//                    em.getTransaction().rollback();
//                }
//                em.close();
//            }
//        }
//        return jobSubmissionProtocolIDs;
//    }
//
//    public void save() throws AppCatalogException {
//        EntityManager em = null;
//        try {
//            em = AppCatalogJPAUtils.getEntityManager();
//            JobSubmissionProtocol existingJobSubProtocol = em.find(JobSubmissionProtocol.class, new JobSubmissionProtocolPK(resourceID, submissionID, jobType));
//            em.close();
//
//            em = AppCatalogJPAUtils.getEntityManager();
//            em.getTransaction().begin();
//            ComputeResource computeResource = em.find(ComputeResource.class, resourceID);
//            if (existingJobSubProtocol != null) {
//                existingJobSubProtocol.setJobType(jobType);
//                existingJobSubProtocol.setSubmissionID(submissionID);
//                existingJobSubProtocol.setComputeResource(computeResource);
//                existingJobSubProtocol.setResourceID(resourceID);
//                em.merge(existingJobSubProtocol);
//            } else {
//                JobSubmissionProtocol jobSubmissionProtocol = new JobSubmissionProtocol();
//                jobSubmissionProtocol.setJobType(jobType);
//                jobSubmissionProtocol.setSubmissionID(submissionID);
//                jobSubmissionProtocol.setResourceID(resourceID);
//                jobSubmissionProtocol.setComputeResource(computeResource);
//                em.persist(jobSubmissionProtocol);
//            }
//            em.getTransaction().commit();
//            em.close();
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            throw new AppCatalogException(e);
//        } finally {
//            if (em != null && em.isOpen()) {
//                if (em.getTransaction().isActive()) {
//                    em.getTransaction().rollback();
//                }
//                em.close();
//            }
//        }
//
//    }
//
//    public boolean isExists(Object identifier) throws AppCatalogException {
//        HashMap<String, String> ids;
//        if (identifier instanceof Map) {
//            ids = (HashMap) identifier;
//        } else {
//            logger.error("Identifier should be a map with the field name and it's value");
//            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
//        }
//
//        EntityManager em = null;
//        try {
//            em = AppCatalogJPAUtils.getEntityManager();
//            JobSubmissionProtocol jobSubmissionProtocol = em.find(JobSubmissionProtocol.class, new JobSubmissionProtocolPK(ids.get(JobSubmissionProtocolConstants.RESOURCE_ID),
//                    ids.get(JobSubmissionProtocolConstants.SUBMISSION_ID), ids.get(JobSubmissionProtocolConstants.JOB_TYPE)));
//
//            em.close();
//            return jobSubmissionProtocol != null;
//        } catch (ApplicationSettingsException e) {
//            logger.error(e.getMessage(), e);
//            throw new AppCatalogException(e);
//        } finally {
//            if (em != null && em.isOpen()) {
//                if (em.getTransaction().isActive()) {
//                    em.getTransaction().rollback();
//                }
//                em.close();
//            }
//        }
//    }
//
//    public String getResourceID() {
//        return resourceID;
//    }
//
//    public void setResourceID(String resourceID) {
//        this.resourceID = resourceID;
//    }
//
//    public String getSubmissionID() {
//        return submissionID;
//    }
//
//    public void setSubmissionID(String submissionID) {
//        this.submissionID = submissionID;
//    }
//
//    public String getJobType() {
//        return jobType;
//    }
//
//    public void setJobType(String jobType) {
//        this.jobType = jobType;
//    }
//
//    public ComputeResourceResource getComputeHostResource() {
//        return computeHostResource;
//    }
//
//    public void setComputeHostResource(ComputeResourceResource computeHostResource) {
//        this.computeHostResource = computeHostResource;
//    }
//}
