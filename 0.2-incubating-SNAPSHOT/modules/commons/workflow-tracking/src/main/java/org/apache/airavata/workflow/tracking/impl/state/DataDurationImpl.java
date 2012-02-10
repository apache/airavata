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

package org.apache.airavata.workflow.tracking.impl.state;

import java.net.URI;

import org.apache.airavata.workflow.tracking.common.DataDurationObj;
import org.apache.airavata.workflow.tracking.common.DataObj;

public class DataDurationImpl extends DurationImpl implements DataDurationObj {

    protected DataObj dataObj;
    protected URI remoteLocation;

    public DataDurationImpl(DataObj dataObj_, URI remoteLocation_) {

        super(); // set start time to now
        dataObj = dataObj_;
        remoteLocation = remoteLocation_;
    }

    public DataDurationImpl(DataObj dataObj_, URI remoteLocation_, long fixedDuration) {

        super(fixedDuration); // set duration to passed value
        dataObj = dataObj_;
        remoteLocation = remoteLocation_;
    }

    public DataObj getDataObj() {

        return dataObj;
    }

    public URI getRemoteLocation() {

        return remoteLocation;
    }

}
