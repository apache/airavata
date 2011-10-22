/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.xbaya.gridftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.globus.ftp.DataSink;
import org.globus.ftp.DataSinkStream;
import org.globus.ftp.DataSource;
import org.globus.ftp.DataSourceStream;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.Marker;
import org.globus.ftp.MarkerListener;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.ServerException;
import org.ietf.jgss.GSSCredential;

public class GridFtpClient implements MarkerListener {

    public void upload(File file, String directory, String remoteFile, GSSCredential credential) {
        try {
            GridFTPClient gridFTPClient = new GridFTPClient("", 9393);
            gridFTPClient.authenticate(credential);
            DataSource source = new DataSourceStream(new FileInputStream(file));
            if (null != directory) {
                gridFTPClient.changeDir(directory);
            }
            gridFTPClient.extendedPut(remoteFile, source, this);
        } catch (ServerException e) {
            throw new XBayaRuntimeException(e);
        } catch (IOException e) {
            throw new XBayaRuntimeException(e);
        } catch (ClientException e) {
            throw new XBayaRuntimeException(e);
        }
    }

    public void downloadFile(String remoteFileName, String directory, GSSCredential credential, OutputStream out) {
        try {
            GridFTPClient gridFTPClient = new GridFTPClient("", 9393);
            gridFTPClient.authenticate(credential);
            gridFTPClient.changeDir(directory);
            DataSink sink = new DataSinkStream(out);
            gridFTPClient.get(remoteFileName, sink, this);
        } catch (ServerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @see org.globus.ftp.MarkerListener#markerArrived(org.globus.ftp.Marker)
     */
    public void markerArrived(Marker arg0) {
        // TODO Auto-generated method stub

    }

}