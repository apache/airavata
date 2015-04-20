/**
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

package org.apache.aiaravata.application.catalog.data.resources;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.EmailMonitorProperty;
import org.apache.aiaravata.application.catalog.data.model.ResourceJobManager;
import org.apache.aiaravata.application.catalog.data.model.SshJobSubmission;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogJPAUtils;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogQueryGenerator;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogResourceType;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EmailPropertyResource extends AbstractResource {
	private final static Logger logger = LoggerFactory.getLogger(EmailPropertyResource.class);
	private String jobSubmissionInterfaceId;
	private String host;
	private String emailAddress;
	private String password;
    private String folderName;
    private String protocol;
    private String senderEmailAddress;
    private SshJobSubmissionResource sshJobSubmissionResource;

	@Override
	public void remove(Object identifier) throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(EMAIL_PROPERTY);
			generator.setParameter(EmailMonitorPropertyConstants.JOB_SUBMISSION_INTERFACE_ID, identifier);
			Query q = generator.deleteQuery(em);
			q.executeUpdate();
			em.getTransaction().commit();
			em.close();
		} catch (ApplicationSettingsException e) {
			logger.error(e.getMessage(), e);
			throw new AppCatalogException(e);
		} finally {
			if (em != null && em.isOpen()) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				em.close();
			}
		}
	}
	
	@Override
	public Resource get(Object identifier) throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(EMAIL_PROPERTY);
			generator.setParameter(EmailMonitorPropertyConstants.JOB_SUBMISSION_INTERFACE_ID, identifier);
			Query q = generator.selectQuery(em);
			EmailMonitorProperty emailProperty = (EmailMonitorProperty) q.getSingleResult();
			EmailPropertyResource emailPropertyResource = (EmailPropertyResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.EMAIL_MONITOR_PROPERTY, emailProperty);
			em.getTransaction().commit();
			em.close();
			return emailPropertyResource;
		} catch (ApplicationSettingsException e) {
			logger.error(e.getMessage(), e);
			throw new AppCatalogException(e);
		} finally {
			if (em != null && em.isOpen()) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				em.close();
			}
		}
	}
	
	@Override
	public List<Resource> get(String fieldName, Object value) throws AppCatalogException {
		List<Resource> sshJobSubmissionResources = new ArrayList<Resource>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(EMAIL_PROPERTY);
			Query q;
			if (fieldName.equals(EmailMonitorPropertyConstants.JOB_SUBMISSION_INTERFACE_ID)){
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					EmailMonitorProperty emailMonitorProperty = (EmailMonitorProperty) result;
					EmailPropertyResource emailPropertyResource = (EmailPropertyResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.EMAIL_MONITOR_PROPERTY, emailMonitorProperty);
					sshJobSubmissionResources.add(emailPropertyResource);
				}
			} else {
				em.getTransaction().commit();
					em.close();
				logger.error("Unsupported field name for Ssh Job Submission Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Ssh Job Submission Resource.");
			}
			em.getTransaction().commit();
			em.close();
		} catch (ApplicationSettingsException e) {
			logger.error(e.getMessage(), e);
			throw new AppCatalogException(e);
		} finally {
			if (em != null && em.isOpen()) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				em.close();
			}
		}
		return sshJobSubmissionResources;
	}

    @Override
    public List<Resource> getAll() throws AppCatalogException {
        return null;
    }

    @Override
    public List<String> getAllIds() throws AppCatalogException {
        return null;
    }

    @Override
	public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
		List<String> sshJobSubmissionResourceIDs = new ArrayList<String>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(EMAIL_PROPERTY);
			Query q;
			if (fieldName.equals(EmailMonitorPropertyConstants.JOB_SUBMISSION_INTERFACE_ID)) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					EmailMonitorProperty monitorProperty = (EmailMonitorProperty) result;
					EmailPropertyResource emailPropertyResource = (EmailPropertyResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.EMAIL_MONITOR_PROPERTY, monitorProperty);
					sshJobSubmissionResourceIDs.add(emailPropertyResource.getJobSubmissionInterfaceId());
				}
			} else {
				em.getTransaction().commit();
					em.close();
				logger.error("Unsupported field name for Ssh Job Submission Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Ssh Job Submission Resource.");
			}
			em.getTransaction().commit();
			em.close();
		} catch (ApplicationSettingsException e) {
			logger.error(e.getMessage(), e);
			throw new AppCatalogException(e);
		} finally {
			if (em != null && em.isOpen()) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				em.close();
			}
		}
		return sshJobSubmissionResourceIDs;
	}
	
	@Override
	public void save() throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			EmailMonitorProperty monitorProperty = em.find(EmailMonitorProperty.class, jobSubmissionInterfaceId);
			em.close();
			EmailMonitorProperty emailMonitorProperty;
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			if (monitorProperty == null) {
				emailMonitorProperty = new EmailMonitorProperty();
			} else {
				emailMonitorProperty = monitorProperty;
			}
			emailMonitorProperty.setJobSubmissionId(jobSubmissionInterfaceId);
			emailMonitorProperty.setEmailAddress(emailAddress);
			emailMonitorProperty.setEmailProtocol(protocol);
			emailMonitorProperty.setFolderName(folderName);
            emailMonitorProperty.setPassword(password);
			emailMonitorProperty.setHost(host);
            emailMonitorProperty.setSenderEmailAddress(senderEmailAddress);
            if (monitorProperty == null) {
				em.persist(emailMonitorProperty);
			} else {
				em.merge(emailMonitorProperty);
			}
			em.getTransaction().commit();
			em.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new AppCatalogException(e);
		} finally {
			if (em != null && em.isOpen()) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				em.close();
			}
		}
	}
	
	@Override
	public boolean isExists(Object identifier) throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			EmailMonitorProperty emailMonitorProperty = em.find(EmailMonitorProperty.class, identifier);
			em.close();
			return emailMonitorProperty != null;
		} catch (ApplicationSettingsException e) {
			logger.error(e.getMessage(), e);
			throw new AppCatalogException(e);
		} finally {
			if (em != null && em.isOpen()) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				em.close();
			}
		}
	}

    public String getJobSubmissionInterfaceId() {
        return jobSubmissionInterfaceId;
    }

    public void setJobSubmissionInterfaceId(String jobSubmissionInterfaceId) {
        this.jobSubmissionInterfaceId = jobSubmissionInterfaceId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public SshJobSubmissionResource getSshJobSubmissionResource() {
        return sshJobSubmissionResource;
    }

    public void setSshJobSubmissionResource(SshJobSubmissionResource sshJobSubmissionResource) {
        this.sshJobSubmissionResource = sshJobSubmissionResource;
    }

    public String getSenderEmailAddress() {
        return senderEmailAddress;
    }

    public void setSenderEmailAddress(String senderEmailAddress) {
        this.senderEmailAddress = senderEmailAddress;
    }
}