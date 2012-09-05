package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.persistance.registry.jpa.Resource;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public abstract class AbstractResource implements Resource{
    private static final String PERSISTENCE_UNIT_NAME = "airavata_data";
	protected EntityManagerFactory factory;

    protected EntityManager em;

    protected AbstractResource() {
        em = factory.createEntityManager();
    }

    protected void begin(){
       em.getTransaction().begin();
    }

    protected void end(){
        em.getTransaction().commit();
		em.close();

    }


}
