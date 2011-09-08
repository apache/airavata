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

package org.apache.airavata.core.gfac.scheduler.impl;

import java.net.UnknownHostException;

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.exception.SchedulerException;
import org.apache.airavata.core.gfac.provider.GramProvider;
import org.apache.airavata.core.gfac.provider.LocalProvider;
import org.apache.airavata.core.gfac.provider.Provider;
import org.apache.airavata.core.gfac.scheduler.Scheduler;
import org.apache.airavata.core.gfac.utils.GfacUtils;

public class POJOSchedulerImpl implements Scheduler {

    public Provider schedule(InvocationContext context) throws SchedulerException {
        try {
            HostDescription host = context.getExecutionDescription().getHost();
            if (GfacUtils.isLocalHost(host.getName())) {
                return new LocalProvider();
            }
            return new GramProvider();
        } catch (UnknownHostException e) {
            throw new SchedulerException("Cannot get IP for current host", e);
        }
    }
}
