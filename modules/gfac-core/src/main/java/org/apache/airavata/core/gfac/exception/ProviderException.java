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

import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.provider.Provider;

/**
 * The exception for {@link Provider}
 *
 */
public class ProviderException extends GfacException {

	private static final long serialVersionUID = 7994167766799131223L;
    private String aditionalInfo[] = null;

	public ProviderException(String message, InvocationContext invocationContext, String...additionalExceptiondata) {
        super(message);
        aditionalInfo = additionalExceptiondata;
        sendFaultNotification(message, invocationContext, new Exception(message),additionalExceptiondata);
    }

    public ProviderException(String message, Throwable cause, InvocationContext invocationContext, String...additionalExceptiondata) {
        super(message, cause);
        Exception e = new Exception(cause);
        aditionalInfo = additionalExceptiondata;
        sendFaultNotification(message, invocationContext, e, additionalExceptiondata);
    }

	private void sendFaultNotification(String message,
			InvocationContext invocationContext, Exception e,
			String... additionalExceptiondata) {
		if (additionalExceptiondata==null || additionalExceptiondata.length==0){
        	additionalExceptiondata=new String[]{message,e.getLocalizedMessage()};
        }
		invocationContext.getExecutionContext().getNotifier().executionFail(invocationContext,e,additionalExceptiondata);
	}

    public String[] getAditionalInfo() {
        return aditionalInfo;
    }
}
