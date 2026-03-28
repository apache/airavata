package org.apache.airavata.research.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "airavata.research")
public class ResearchProperties {
    private String hubUrl;
    private String portalUrl;
    private String devUrl;
    private String devUser;
    private String adminApiKey;
    private int limit = 10;

    public String getHubUrl() { return hubUrl; }
    public void setHubUrl(String hubUrl) { this.hubUrl = hubUrl; }
    public String getPortalUrl() { return portalUrl; }
    public void setPortalUrl(String portalUrl) { this.portalUrl = portalUrl; }
    public String getDevUrl() { return devUrl; }
    public void setDevUrl(String devUrl) { this.devUrl = devUrl; }
    public String getDevUser() { return devUser; }
    public void setDevUser(String devUser) { this.devUser = devUser; }
    public String getAdminApiKey() { return adminApiKey; }
    public void setAdminApiKey(String adminApiKey) { this.adminApiKey = adminApiKey; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
}
