package org.apache.airavata.credential.store.server;

import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.datamodel.CertificateCredential;
import org.apache.airavata.credential.store.datamodel.PasswordCredential;
import org.apache.airavata.credential.store.datamodel.SSHCredential;
import org.apache.thrift.TException;

public class CredentialStoreServerHandler implements CredentialStoreService.Iface {
    @Override
    public String getCSServiceVersion() throws TException {
        return null;
    }

    @Override
    public String addSSHCredential(SSHCredential sshCredential) throws TException {
        return null;
    }

    @Override
    public String addCertificateCredential(CertificateCredential certificateCredential) throws TException {
        return null;
    }

    @Override
    public String addPasswordCredential(PasswordCredential passwordCredential) throws TException {
        return null;
    }

    @Override
    public SSHCredential getSSHCredential(String tokenId) throws TException {
        return null;
    }

    @Override
    public CertificateCredential getCertificateCredential(String tokenId) throws TException {
        return null;
    }

    @Override
    public PasswordCredential getPasswordCredential(String tokenId) throws TException {
        return null;
    }
}
