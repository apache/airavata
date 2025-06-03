package org.apache.airavata.file.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "org.apache.airavata.file.server",
        exclude = {GroovyTemplateAutoConfiguration.class})
@EnableConfigurationProperties(FileServerConfiguration.class)
public class FileServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileServerApplication.class, args);
    }
}
