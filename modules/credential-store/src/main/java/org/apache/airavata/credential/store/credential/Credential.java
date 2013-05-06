package org.apache.airavata.credential.store.credential;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * This class represents the actual credential. The credential can be a certificate, user name password
 * or a SSH key. As per now we only have certificate implementation.
 */
public abstract class Credential implements Serializable {

    private String portalUserName;
    private Date persistedTime;
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setPortalUserName(String userName) {
        portalUserName = userName;
    }

    public String getPortalUserName() {
        return portalUserName;
    }

    public void setCertificateRequestedTime(Date ts) {
        persistedTime = ts;
    }

    public Date getCertificateRequestedTime() {
        return persistedTime;
    }


}
