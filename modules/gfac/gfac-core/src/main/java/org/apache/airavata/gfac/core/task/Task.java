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
package org.apache.airavata.gfac.core.task;

import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.TaskTypes;

import java.util.Map;

/**
 * All Tasks should inherit this interface.
 */
public interface Task {

	/**
	 * Task initialization method, this method will be invoked after create a new task instance.
	 * @param propertyMap
	 * @throws TaskException
	 */
	public void init(Map<String, String> propertyMap) throws TaskException;

	/**
	 * This method will be called at the first time of task chain execution. This method should called before recover
	 * method. For a given task chain execute method only call one time. recover method may be called more than once.
	 * @param taskContext
	 * @return completed task status if success otherwise failed task status.
	 */
	public TaskStatus execute(TaskContext taskContext);

	/**
	 * This methond will be invoked at recover path.Before this method is invoked, execute method should be invoked.
	 * This method may be called zero or few time in a process chain.
	 * @param taskContext
	 * @return completed task status if success otherwise failed task status.
	 */
	public TaskStatus recover(TaskContext taskContext);

	/**
	 * Task type will be used to identify the task behaviour. eg : DATA_STAGING , JOB_SUBMISSION
	 * @return type of this task object
	 */
	public TaskTypes getType();

}
