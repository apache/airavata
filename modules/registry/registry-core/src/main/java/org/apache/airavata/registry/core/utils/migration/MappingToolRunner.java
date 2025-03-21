package org.apache.airavata.registry.core.utils.migration;

import jakarta.persistence.Persistence;
import jakarta.persistence.metamodel.Type;
import org.apache.airavata.common.utils.JDBCConfig;
import org.apache.airavata.common.utils.JPAUtils;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.internal.SchemaCreatorImpl;
import org.hibernate.dialect.MySQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class MappingToolRunner {

    private static Logger logger = LoggerFactory.getLogger(MappingToolRunner.class);

    public static void run(JDBCConfig jdbcConfig, String outputFile, String persistenceUnitName) {
        run(jdbcConfig, outputFile, persistenceUnitName, "create");
    }

    public static void run(JDBCConfig jdbcConfig, String outputFile, String persistenceUnitName, String schemaAction) {

        Properties properties = new Properties();
        properties.putAll(JPAUtils.createConnectionProperties(jdbcConfig));
        properties.put("hibernate.dialect", MySQLDialect.class.getName());
        properties.put("hibernate.hbm2ddl.auto", schemaAction);
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySettings(properties)
                .build();
        MetadataSources metadataSources = new MetadataSources(registry);
        Arrays.stream(getEntityClasses(persistenceUnitName)).forEach(metadataSources::addAnnotatedClass);

        Metadata metadata = metadataSources.buildMetadata();
        try (FileWriter fileWriter = new FileWriter(outputFile)) {
            String schemaScript = generateSchema(metadata, registry);
            fileWriter.write(schemaScript);
            logger.info("Generated schema script at: {}", outputFile);
        } catch (IOException e) {
            logger.error("Failed to write schema script to file", e);
            throw new RuntimeException("Failed to write schema script to file", e);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private static String generateSchema(Metadata metadata, StandardServiceRegistry registry) {
        StringBuilder schemaScript = new StringBuilder();
        SchemaCreatorImpl schemaCreator = new SchemaCreatorImpl(registry);
        schemaCreator.generateCreationCommands(metadata, true).forEach(query -> {
            schemaScript.append(query).append(";\n");
        });
        return schemaScript.toString();
    }

    private static Class<?>[] getEntityClasses(String persistenceUnitName) {
        try {
            return Persistence.createEntityManagerFactory(persistenceUnitName)
                    .getMetamodel()
                    .getEntities()
                    .stream()
                    .map(Type::getJavaType)
                    .toArray(Class<?>[]::new);
        } catch (Exception e) {
            logger.error("Failed to load entity classes from persistence unit: {}", persistenceUnitName, e);
            throw new RuntimeException("Failed to load entity classes from persistence unit", e);
        }
    }
}
