package org.apache.airavata.gfac.provider.utils;

import org.apache.airavata.core.gfac.exception.GfacException;
import org.ietf.jgss.GSSCredential;

import java.io.File;
import java.net.URI;
import java.util.Vector;

public interface FileTransferService {
    public URI copy(URI src, URI dest, GSSCredential gssCred) throws GfacException;

    public URI forcedCopy(URI src, URI dest, GSSCredential gssCred) throws GfacException;

    public URI copyToDir(URI src, URI destDir, GSSCredential gssCred) throws GfacException;

    public void makeDir(URI destURI, GSSCredential gssCred) throws GfacException;

    public Vector<URI> listDir(URI srcURI, GSSCredential gssCred) throws GfacException;

    public String readRemoteFile(URI destURI, GSSCredential gsCredential,
                                 File localFile) throws GfacException;

    public boolean isExisits(URI uri, GSSCredential gssCred) throws GfacException;

    public URI copyWithDataID(DataIDType src, URI destURL, GSSCredential gssCred)
            throws GfacException;

    public DataIDType store(URI src) throws GfacException;

    public ContactInfo findContact(URI uri) throws GfacException;

    public URI[] copyToDir(URI[] srcList, URI destDir, GSSCredential gssCred) throws GfacException;
}

