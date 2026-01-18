/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.agent.connection.service.handlers;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import java.nio.charset.Charset;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.fuse.DirEntry;
import org.apache.airavata.fuse.FileEntry;
import org.apache.airavata.fuse.FileInfo;
import org.apache.airavata.fuse.FileInfoReq;
import org.apache.airavata.fuse.FileInfoRes;
import org.apache.airavata.fuse.FuseServiceGrpc;
import org.apache.airavata.fuse.InodeAtt;
import org.apache.airavata.fuse.OpenDirReq;
import org.apache.airavata.fuse.OpenDirRes;
import org.apache.airavata.fuse.OpenFileReq;
import org.apache.airavata.fuse.OpenFileRes;
import org.apache.airavata.fuse.OpenedDir;
import org.apache.airavata.fuse.OpenedFile;
import org.apache.airavata.fuse.ReadDirReq;
import org.apache.airavata.fuse.ReadDirRes;
import org.apache.airavata.fuse.ReadFileReq;
import org.apache.airavata.fuse.ReadFileRes;
import org.apache.airavata.fuse.SetInodeAttReq;
import org.apache.airavata.fuse.SetInodeAttRes;
import org.apache.airavata.fuse.StatFs;
import org.apache.airavata.fuse.StatFsReq;
import org.apache.airavata.fuse.StatFsRes;
import org.apache.airavata.fuse.WriteFileReq;
import org.apache.airavata.fuse.WriteFileRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FuseFSHandler extends FuseServiceGrpc.FuseServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(FuseFSHandler.class);

    @Override
    public void statFs(StatFsReq request, StreamObserver<StatFsRes> responseObserver) {
        responseObserver.onNext(StatFsRes.newBuilder()
                .setResult(StatFs.newBuilder()
                        .setBlocks(100)
                        .setBlocksAvailable(100)
                        .setBlocksFree(100)
                        .setInodes(1)
                        .setIoSize(10)
                        .setBlockSize(1000)
                        .build())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void fileInfo(FileInfoReq request, StreamObserver<FileInfoRes> responseObserver) {
        LOGGER.info("Calling fileInfo {}", request.getName());

        responseObserver.onNext(FileInfoRes.newBuilder()
                .setResult(FileInfo.newBuilder()
                        .setName(request.getName())
                        .setSize(128)
                        .setIno(2)
                        .setIsDir(true)
                        .setMode(0x777)
                        .setModTime(Timestamp.newBuilder()
                                .setSeconds(AiravataUtils.getUniqueTimestamp().getTime() / 1000)
                                .build())
                        .build())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void openDir(OpenDirReq request, StreamObserver<OpenDirRes> responseObserver) {
        LOGGER.info("Calling openDir {}", request.getName());
        responseObserver.onNext(OpenDirRes.newBuilder()
                .setResult(OpenedDir.newBuilder().build())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void openFile(OpenFileReq request, StreamObserver<OpenFileRes> responseObserver) {
        LOGGER.info("Calling openFile {}", request.getName());
        responseObserver.onNext(OpenFileRes.newBuilder()
                .setResult(OpenedFile.newBuilder().build())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void readDir(ReadDirReq request, StreamObserver<ReadDirRes> responseObserver) {
        LOGGER.info("Calling readDir {}", request.getName());
        responseObserver.onNext(ReadDirRes.newBuilder()
                .addResult(DirEntry.newBuilder()
                        .setIsDir(false)
                        .setName("file1")
                        .setFileMode(777)
                        .setInfo(FileInfo.newBuilder()
                                .setModTime(Timestamp.newBuilder()
                                        .setSeconds(AiravataUtils.getUniqueTimestamp()
                                                        .getTime()
                                                / 1000)
                                        .build())
                                .setName("file2")
                                .setIno(100)
                                .setSize(12000)
                                .setIsDir(false)
                                .setMode(777)
                                .build())
                        .build())
                .build());

        responseObserver.onCompleted();
    }

    @Override
    public void readFile(ReadFileReq request, StreamObserver<ReadFileRes> responseObserver) {
        LOGGER.info("Calling readFile {}", request.getName());
        responseObserver.onNext(ReadFileRes.newBuilder()
                .setResult(FileEntry.newBuilder()
                        .setDst(ByteString.copyFrom("Hellllo", Charset.defaultCharset()))
                        .build())
                .build());

        responseObserver.onCompleted();
    }

    @Override
    public void writeFile(WriteFileReq request, StreamObserver<WriteFileRes> responseObserver) {
        LOGGER.info("Calling writeFile {}", request.getName());
        responseObserver.onNext(WriteFileRes.newBuilder().setResult(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void setInodeAtt(SetInodeAttReq request, StreamObserver<SetInodeAttRes> responseObserver) {
        LOGGER.info("Calling setInodeAtt {}", request.getName());

        responseObserver.onNext(SetInodeAttRes.newBuilder()
                .setResult(InodeAtt.newBuilder()
                        .setAtime(Timestamp.newBuilder()
                                .setSeconds(AiravataUtils.getUniqueTimestamp().getTime() / 1000)
                                .build())
                        .setCtime(Timestamp.newBuilder()
                                .setSeconds(AiravataUtils.getUniqueTimestamp().getTime() / 1000)
                                .build())
                        .setMtime(Timestamp.newBuilder()
                                .setSeconds(AiravataUtils.getUniqueTimestamp().getTime() / 1000)
                                .build())
                        .setFileMode(777)
                        .setSize(10800)
                        .build())
                .build());

        responseObserver.onCompleted();
    }
}
