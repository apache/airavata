package org.apache.aiaravata.application.catalog.data.resources;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.GridftpDataMovement;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogJPAUtils;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogQueryGenerator;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogResourceType;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridftpDataMovementResource extends AbstractResource {
	private final static Logger logger = LoggerFactory.getLogger(GridftpDataMovementResource.class);
	private String dataMovementInterfaceId;
	private String securityProtocol;
	
	@Override
	public void remove(Object identifier) throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GRIDFTP_DATA_MOVEMENT);
			generator.setParameter(GridftpDataMovementConstants.DATA_MOVEMENT_INTERFACE_ID, identifier);
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
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GRIDFTP_DATA_MOVEMENT);
			generator.setParameter(GridftpDataMovementConstants.DATA_MOVEMENT_INTERFACE_ID, identifier);
			Query q = generator.selectQuery(em);
			GridftpDataMovement gridftpDataMovement = (GridftpDataMovement) q.getSingleResult();
			GridftpDataMovementResource gridftpDataMovementResource = (GridftpDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GRIDFTP_DATA_MOVEMENT, gridftpDataMovement);
			em.getTransaction().commit();
			em.close();
			return gridftpDataMovementResource;
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
		List<Resource> gridftpDataMovementResources = new ArrayList<Resource>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GRIDFTP_DATA_MOVEMENT);
			Query q;
			if ((fieldName.equals(GridftpDataMovementConstants.DATA_MOVEMENT_INTERFACE_ID)) || (fieldName.equals(GridftpDataMovementConstants.SECURITY_PROTOCOL))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					GridftpDataMovement gridftpDataMovement = (GridftpDataMovement) result;
					GridftpDataMovementResource gridftpDataMovementResource = (GridftpDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GRIDFTP_DATA_MOVEMENT, gridftpDataMovement);
					gridftpDataMovementResources.add(gridftpDataMovementResource);
				}
			} else {
				em.getTransaction().commit();
					em.close();
				logger.error("Unsupported field name for Gridftp Data Movement Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Gridftp Data Movement Resource.");
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
		return gridftpDataMovementResources;
	}
	
	@Override
	public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
		List<String> gridftpDataMovementResourceIDs = new ArrayList<String>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GRIDFTP_DATA_MOVEMENT);
			Query q;
			if ((fieldName.equals(GridftpDataMovementConstants.DATA_MOVEMENT_INTERFACE_ID)) || (fieldName.equals(GridftpDataMovementConstants.SECURITY_PROTOCOL))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					GridftpDataMovement gridftpDataMovement = (GridftpDataMovement) result;
					GridftpDataMovementResource gridftpDataMovementResource = (GridftpDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GRIDFTP_DATA_MOVEMENT, gridftpDataMovement);
					gridftpDataMovementResourceIDs.add(gridftpDataMovementResource.getDataMovementInterfaceId());
				}
			} else {
				em.getTransaction().commit();
					em.close();
				logger.error("Unsupported field name for Gridftp Data Movement Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Gridftp Data Movement Resource.");
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
		return gridftpDataMovementResourceIDs;
	}
	
	@Override
	public void save() throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			GridftpDataMovement existingGridftpDataMovement = em.find(GridftpDataMovement.class, dataMovementInterfaceId);
			em.close();
			GridftpDataMovement gridftpDataMovement;
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			if (existingGridftpDataMovement == null) {
				gridftpDataMovement = new GridftpDataMovement();
			} else {
				gridftpDataMovement = existingGridftpDataMovement;
			}
			gridftpDataMovement.setDataMovementInterfaceId(getDataMovementInterfaceId());
			gridftpDataMovement.setSecurityProtocol(getSecurityProtocol());
			if (existingGridftpDataMovement == null) {
				em.persist(gridftpDataMovement);
			} else {
				em.merge(gridftpDataMovement);
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
			GridftpDataMovement gridftpDataMovement = em.find(GridftpDataMovement.class, identifier);
			em.close();
			return gridftpDataMovement != null;
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
	
	public String getDataMovementInterfaceId() {
		return dataMovementInterfaceId;
	}
	
	public String getSecurityProtocol() {
		return securityProtocol;
	}
	
	public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
		this.dataMovementInterfaceId=dataMovementInterfaceId;
	}
	
	public void setSecurityProtocol(String securityProtocol) {
		this.securityProtocol=securityProtocol;
	}
}