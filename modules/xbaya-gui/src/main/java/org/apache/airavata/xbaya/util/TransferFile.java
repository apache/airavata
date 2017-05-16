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
package org.apache.airavata.xbaya.util;


public class TransferFile {
    private String submission_id;
    private boolean preserve_timestamp;
    private String DATA_TYPE;
    private boolean encrypt_data;
    private String sync_level;
    private String source_endpoint;
    private String label;
    private String destination_endpoint;
    private int length;
    private String deadline;
    private boolean notify_on_succeeded;
    private boolean notify_on_failed;
    private boolean verify_checksum;
    private boolean notify_on_inactive;
    private boolean delete_destination_extra;
    private Data[] DATA;

    public String getSubmission_id() {
        return submission_id;
    }

    public void setSubmission_id(String submission_id) {
        this.submission_id = submission_id;
    }

    public boolean getPreserve_timestamp() {
        return preserve_timestamp;
    }

    public void setPreserve_timestamp(boolean preserve_timestamp) {
        this.preserve_timestamp = preserve_timestamp;
    }

    public String getDATA_TYPE() {
        return DATA_TYPE;
    }

    public void setDATA_TYPE(String DATA_TYPE) {
        this.DATA_TYPE = DATA_TYPE;
    }

    public boolean getEncrypt_data() {
        return encrypt_data;
    }

    public void setEncrypt_data(boolean encrypt_data) {
        this.encrypt_data = encrypt_data;
    }

    public String getSync_level() {
        return sync_level;
    }

    public void setSync_level(String sync_level) {
        this.sync_level = sync_level;
    }

    public String getSource_endpoint() {
        return source_endpoint;
    }

    public void setSource_endpoint(String source_endpoint) {
        this.source_endpoint = source_endpoint;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDestination_endpoint() {
        return destination_endpoint;
    }

    public void setDestination_endpoint(String destination_endpoint) {
        this.destination_endpoint = destination_endpoint;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public boolean getNotify_on_succeeded() {
        return notify_on_succeeded;
    }

    public void setNotify_on_succeeded(boolean notify_on_succeeded) {
        this.notify_on_succeeded = notify_on_succeeded;
    }

    public boolean getNotify_on_failed() {
        return notify_on_failed;
    }

    public void setNotify_on_failed(boolean notify_on_failed) {
        this.notify_on_failed = notify_on_failed;
    }

    public boolean getVerify_checksum() {
        return verify_checksum;
    }

    public void setVerify_checksum(boolean verify_checksum) {
        this.verify_checksum = verify_checksum;
    }

    public boolean getNotify_on_inactive() {
        return notify_on_inactive;
    }

    public void setNotify_on_inactive(boolean notify_on_inactive) {
        this.notify_on_inactive = notify_on_inactive;
    }

    public boolean getDelete_destination_extra() {
        return delete_destination_extra;
    }

    public void setDelete_destination_extra(boolean delete_destination_extra) {
        this.delete_destination_extra = delete_destination_extra;
    }

    public Data[] getDATA() {
        return DATA;
    }

    public void setDATA(Data[] DATA) {
        this.DATA = DATA;
    }
}
