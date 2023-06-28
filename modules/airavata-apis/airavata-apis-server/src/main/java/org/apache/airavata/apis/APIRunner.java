package org.apache.airavata.apis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@ComponentScan(basePackages = {"org.apache.airavata.apis",
        "org.apache.airavata.mft.secret.server.handler",
        "org.apache.airavata.mft.resource.server.handler",
        "org.apache.airavata.mft.api.handler",
        "org.apache.airavata.mft.controller",
        "org.apache.airavata.mft.agent"
})
@EntityScan(basePackages = {"org.apache.airavata"})
@EnableJpaAuditing
public class APIRunner {

    public static void main(String[] args) {
        SpringApplication.run(APIRunner.class, args);
    }
}
