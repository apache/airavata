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

package org.apache.airavata.xbaya.component.dynamic;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.invoker.Invoker;

import xsul.wsif.WSIFMessage;
import xsul.xwsif_runtime.WSIFClient;

public class DynamicInvoker implements Invoker {

    private URL jarUrl;

    private String className;

    private String operationName;

    private Object[] inputs;

    private Object result;

    /**
     * Constructs a DynamicInvoker.
     * 
     * @param className
     * @param jarUrl
     * @param operationName
     */
    public DynamicInvoker(String className, URL jarUrl, String operationName, Object[] inputs) {
        this.className = className;
        this.jarUrl = jarUrl;
        this.operationName = operationName;
        this.inputs = inputs;
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.WorkflowInvoker#getOutput(java.lang.String)
     */
    public Object getOutput(String name) throws XBayaException {
        waitToFinish();
        return result;
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.WorkflowInvoker#invoke()
     */
    public boolean invoke() throws XBayaException {
        try {
            Class<?> targetClass = Class.forName(this.className);
            Object obj = targetClass.newInstance();

            Method[] methods = targetClass.getDeclaredMethods();
            Method targetMethod = null;
            for (Method method : methods) {
                if (this.operationName.equals(method.getName())) {
                    targetMethod = method;
                    break;
                }
            }
            if (targetMethod == null) {
                throw new XBayaException("Could not find the method using reflection: " + this.operationName);
            }

            targetMethod.setAccessible(true);
            this.result = targetMethod.invoke(obj, inputs);

        } catch (Exception e) {
            throw new XBayaException(e);
        }
        return true;
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.WorkflowInvoker#setInput(java.lang.String, java.lang.Object)
     */
    public void setInput(String name, Object value) throws XBayaException {

    }

    /**
     * @see org.apache.airavata.xbaya.invoker.WorkflowInvoker#setOperation(java.lang.String)
     */
    public void setOperation(String operationName) throws XBayaException {
        this.operationName = operationName;
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.WorkflowInvoker#setup()
     */
    public void setup() throws XBayaException {
        Class[] parameters = new Class[] { URL.class };
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { this.jarUrl });
        } catch (Throwable t) {
            t.printStackTrace();
            throw new XBayaException("Error, could not add URL to system classloader");
        }
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.WorkflowInvoker#waitToFinish()
     */
    public void waitToFinish() throws XBayaException {
        while (this.result == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /**
     * @see org.apache.airavata.xbaya.invoker.WorkflowInvoker#getOutputs()
     */
    public WSIFMessage getOutputs() throws XBayaException {
        waitToFinish();
        return (WSIFMessage) this.result;

    }

    @Override
    public WSIFClient getClient() {
        return null;
    }

    @Override
    public WSIFMessage getInputs() throws XBayaException {
        return null;
    }

    @Override
    public WSIFMessage getFault() throws XBayaException {
        return null;
    }

}