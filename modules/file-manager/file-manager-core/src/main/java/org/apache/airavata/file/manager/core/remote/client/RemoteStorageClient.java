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
package org.apache.airavata.file.manager.core.remote.client;

import org.apache.airavata.model.file.transfer.LSEntryModel;

import java.io.File;
import java.util.List;

public interface RemoteStorageClient {

    /**
     * Reads a remote file, write it to local temporary directory and returns a file pointer to it
     * @param filePath
     * @return
     * @throws Exception
     */
    File readFile(String filePath) throws Exception;

    /**
     * Writes the source file in the local storage to specified path in the remote storage
     * @param sourceFile
     * @return
     * @throws Exception
     */
    void writeFile(File sourceFile, String filePath) throws Exception;

    /**
     * Returns a directory listing of the specified directory
     * @param directoryPath
     * @return
     * @throws Exception
     */
    List<LSEntryModel> getDirectoryListing(String directoryPath) throws Exception;

    /**
     * Move the specified file from source to destination within the same storage resource
     * @param currentPath
     * @param newPath
     * @throws Exception
     */
    void moveFile(String currentPath, String newPath) throws Exception;

    /**
     *
     * @param sourcePath
     * @param destinationPath
     * @throws Exception
     */
    void copyFile(String sourcePath, String destinationPath) throws Exception;

    /**
     * Rename file with the given name
     * @param filePath
     * @param newFileName
     * @throws Exception
     */
    void renameFile(String filePath, String newFileName) throws Exception;

    /**
     * Delete the specified file
     * @param filePath
     * @throws Exception
     */
    void deleteFile(String filePath) throws Exception;

    /**
     * Create new directory in the specified file
     * @param newDirPath
     * @throws Exception
     */
    void mkdir(String newDirPath) throws Exception;

    /**
     * Checks whether specified file exists in the remote storage system
     * @param filePath
     * @return
     * @throws Exception
     */
    boolean checkFileExists(String filePath) throws Exception;

    /**
     * Checks whether the given path is a directory
     * @param filePath
     * @return
     * @throws Exception
     */
    boolean checkIsDirectory(String filePath) throws Exception;
}