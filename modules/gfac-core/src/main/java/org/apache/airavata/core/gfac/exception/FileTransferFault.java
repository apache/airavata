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

package org.apache.airavata.core.gfac.exception;

import java.net.URI;

import org.apache.airavata.core.gfac.utils.GFacOptions.FileTransferServiceType;
import org.ogce.schemas.gfac.inca.faults.DataTransfer;

public class FileTransferFault extends GfacException {
    public FileTransferFault(Throwable cause, FileTransferServiceType api, String origHost, URI source, URI dest,
            String options) {
        super(cause, FaultCode.ErrorAtDependentService);
        DataTransfer dataTransfer = DataTransfer.Factory.newInstance();
        dataTransfer.setOrigHost(origHost);
        dataTransfer.setSource(source.toString());
        dataTransfer.setDest(dest.toString());
        dataTransfer.setOptions(options);
        errorActionDocument = createFaultData(dataTransfer, api.toString(), cause);
    }
}
