package org.apache.airavata.credential.store.credential.impl.certificate;

import org.apache.airavata.credential.store.credential.CommunityUser;
import org.apache.airavata.credential.store.credential.Credential;import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Represents the certificate credentials.
 */
public class CertificateCredential extends Credential {

    static final long serialVersionUID = 6603675553790734432L;

    /**
     * The community user associated with this credentials.
     */
    private CommunityUser communityUser;

    private String notAfter;

    private X509Certificate certificate;

    private PrivateKey privateKey;

    private long lifeTime;

    private String notBefore;

    public CertificateCredential() {
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


    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

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

    public CommunityUser getCommunityUser() {
        return communityUser;
    }

    public void setCommunityUser(CommunityUser communityUser) {
        this.communityUser = communityUser;
    }

}
