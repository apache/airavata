/**
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
 */
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

package org.apache.airavata.gfac.bes.handlers;



import org.apache.airavata.gfac.bes.utils.ActivityInfo;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.handler.GFacHandler;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStateEnumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMSByteIOOutHandler extends AbstractSMSHandler implements GFacHandler{

	// TODO: later use AbstractHandler, which cannot be used due to error in RegistryFactory
		private final Logger log = LoggerFactory.getLogger(this.getClass());
		
		@Override
		public void invoke(JobExecutionContext jobExecutionContext)
				throws GFacHandlerException {
			super.invoke(jobExecutionContext);
			
			ActivityInfo activityInfo = (ActivityInfo)jobExecutionContext.getProperty(PROP_ACTIVITY_INFO);
			try {
			if(activityInfo == null) {
				log.error("No ActivityInfo instance found. The activity execution is ended due to an exception, see provider logs");
				return;
			}
			
			if ((activityInfo.getActivityStatus().getState() == ActivityStateEnumeration.FAILED)) {
	            try {Thread.sleep(5000);}catch (InterruptedException e){}
	            
	            try {
					dataTransferrer.downloadStdOuts();
				} catch (GFacProviderException e) {
					throw new GFacHandlerException("Cannot download stdout data",e);
				}
			}
	        else if (activityInfo.getActivityStatus().getState() == ActivityStateEnumeration.FINISHED) {
	        	try {Thread.sleep(5000);}catch (InterruptedException e){}
	        	
	        	try {
						if (activityInfo.getActivityStatus().getExitCode() == 0) {
							dataTransferrer.downloadRemoteFiles();
						} else {
							dataTransferrer.downloadStdOuts();
						}
					} catch (GFacProviderException e) {
						throw new GFacHandlerException(
								"Cannot download stdout data", e);
					}
				}
			} finally {
				try {
					if (storageClient != null) {
						storageClient.destroy();
					}
				} catch (Exception e) {
					log.warn("Cannot destroy temporary SMS instance", e);
				}

			}		

	 	}

    @Override
    public void recover(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        // TODO: Auto generated method body.
    }
}

