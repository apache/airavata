package org.apache.airavata.apis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@ComponentScan(basePackages = {"org.apache.airavata",
        "org.apache.airavata.mft.secret.server.handler",
        "org.apache.airavata.mft.resource.server.handler"
})
@EntityScan(basePackages = {"org.apache.airavata"})
@PropertySource(value = "classpath:api.properties")
public class APIRunner {

    public static void main(String[] args) {
        SpringApplication.run(APIRunner.class, args);
    }
}
