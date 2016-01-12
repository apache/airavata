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
package org.apache.airavata.data.manager.core.db.dao;

import org.apache.airavata.model.data.transfer.FileTransferRequestModel;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Ignore
public class FileTransferRequestDaoTest {
    private final static Logger logger = LoggerFactory.getLogger(FileTransferRequestDaoTest.class);

    @Test
    public void testFileTransferRequestDao() throws IOException {
        FileTransferRequestModel fileTransferRequestModel = new FileTransferRequestModel();
        fileTransferRequestModel.setSrcHostCredToken("djkalbsbdaslfbalsfbslf");
        fileTransferRequestModel.setSrcFilePath("test-file-path");
        FileTransferRequestDao fileTransferRequestDao = new FileTransferRequestDao();
        String transferId = fileTransferRequestDao.createFileTransferRequest(fileTransferRequestModel);
        fileTransferRequestModel = fileTransferRequestDao.getFileTransferRequest(transferId);
        System.out.println("Transfer Id:" + fileTransferRequestModel.getTransferId());
    }
}