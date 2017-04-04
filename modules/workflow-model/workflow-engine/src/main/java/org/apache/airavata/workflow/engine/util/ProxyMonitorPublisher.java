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
package org.apache.airavata.workflow.engine.util;

import org.apache.airavata.common.utils.LocalEventPublisher;
import org.apache.airavata.common.utils.listener.AbstractActivityListener;

public class ProxyMonitorPublisher implements AbstractActivityListener{

	private static Object[] setupConfigurations;
	
	@Override
	public void setup(Object... configurations) {
		setupConfigurations=configurations;
	}
	
	private static LocalEventPublisher getPublisher(){
		if (setupConfigurations!=null) {
			for (Object configuration : setupConfigurations) {
				if (configuration instanceof LocalEventPublisher){
					return (LocalEventPublisher) configuration;
				}
			}
		}
		return null;
	}
	
    public static void registerListener(Object listener) {
    	if (listener instanceof AbstractActivityListener){
    		((AbstractActivityListener) listener).setup(setupConfigurations);
    	}
        getPublisher().registerListener(listener);
    }
    
    public static void unregisterListener(Object listener) {
        getPublisher().unregisterListener(listener);
    }

    public void publish(Object o) {
        getPublisher().publish(o);
    }

}
