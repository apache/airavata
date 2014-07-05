package org.apache.aiaravata.application.catalog.data.resources;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.ResourceJobManager;
import org.apache.aiaravata.application.catalog.data.model.SshJobSubmission;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogJPAUtils;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogQueryGenerator;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogResourceType;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshJobSubmissionResource extends AbstractResource {
	private final static Logger logger = LoggerFactory.getLogger(SshJobSubmissionResource.class);
	private String resourceJobManagerId;
	private ResourceJobManagerResource resourceJobManagerResource;
	private String jobSubmissionInterfaceId;
	private String alternativeSshHostname;
	private String securityProtocol;
	private int sshPort;
	
	@Override
	public void remove(Object identifier) throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(SSH_JOB_SUBMISSION);
			generator.setParameter(SshJobSubmissionConstants.JOB_SUBMISSION_INTERFACE_ID, identifier);
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
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(SSH_JOB_SUBMISSION);
			generator.setParameter(SshJobSubmissionConstants.JOB_SUBMISSION_INTERFACE_ID, identifier);
			Query q = generator.selectQuery(em);
			SshJobSubmission sshJobSubmission = (SshJobSubmission) q.getSingleResult();
			SshJobSubmissionResource sshJobSubmissionResource = (SshJobSubmissionResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.SSH_JOB_SUBMISSION, sshJobSubmission);
			em.getTransaction().commit();
			em.close();
			return sshJobSubmissionResource;
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
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(SSH_JOB_SUBMISSION);
			Query q;
			if ((fieldName.equals(SshJobSubmissionConstants.RESOURCE_JOB_MANAGER_ID)) || (fieldName.equals(SshJobSubmissionConstants.JOB_SUBMISSION_INTERFACE_ID)) || (fieldName.equals(SshJobSubmissionConstants.ALTERNATIVE_SSH_HOSTNAME)) || (fieldName.equals(SshJobSubmissionConstants.SECURITY_PROTOCOL)) || (fieldName.equals(SshJobSubmissionConstants.SSH_PORT))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					SshJobSubmission sshJobSubmission = (SshJobSubmission) result;
					SshJobSubmissionResource sshJobSubmissionResource = (SshJobSubmissionResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.SSH_JOB_SUBMISSION, sshJobSubmission);
					sshJobSubmissionResources.add(sshJobSubmissionResource);
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
	public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
		List<String> sshJobSubmissionResourceIDs = new ArrayList<String>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(SSH_JOB_SUBMISSION);
			Query q;
			if ((fieldName.equals(SshJobSubmissionConstants.RESOURCE_JOB_MANAGER_ID)) || (fieldName.equals(SshJobSubmissionConstants.JOB_SUBMISSION_INTERFACE_ID)) || (fieldName.equals(SshJobSubmissionConstants.ALTERNATIVE_SSH_HOSTNAME)) || (fieldName.equals(SshJobSubmissionConstants.SECURITY_PROTOCOL)) || (fieldName.equals(SshJobSubmissionConstants.SSH_PORT))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					SshJobSubmission sshJobSubmission = (SshJobSubmission) result;
					SshJobSubmissionResource sshJobSubmissionResource = (SshJobSubmissionResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.SSH_JOB_SUBMISSION, sshJobSubmission);
					sshJobSubmissionResourceIDs.add(sshJobSubmissionResource.getJobSubmissionInterfaceId());
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
			SshJobSubmission existingSshJobSubmission = em.find(SshJobSubmission.class, jobSubmissionInterfaceId);
			em.close();
			SshJobSubmission sshJobSubmission;
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			if (existingSshJobSubmission == null) {
				sshJobSubmission = new SshJobSubmission();
			} else {
				sshJobSubmission = existingSshJobSubmission;
			}
			sshJobSubmission.setResourceJobManagerId(getResourceJobManagerId());
			ResourceJobManager resourceJobManager = em.find(ResourceJobManager.class, getResourceJobManagerId());
			sshJobSubmission.setResourceJobManager(resourceJobManager);
			sshJobSubmission.setJobSubmissionInterfaceId(getJobSubmissionInterfaceId());
			sshJobSubmission.setAlternativeSshHostname(getAlternativeSshHostname());
			sshJobSubmission.setSecurityProtocol(getSecurityProtocol());
			sshJobSubmission.setSshPort(getSshPort());
			if (existingSshJobSubmission == null) {
				em.persist(sshJobSubmission);
			} else {
				em.merge(sshJobSubmission);
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
			SshJobSubmission sshJobSubmission = em.find(SshJobSubmission.class, identifier);
			em.close();
			return sshJobSubmission != null;
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
	
	public String getResourceJobManagerId() {
		return resourceJobManagerId;
	}
	
	public ResourceJobManagerResource getResourceJobManagerResource() {
		return resourceJobManagerResource;
	}
	
	public String getJobSubmissionInterfaceId() {
		return jobSubmissionInterfaceId;
	}
	
	public String getAlternativeSshHostname() {
		return alternativeSshHostname;
	}
	
	public String getSecurityProtocol() {
		return securityProtocol;
	}
	
	public int getSshPort() {
		return sshPort;
	}
	
	public void setResourceJobManagerId(String resourceJobManagerId) {
		this.resourceJobManagerId=resourceJobManagerId;
	}
	
	public void setResourceJobManagerResource(ResourceJobManagerResource resourceJobManagerResource) {
		this.resourceJobManagerResource=resourceJobManagerResource;
	}
	
	public void setJobSubmissionInterfaceId(String jobSubmissionInterfaceId) {
		this.jobSubmissionInterfaceId=jobSubmissionInterfaceId;
	}
	
	public void setAlternativeSshHostname(String alternativeSshHostname) {
		this.alternativeSshHostname=alternativeSshHostname;
	}
	
	public void setSecurityProtocol(String securityProtocol) {
		this.securityProtocol=securityProtocol;
	}
	
	public void setSshPort(int sshPort) {
		this.sshPort=sshPort;
	}
}