package org.apache.airavata.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class JpaMappingContextRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(JpaMappingContextRegistrar.class);

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        logger.info("Registering custom jpaMappingContext bean definition via JpaMappingContextRegistrar");
        System.out.println("DEBUG: JpaMappingContextRegistrar running");

        // Overwrite 'jpaMappingContext' with our bean
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(OpenJpaMetamodelMappingContextFactoryBean.class);
        builder.setPrimary(true);
        // Autowiring will handle the EMF injection since we added @Autowired to the setter
        builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        
        registry.registerBeanDefinition("jpaMappingContext", builder.getBeanDefinition());
    }
}