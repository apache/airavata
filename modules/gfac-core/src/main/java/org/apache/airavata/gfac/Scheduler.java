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

package org.apache.airavata.gfac;

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.provider.impl.BESProvider;
import org.apache.airavata.gfac.provider.impl.EC2Provider;
import org.apache.airavata.gfac.provider.impl.GramProvider;
import org.apache.airavata.gfac.provider.impl.LocalProvider;
import org.apache.airavata.schemas.gfac.Ec2HostType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.UnicoreHostType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Scheduler decides the execution order of handlers based on application description. In addition
 * to that scheduler decides which provider to invoke at the end. Scheduler will set
 * provider instance and in/out handler chains in JobExecutionContext.
 */
public class Scheduler {
    private static Logger log = LoggerFactory.getLogger(Scheduler.class);

    /**
     * Decide which provider to use and execution sequence of handlers based on job request and
     * job configuration.
     * @param jobExecutionContext containing job request as well as all the configurations.
     */
    public static void schedule(JobExecutionContext jobExecutionContext) {
        // Current implementation only support static handler sequence.
        jobExecutionContext.setProvider(getProvider(jobExecutionContext));
        // TODO: Selecting the provider based on application description.
    }

    /**
     * Figure out which provider to use based on application configuration.
     * @param jobExecutionContext containing all the required configurations.
     * @return GFacProvider instance.
     */
    private static GFacProvider getProvider(JobExecutionContext jobExecutionContext){
        HostDescription hostDescription = jobExecutionContext.getApplicationContext().getHostDescription();
        if(hostDescription.getType() instanceof GlobusHostType){
            return new GramProvider();
        }
        else if (hostDescription.getType() instanceof UnicoreHostType) {
        	return new BESProvider();
        }
        else if (hostDescription.getType() instanceof Ec2HostType) {
            return new EC2Provider();
        }
        else {
            return new LocalProvider();
        }
    }


}
