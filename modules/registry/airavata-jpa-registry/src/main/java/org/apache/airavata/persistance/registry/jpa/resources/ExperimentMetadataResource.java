package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.Experiment_Metadata;

import javax.persistence.EntityManager;
import java.util.List;

public class ExperimentMetadataResource extends AbstractResource{
    private String expID;
    private String metadata;

    public String getExpID() {
        return expID;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setExpID(String expID) {
        this.expID = expID;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public Resource create(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource get(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Resource> get(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        Experiment_Metadata existingExpMetaData = em.find(Experiment_Metadata.class, expID);
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Experiment_Metadata experimentMetadata = new Experiment_Metadata();
        experimentMetadata.setExperiment_ID(expID);
        experimentMetadata.setMetadata(metadata);

        if(existingExpMetaData != null){
            existingExpMetaData.setMetadata(metadata);
            existingExpMetaData.setExperiment_ID(expID);
            experimentMetadata = em.merge(existingExpMetaData);
        }else{
            em.persist(experimentMetadata);
        }
        em.getTransaction().commit();
        em.close();

    }
}
