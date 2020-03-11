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
 */

 package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.transfer.TransferModel;
import org.apache.airavata.registry.core.RegistryException;
import org.apache.airavata.registry.core.entities.expcatalog.TransferEntity;
import org.apache.airavata.registry.core.entities.expcatalog.TransferEntityPK;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.QueryConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransferRepository extends ExpCatAbstractRepository<TransferModel, TransferEntity, TransferEntityPK>  {

    public TransferRepository() {
        super(TransferModel.class, TransferEntity.class);
    }

    public void saveTransfer(TransferModel transferModel) throws RegistryException {
        create(transferModel);
    }

    public List<TransferModel> getTransfersForTask(String taskId) throws RegistryException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.Transfer.TASK_ID, taskId);
        List<TransferModel> transfers = select(QueryConstants.GET_TRANSFERS_FOR_TASK_ID, -1, 0, queryParameters);
        return transfers;
    }

    public List<TransferModel> getTransfersForTransferId(String transferId) throws RegistryException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.Transfer.TRANSFER_ID, transferId);
        List<TransferModel> transfers = select(QueryConstants.GET_TRANSFERS_FOR_TRANSFER_ID, -1, 0, queryParameters);
        return transfers;
    }

}
