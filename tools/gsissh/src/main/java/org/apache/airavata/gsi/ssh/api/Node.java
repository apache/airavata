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
package org.apache.airavata.gsi.ssh.api;

import java.util.HashMap;

public class Node {
    private String Name;
    private Core[] Cores;
    private String state;
    private HashMap<String, String> status;
    private String np;
    private String ntype;

    /**
     * @return the machine's name
     */
    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    /**
     * @return machine cores as an array
     */
    public Core[] getCores() {
        return Cores;
    }

    public void setCores(Core[] Cores) {
        this.Cores = Cores;
    }


    /**
     * @return the machine state
     */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the status
     */
    public HashMap<String, String> getStatus() {
        return status;
    }

    public void setStatus(HashMap<String, String> status) {
        this.setStatus(status);
    }


    /**
     * @return the number of cores in the machine
     */
    public String getNp() {
        return np;
    }


    public void setNp(String np) {
        this.np = np;
    }

    /**
     * @return the ntype of the machine
     */
    public String getNtype() {
        return ntype;
    }


    public void setNtype(String ntype) {
        this.ntype = ntype;
    }


}
