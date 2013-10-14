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
 package org.apache.airavata.gsi.ssh.impl;

 /**
  * This will contains all the PBS specific job statuses.
  * C -     Job is completed after having run/
  * E -  Job is exiting after having run.
  * H -  Job is held.
  * Q -  job is queued, eligible to run or routed.
  * R -  job is running.
  * T -  job is being moved to new location.
  * W -  job is waiting for its execution time
  * (-a option) to be reached.
  * S -  (Unicos only) job is suspend.
  */
 public enum JobStatus {
     C, E, H, Q, R, T, W, S,U;

     public static JobStatus fromString(String status){
        if(status != null){
            if("C".equals(status)){
                return JobStatus.C;
            }else if("E".equals(status)){
                return JobStatus.E;
            }else if("H".equals(status)){
                return JobStatus.H;
            }else if("Q".equals(status)){
                return JobStatus.Q;
            }else if("R".equals(status)){
                return JobStatus.R;
            }else if("T".equals(status)){
                return JobStatus.T;
            }else if("W".equals(status)){
                return JobStatus.W;
            }else if("S".equals(status)){
                return JobStatus.S;
            }
        }
         return JobStatus.U;
     }
 }
