package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.registry.core.repositories.AbstractRepository;
import org.apache.airavata.registry.core.utils.JPAUtil.ExpCatalogJPAUtils;

import javax.persistence.EntityManager;

public class ExpCatAbstractRepository<T, E, Id> extends AbstractRepository<T, E, Id> {

    public ExpCatAbstractRepository(Class<T> thriftGenericClass, Class<E> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
    }

    @Override
    protected EntityManager getEntityManager() {
        try {
            return ExpCatalogJPAUtils.getEntityManager();
        } catch (ApplicationSettingsException e) {
            throw new RuntimeException("Failed to get Experiment Catalog EntityManager", e);
        }
    }
}
