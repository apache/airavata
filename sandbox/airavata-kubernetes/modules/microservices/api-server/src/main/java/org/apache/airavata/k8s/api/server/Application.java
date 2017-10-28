package org.apache.airavata.k8s.api.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * TODO: Class level comments please
 *
 * @author Dimuthu Upeksha
 * @since 1.0.0-SNAPSHOT
 */
@SpringBootApplication(scanBasePackages={"org.apache.airavata.k8s.api.server.controller"})
@EnableJpaRepositories(basePackages = {"org.apache.airavata.k8s.api.server.repository"})
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }

}
