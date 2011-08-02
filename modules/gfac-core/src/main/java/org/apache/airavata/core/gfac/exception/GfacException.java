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

import java.text.DateFormat;
import java.util.Date;

import org.apache.xmlbeans.XmlObject;
import org.ogce.schemas.gfac.inca.faults.ActionDocument;
import org.ogce.schemas.gfac.inca.faults.ActionDocument.Action;
import org.ogce.schemas.gfac.inca.faults.Application;
import org.ogce.schemas.gfac.inca.faults.DataTransfer;
import org.ogce.schemas.gfac.inca.faults.FileSystem;
import org.ogce.schemas.gfac.inca.faults.Job;

public class GfacException extends Exception {

    public static enum FaultCode {
        ServiceNotReachable, InvalidRequest, InternalServiceError, ErrorAtDependentService, ErrorAtClientBeforeWsCall, ErrorAtClientWhileWsCall, ErrorAtClientAfterWsCall, InitalizationError, UnsupportedMessage, InvaliedLocalArgumnet, LocalError, CmdAppError, ErrorAtCreatedService, InvalidConfig, Unknown, InvaliedResponse, JobSubmissionFailed
    };

    private static final long serialVersionUID = 1L;
    private static DateFormat formatter = DateFormat.getTimeInstance();
    protected ActionDocument errorActionDocument;

    protected String faultCode;

    public GfacException(String message, Throwable cause) {
        super(message, cause);
    }

    public GfacException(String message, Throwable cause, FaultCode faultCode) {
        super(message, cause);
        this.faultCode = faultCode.toString();
    }

    public GfacException(String message, FaultCode faultCode) {
        super(message);
        this.faultCode = faultCode.toString();
    }

    public GfacException(Throwable cause, FaultCode faultCode) {
        super(cause);
        if (cause instanceof GfacException) {
            GfacException gfacExeption = (GfacException) cause;
            setFaultCode(gfacExeption.getFaultCode());
        }
        this.faultCode = faultCode.toString();
    }

    public String getFaultCode() {
        return faultCode != null ? faultCode : getMessage();
    }

    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode;
    }

    public ActionDocument createFaultData(XmlObject fault, String api, Throwable e) {
        ActionDocument document = ActionDocument.Factory.newInstance();
        Action type = document.addNewAction();
        type.setId("");
        type.setApplication("lead");
        type.setApi(api);

        if (fault instanceof DataTransfer) {
            type.setDataTransferArray(new DataTransfer[] { (DataTransfer) fault });
        } else if (fault instanceof Job) {
            type.setJobArray(new Job[] { (Job) fault });
        } else if (fault instanceof Job) {
            type.setFileSystemArray(new FileSystem[] { (FileSystem) fault });
        } else if (fault instanceof Application) {
            type.setCmdapplicationArray(new Application[] { (Application) fault });
        }
        type.setEndTime(formatter.format(new Date()));
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return document;
    }
}
