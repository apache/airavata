/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

namespace java org.apache.airavata.model.appcatalog.datamodels
namespace php Airavata.Model.AppCatalog.DataModels
namespace cpp apache.airavata.model.appcatalog.datamodels
namespace py airavata.model.appcatalog.datamodels

struct FileStructure {
    1: required string name;
    2: required string path;
    3: required bool isFile;
    4: required i64 createdDate;
    5: required i64 modifiedDate;
    6: optional binary content;
    7: required i64 size;
}
