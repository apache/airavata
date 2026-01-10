/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.config;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * Centralized Quartz configuration using Spring Boot's Quartz integration.
 * 
 * <p>This configuration provides:
 * <ul>
 *   <li>Spring-managed Quartz scheduler with dependency injection support</li>
 *   <li>Configurable via application properties (spring.quartz.*)</li>
 *   <li>Optional persistent job store using database</li>
 *   <li>Proper integration with Spring lifecycle and transaction management</li>
 * </ul>
 * 
 * <p>Configuration properties:
 * <pre>
 * spring.quartz.job-store-type=memory      # or 'jdbc' for persistent store
 * spring.quartz.properties.org.quartz.threadPool.threadCount=10
 * spring.quartz.properties.org.quartz.jobStore.isClustered=true
 * </pre>
 */
@Configuration
@ConditionalOnProperty(name = "spring.quartz.enabled", havingValue = "true", matchIfMissing = true)
public class QuartzConfig {

    /**
     * Create SpringBeanJobFactory that enables Spring DI in Quartz jobs.
     * This allows @Autowired and other Spring annotations to work in Job classes.
     */
    @Bean
    public SpringBeanJobFactory springBeanJobFactory(ApplicationContext applicationContext) {
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    /**
     * Configure the main Scheduler using Spring Boot's SchedulerFactoryBean.
     * This integrates with Spring Boot's autoconfiguration and allows
     * properties-based configuration.
     * 
     * @param jobFactory Spring-aware job factory for DI support
     * @param quartzProperties Quartz-specific properties from application config
     * @return Configured SchedulerFactoryBean
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.quartz")
    public SchedulerFactoryBean schedulerFactoryBean(
            SpringBeanJobFactory jobFactory,
            @Qualifier("quartzProperties") Properties quartzProperties) {
        
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(jobFactory);
        factory.setQuartzProperties(quartzProperties);
        
        // Override start delay for immediate startup
        factory.setStartupDelay(0);
        
        // Auto-start scheduler
        factory.setAutoStartup(true);
        
        // Wait for jobs to complete on shutdown
        factory.setWaitForJobsToCompleteOnShutdown(true);
        
        // Override existing jobs on restart
        factory.setOverwriteExistingJobs(true);
        
        return factory;
    }

    /**
     * Quartz-specific properties configuration.
     * These can be overridden in airavata.properties or application.yml.
     * 
     * @return Default Quartz properties
     */
    @Bean
    public Properties quartzProperties() {
        Properties props = new Properties();
        
        // Scheduler properties
        props.setProperty("org.quartz.scheduler.instanceName", "AiravataQuartzScheduler");
        props.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        
        // Thread pool configuration
        props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        props.setProperty("org.quartz.threadPool.threadCount", "10");
        props.setProperty("org.quartz.threadPool.threadPriority", "5");
        
        // Job store - default to in-memory (can be overridden for JDBC)
        props.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        
        // Misfire threshold
        props.setProperty("org.quartz.jobStore.misfireThreshold", "60000");
        
        return props;
    }

    /**
     * Optional: DataSource for Quartz JDBC job store.
     * Only created if spring.quartz.job-store-type=jdbc is configured.
     * 
     * By default, uses the primary datasource, but can be configured
     * to use a separate database for Quartz tables.
     * 
     * @param dataSource Primary datasource
     * @return Quartz-specific datasource
     */
    @Bean
    @QuartzDataSource
    @ConditionalOnProperty(name = "spring.quartz.job-store-type", havingValue = "jdbc")
    public DataSource quartzDataSource(@Qualifier("profileServiceDataSource") DataSource dataSource) {
        // For now, reuse the profile service datasource
        // In production, you might want a separate database for Quartz
        return dataSource;
    }

    /**
     * JDBC Job Store properties (activated when job-store-type=jdbc).
     * Configures Quartz to use database for persistent job storage.
     * 
     * @return JDBC job store properties
     */
    @Bean
    @ConditionalOnProperty(name = "spring.quartz.job-store-type", havingValue = "jdbc")
    public Properties quartzJdbcProperties() {
        Properties props = quartzProperties();
        
        // Override job store for JDBC
        props.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        props.setProperty("org.quartz.jobStore.driverDelegateClass", 
                         "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        props.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
        props.setProperty("org.quartz.jobStore.isClustered", "true");
        props.setProperty("org.quartz.jobStore.clusterCheckinInterval", "20000");
        props.setProperty("org.quartz.jobStore.useProperties", "false");
        
        return props;
    }
}
