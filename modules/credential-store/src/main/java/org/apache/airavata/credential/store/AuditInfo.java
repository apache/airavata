package org.apache.airavata.credential.store;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 * Audit information related to community credential.
 */
@XmlRootElement
public class AuditInfo implements Serializable {

    private static final long serialVersionUID = 13213123L;

    private String gatewayName;
    private String communityUserName;
    private String portalUserName;
    private Date credentialsRequestedTime;
    private String notBefore;
    private String notAfter;
    private long credentialLifeTime;

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getCommunityUserName() {
        return communityUserName;
    }

    public void setCommunityUserName(String communityUserName) {
        this.communityUserName = communityUserName;
    }

    public String getPortalUserName() {
        return portalUserName;
    }

    public void setPortalUserName(String portalUserName) {
        this.portalUserName = portalUserName;
    }

    public Date getCredentialsRequestedTime() {
        return credentialsRequestedTime;
    }

    public void setCredentialsRequestedTime(Date credentialsRequestedTime) {
        this.credentialsRequestedTime = credentialsRequestedTime;
    }

    public String getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(String notBefore) {
        this.notBefore = notBefore;
    }

    public String getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(String notAfter) {
        this.notAfter = notAfter;
    }

    public long getCredentialLifeTime() {
        return credentialLifeTime;
    }

    public void setCredentialLifeTime(long credentialLifeTime) {
        this.credentialLifeTime = credentialLifeTime;
    }
}
