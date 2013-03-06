package org.apache.airavata.gfac.provider.utils;

import org.apache.airavata.gfac.provider.GFacProviderException;
import org.ietf.jgss.GSSCredential;

import java.io.File;
import java.net.URI;
import java.util.Vector;

public interface FileTransferService {
    public URI copy(URI src, URI dest, GSSCredential gssCred) throws GFacProviderException;

    public URI forcedCopy(URI src, URI dest, GSSCredential gssCred) throws GFacProviderException;

    public URI copyToDir(URI src, URI destDir, GSSCredential gssCred) throws GFacProviderException;

    public void makeDir(URI destURI, GSSCredential gssCred) throws GFacProviderException;

    public Vector<URI> listDir(URI srcURI, GSSCredential gssCred) throws GFacProviderException;

    public String readRemoteFile(URI destURI, GSSCredential gsCredential,
                                 File localFile) throws GFacProviderException;

    public boolean isExisits(URI uri, GSSCredential gssCred) throws GFacProviderException;

    public URI copyWithDataID(DataIDType src, URI destURL, GSSCredential gssCred)
            throws GFacProviderException;

    public DataIDType store(URI src) throws GFacProviderException;

    public ContactInfo findContact(URI uri) throws GFacProviderException;

    public URI[] copyToDir(URI[] srcList, URI destDir, GSSCredential gssCred) throws GFacProviderException;
}

