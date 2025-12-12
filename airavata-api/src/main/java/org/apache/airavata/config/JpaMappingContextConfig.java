package org.apache.airavata.config;

import jakarta.persistence.EntityManagerFactory;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

@Configuration
public class JpaMappingContextConfig {

    private static final Logger logger = LoggerFactory.getLogger(JpaMappingContextConfig.class);

    public JpaMappingContextConfig() {
        System.out.println("DEBUG: JpaMappingContextConfig CONSTRUCTOR called");
    }

    /**
     * Custom jpaMappingContext bean that handles OpenJPA enhancement errors gracefully.
     * This allows the application to start even if some entities have enhancement issues,
     * by catching errors when accessing metamodels and continuing with working EntityManagerFactories.
     */
    @Bean(name = "jpaMappingContext")
    @Primary
    @DependsOn({
        "profileServiceEntityManagerFactory",
        "appCatalogEntityManagerFactory",
        "expCatalogEntityManagerFactory",
        "replicaCatalogEntityManagerFactory",
        "workflowCatalogEntityManagerFactory",
        "sharingRegistryEntityManagerFactory",
        "credentialStoreEntityManagerFactory"
    })
    public OpenJpaMetamodelMappingContextFactoryBean jpaMappingContext(
            @Qualifier("profileServiceEntityManagerFactory") EntityManagerFactory profileEmf,
            @Qualifier("appCatalogEntityManagerFactory") EntityManagerFactory appCatalogEmf,
            @Qualifier("expCatalogEntityManagerFactory") EntityManagerFactory expCatalogEmf,
            @Qualifier("replicaCatalogEntityManagerFactory") EntityManagerFactory replicaCatalogEmf,
            @Qualifier("workflowCatalogEntityManagerFactory") EntityManagerFactory workflowCatalogEmf,
            @Qualifier("sharingRegistryEntityManagerFactory") EntityManagerFactory sharingRegistryEmf,
            @Qualifier("credentialStoreEntityManagerFactory") EntityManagerFactory credentialStoreEmf) {
        
        System.out.println("DEBUG: jpaMappingContext @Bean method called in JpaMappingContextConfig");
        logger.info("Creating custom OpenJpaMetamodelMappingContextFactoryBean from JpaMappingContextConfig");
        
        OpenJpaMetamodelMappingContextFactoryBean factory = new OpenJpaMetamodelMappingContextFactoryBean();
        factory.setEntityManagerFactories(Arrays.asList(
                profileEmf, appCatalogEmf, expCatalogEmf, replicaCatalogEmf,
                workflowCatalogEmf, sharingRegistryEmf, credentialStoreEmf));
        return factory;
    }
}
