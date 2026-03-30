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
package org.apache.airavata.server;

import javax.sql.DataSource;

import org.apache.airavata.common.config.AiravataServerProperties;
import org.apache.airavata.server.grpc.AiravataGrpcServerConfig;
import org.apache.airavata.server.rest.AiravataRestServerConfig;
import org.apache.airavata.server.thrift.AiravataThriftServerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

@SpringBootApplication(
    scanBasePackages = {"org.apache.airavata.server", "org.apache.airavata.common.db"},
    exclude = {HibernateJpaAutoConfiguration.class}
)
@EnableConfigurationProperties(AiravataServerProperties.class)
@Import({AiravataRestServerConfig.class, AiravataGrpcServerConfig.class, AiravataThriftServerConfig.class})
public class AiravataServerMain {
    public static void main(String[] args) {
        SpringApplication.run(AiravataServerMain.class, args);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        // Scan all entity packages instead of using persistence.xml
        em.setPackagesToScan("org.apache.airavata");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "none");
        em.getJpaPropertyMap().put("hibernate.enable_lazy_load_no_trans", "true");
        em.getJpaPropertyMap().put("hibernate.physical_naming_strategy",
            "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
