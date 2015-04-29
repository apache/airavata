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
package org.apache.airavata.workflow.engine.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Used to run jobs that need to be started after a predicate is
 *
 * @author Chathura Herath
 */
public class PredicatedTaskRunner {

	protected volatile ConcurrentLinkedQueue<PredicatedExecutable> jobQueue = new ConcurrentLinkedQueue<PredicatedExecutable>();

	protected ExecutorService threadPool;

	protected volatile boolean stop = false;
    private static final Logger logger = LoggerFactory.getLogger(PredicatedTaskRunner.class);

	public PredicatedTaskRunner(int numberOfThreads) {
		this.threadPool = Executors.newFixedThreadPool(numberOfThreads);
		addIdleTask();
		startCheckThread();

	}

	private void addIdleTask() {
		PredicatedExecutable sleepTask = new PredicatedExecutable() {

			@Override
			public void run() {
				synchronized (jobQueue) {
					if (jobQueue.size() == 1) {
						try {
							jobQueue.wait();
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					} else if (allTasksAreWaiting(jobQueue)) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
				}

			}

			private boolean allTasksAreWaiting(
					ConcurrentLinkedQueue<PredicatedExecutable> jobQueue) {
				for (PredicatedExecutable predicatedExecutable : jobQueue) {
					if (predicatedExecutable.isReady()) {
						return false;
					}
				}
				return true;

			}

			@Override
			public boolean isReady() {
				// TODO Auto-generated method stub
				return true;
			}
		};

		this.jobQueue.add(sleepTask);
	}

	private void startCheckThread() {
		new Thread(new Runnable() {

			@Override
			public void run() {

				while (!stop) {
					try {
						synchronized (jobQueue) {
							while(jobQueue.size() == 0 || allTasksAreWaiting(jobQueue)){
								jobQueue.wait(50);
							}
						}

						PredicatedExecutable job = jobQueue.remove();
						if (job.isReady()) {
							// remove from front and execute and you are done
							threadPool.execute(job);
						} else {
							// add to end if not ready to run
							jobQueue.add(job);
						}



					} catch (Throwable e) {
						// we go on no matter what
                        logger.error(e.getMessage(), e);
					}
				}

			}
		}).start();
	}

	private  boolean allTasksAreWaiting(
			ConcurrentLinkedQueue<PredicatedExecutable> jobQueue) {
		for (PredicatedExecutable predicatedExecutable : jobQueue) {
			if (predicatedExecutable.isReady()) {
				return false;
			}
		}
		return true;

	}

	public void scedule(PredicatedExecutable job) {

		synchronized (jobQueue) {
			this.jobQueue.add(job);
			this.jobQueue.notifyAll();
		}

	}

	public void shutDown() {
		this.threadPool.shutdown();
		this.stop = true;
	}

}
