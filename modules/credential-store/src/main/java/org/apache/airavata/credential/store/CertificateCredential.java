package org.apache.airavata.credential.store;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Represents the certificate credentials.
 */
public class CertificateCredential implements Credential {

    public CertificateCredential() {

    }

    /**
     * The community user associated with this credentials.
     */
    private CommunityUser communityUser;

    private X509Certificate certificate;

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    private PrivateKey privateKey;

    private long lifeTime;

    private String portalUserName;

    private String notBefore;

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

    private String notAfter;

    public Date getCertificateRequestedTime() {
        return certificateRequestedTime;
    }

    public void setCertificateRequestedTime(Date certificateRequestedTime) {
        this.certificateRequestedTime = certificateRequestedTime;
    }

    private Date certificateRequestedTime;

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public long getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(long lifeTime) {
        this.lifeTime = lifeTime;
    }

    public String getPortalUserName() {
        return portalUserName;
    }

    public void setPortalUserName(String portalUserName) {
        this.portalUserName = portalUserName;
    }

    public CommunityUser getCommunityUser() {
        return communityUser;
    }

    public void setCommunityUser(CommunityUser communityUser) {
        this.communityUser = communityUser;
    }

}
