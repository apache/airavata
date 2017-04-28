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
package org.apache.airavata.xbaya.jython.runner;

import java.util.List;

import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;

public class JythonRunner {

    private JythonClassLoader loader;

    /**
     * 
     * Constructs a JythonRunner.
     * 
     */
    public JythonRunner() {
        this.loader = new JythonClassLoader(this.getClass().getClassLoader());
    }

    /**
     * @param script
     * @param arguments
     * @throws WorkflowException
     */
    public void run(String script, List<String> arguments) throws WorkflowException {
        run(script, arguments.toArray(new String[arguments.size()]));
    }

    /**
     * @param script
     * @param arguments
     * @throws WorkflowException
     */
    public void run(final String script, final String[] arguments) throws WorkflowException {
        try {
            Class<?> runnerClass = this.loader.loadClass(JythonOneTimeRunnerImpl.class.getName(), true);
            JythonOneTimeRunner runner = (JythonOneTimeRunner) runnerClass.newInstance();
            runner.run(script, arguments);

        } catch (ClassNotFoundException e) {
            throw new WorkflowRuntimeException(ErrorMessages.UNEXPECTED_ERROR, e);
        } catch (InstantiationException e) {
            throw new WorkflowRuntimeException(ErrorMessages.UNEXPECTED_ERROR, e);
        } catch (IllegalAccessException e) {
            throw new WorkflowRuntimeException(ErrorMessages.UNEXPECTED_ERROR, e);
        } finally {
            loader.cleanUp();
        }
    }
}