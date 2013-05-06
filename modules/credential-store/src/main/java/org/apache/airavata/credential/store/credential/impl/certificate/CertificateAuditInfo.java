package org.apache.airavata.credential.store.credential.impl.certificate;

import org.apache.airavata.credential.store.credential.AuditInfo;
import org.apache.airavata.credential.store.credential.CommunityUser;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Audit information related to community credential.
 */
@XmlRootElement
public class CertificateAuditInfo implements AuditInfo {

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

    public void setCommunityUserName(String communityUserName) {
        this.communityUserName = communityUserName;
    }

    public void setPortalUserName(String portalUserName) {
        this.portalUserName = portalUserName;
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

    public CommunityUser getCommunityUser() {
        return new CommunityUser(gatewayName, communityUserName);
    }

    public String getPortalUserId() {
        return portalUserName;
    }

    public Date getTimePersisted() {
        return credentialsRequestedTime;
    }
}
