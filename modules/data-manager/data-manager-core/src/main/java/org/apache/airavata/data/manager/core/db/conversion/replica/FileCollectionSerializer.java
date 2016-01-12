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
package org.apache.airavata.data.manager.core.db.conversion.replica;

import org.apache.airavata.data.manager.core.db.conversion.AbstractThriftSerializer;
import org.apache.airavata.model.data.replica.FileCollectionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCollectionSerializer extends
        AbstractThriftSerializer<FileCollectionModel._Fields, FileCollectionModel> {
    private final static Logger logger = LoggerFactory.getLogger(FileCollectionSerializer.class);

    @Override
    protected FileCollectionModel._Fields[] getFieldValues() {
        return FileCollectionModel._Fields.values();
    }

    @Override
    protected Class<FileCollectionModel> getThriftClass() {
        return null;
    }
}