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

/**
 * Application Parallelism Enumerations.
 *
*/

namespace java org.apache.airavata.model.parallelism
namespace php Airavata.Model.AppCatalog.Parallelism
namespace cpp apache.airavata.model.appcatalog.parallelism
namespace py airavata.model.appcatalog.parallelism

/**
 * Enumeration of application parallelism supported by Airavata
 *
 * SERIAL:
 *  Single processor applications without any parallelization.
 *
 * MPI:
 *  Messaging Passing Interface.
 *
 * OPENMP:
 *  Shared Memory Implementtaion.
 *
 * OPENMP_MPI:
 *  Hybrid Applications.
 *
*/
enum ApplicationParallelismType {
    SERIAL,
    MPI,
    OPENMP,
    OPENMP_MPI,
    CCM,
    CRAY_MPI
}
