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

package org.apache.airavata.gfac.handler;

import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;

public class GFacHandlerException extends GFacException{
    public GFacHandlerException(String s) {
        super(s,new Throwable(s));
    }

    public GFacHandlerException(String s, Throwable throwable) {
        super(s, throwable);
    }
        public GFacHandlerException(String message, Throwable cause,JobExecutionContext context) {
        super(message, cause);
        sendFaultNotification(message,context,new Exception(cause));
    }

    public GFacHandlerException(String message, JobExecutionContext context) {
        super(message,new Throwable(message));
        sendFaultNotification(message,context,new Exception(message));
    }

    public GFacHandlerException(String message, JobExecutionContext context,Exception e,String... additionExceptiondata) {
        super(message,e);
        sendFaultNotification(message,context,e, additionExceptiondata);
    }

    private void sendFaultNotification(String message,
			JobExecutionContext executionContext, Exception e,
			String... additionalExceptiondata) {
		if (additionalExceptiondata==null || additionalExceptiondata.length==0){
        	additionalExceptiondata=new String[]{message,e.getLocalizedMessage()};
        }
	}
}
