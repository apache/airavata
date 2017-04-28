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
package org.apache.airavata.registry.core.app.catalog.resources;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.registry.core.app.catalog.model.ComputeResource;
import org.apache.airavata.registry.core.app.catalog.model.HostIPAddress;
import org.apache.airavata.registry.core.app.catalog.model.HostIPAddressPK;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HostIPAddressResource extends AppCatAbstractResource {

    private final static Logger logger = LoggerFactory.getLogger(HostIPAddressResource.class);

    private String resourceID;
    private String ipaddress;
    private ComputeResourceResource computeHostResource;

    public void remove(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator= new AppCatalogQueryGenerator(HOST_IPADDRESS);
            generator.setParameter(HostIPAddressConstants.RESOURCE_ID, identifier);
            Query q = generator.deleteQuery(em);
            q.executeUpdate();
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }

    }

    public AppCatalogResource get(Object identifier) throws AppCatalogException {
        HashMap<String, String> ids;
        if (identifier instanceof Map){
            ids = (HashMap)identifier;
        }else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(HOST_IPADDRESS);
            generator.setParameter(HostIPAddressConstants.RESOURCE_ID, ids.get(HostIPAddressConstants.RESOURCE_ID));
            generator.setParameter(HostIPAddressConstants.IP_ADDRESS, ids.get(HostIPAddressConstants.IP_ADDRESS));
            Query q = generator.selectQuery(em);
            HostIPAddress hostIPAddress = (HostIPAddress) q.getSingleResult();
            HostIPAddressResource hostIPAddressResource =
                    (HostIPAddressResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.HOST_IPADDRESS, hostIPAddress);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return hostIPAddressResource;
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

    public List<AppCatalogResource> get(String fieldName, Object value) throws AppCatalogException {

        List<AppCatalogResource> hostIPAddressResources = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(HOST_IPADDRESS);
            List results;
            if (fieldName.equals(HostIPAddressConstants.IP_ADDRESS)) {
                generator.setParameter(HostIPAddressConstants.IP_ADDRESS, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        HostIPAddress hostIPAddress = (HostIPAddress) result;
                        HostIPAddressResource hostIPAddressResource =
                                (HostIPAddressResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.HOST_IPADDRESS, hostIPAddress);
                        hostIPAddressResources.add(hostIPAddressResource);
                    }
                }
            } else if (fieldName.equals(HostIPAddressConstants.RESOURCE_ID)) {
                generator.setParameter(HostIPAddressConstants.RESOURCE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        HostIPAddress hostIPAddress = (HostIPAddress) result;
                        HostIPAddressResource hostIPAddressResource =
                                (HostIPAddressResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.HOST_IPADDRESS, hostIPAddress);
                        hostIPAddressResources.add(hostIPAddressResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for Host IPAddress Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Host IPAddress Resource.");
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
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
        return hostIPAddressResources;
    }

    @Override
    public List<AppCatalogResource> getAll() throws AppCatalogException {
        return null;
    }

    @Override
    public List<String> getAllIds() throws AppCatalogException {
        return null;
    }

    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {

        List<String> hostIPAddressResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(HOST_IPADDRESS);
            List results;
            if (fieldName.equals(HostIPAddressConstants.IP_ADDRESS)) {
                generator.setParameter(HostIPAddressConstants.IP_ADDRESS, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        HostIPAddress hostIPAddress = (HostIPAddress) result;
                        hostIPAddressResourceIDs.add(hostIPAddress.getResourceID());
                    }
                }
            } else if (fieldName.equals(HostIPAddressConstants.RESOURCE_ID)) {
                generator.setParameter(HostIPAddressConstants.RESOURCE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        HostIPAddress hostIPAddress = (HostIPAddress) result;
                        hostIPAddressResourceIDs.add(hostIPAddress.getResourceID());
                    }
                }
            } else {
                em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for Host IP Address resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Host IPAddress Resource.");
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
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
        return hostIPAddressResourceIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            HostIPAddress existingHostIP = em.find(HostIPAddress.class, new HostIPAddressPK(resourceID,ipaddress));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            ComputeResource computeResource = em.find(ComputeResource.class, resourceID);
            if (existingHostIP !=  null){
                existingHostIP.setIpaddress(ipaddress);
                existingHostIP.setResourceID(resourceID);
                existingHostIP.setComputeResource(computeResource);
                em.merge(existingHostIP);
            }else {
                HostIPAddress hostIPAddress = new HostIPAddress();
                hostIPAddress.setIpaddress(ipaddress);
                hostIPAddress.setResourceID(resourceID);
                hostIPAddress.setComputeResource(computeResource);
                em.persist(hostIPAddress);
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }

    }

    public boolean isExists(Object identifier) throws AppCatalogException {
        HashMap<String, String> ids;
        if (identifier instanceof Map){
            ids = (HashMap)identifier;
        }else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            HostIPAddress hostIPAddress = em.find(HostIPAddress.class, new HostIPAddressPK(ids.get(HostIPAddressConstants.RESOURCE_ID),
                    ids.get(HostIPAddressConstants.IP_ADDRESS)));

            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return hostIPAddress != null;
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

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public ComputeResourceResource getComputeHostResource() {
        return computeHostResource;
    }

    public void setComputeHostResource(ComputeResourceResource computeHostResource) {
        this.computeHostResource = computeHostResource;
    }
}
