package org.apache.airavata.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

/**
 * Binds the {@code airavata.servers} list property.
 * Consumers must register via {@code @EnableConfigurationProperties(AiravataServerProperties.class)}.
 */
@ConfigurationProperties(prefix = "airavata")
public class AiravataServerProperties {
    private List<String> servers = List.of("thrift", "rest", "grpc");

    public List<String> getServers() { return servers; }
    public void setServers(List<String> servers) { this.servers = servers; }
}
