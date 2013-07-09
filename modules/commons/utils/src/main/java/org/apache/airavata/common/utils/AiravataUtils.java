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

package org.apache.airavata.common.utils;

public class AiravataUtils {
	public static final String EXECUTION_MODE="application.execution.mode";
	public static void setExecutionMode(ExecutionMode mode){
		System.setProperty(EXECUTION_MODE, mode.name());
	}
	
	public static ExecutionMode getExecutionMode(){
		if (System.getProperties().containsKey(EXECUTION_MODE)) {
			return ExecutionMode.valueOf(System.getProperty(EXECUTION_MODE));
		}else{
			return null;
		}
	}
	
	public static boolean isServer(){
		return getExecutionMode()==ExecutionMode.SERVER;
	}
	
	public static boolean isClient(){
		return getExecutionMode()==ExecutionMode.CLIENT;
	}
	
	public static void setExecutionAsServer(){
		setExecutionMode(ExecutionMode.SERVER);
	}
	
	public static void setExecutionAsClient(){
		setExecutionMode(ExecutionMode.CLIENT);
	}
}
