/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
 */

package repository_test

import (
	"context"
	"testing"

	"github.com/apache/airavata/internal/ptr"

	model "github.com/apache/airavata/api/process/model"
	repository "github.com/apache/airavata/api/process/repository"
)

func TestAStoppedOrCompletedProcessTakesNoFurtherStatus(t *testing.T) {
	for ended, late := range map[model.ProcessStatusType]model.ProcessStatusType{
		model.ProcessStatusTypeStopped:   model.ProcessStatusTypeCompleted,
		model.ProcessStatusTypeCompleted: model.ProcessStatusTypeStopped,
	} {
		gdb := newDB(t)
		proc, _ := newBatchProcess(t, gdb)
		statuses := repository.NewStatusRepository(gdb)
		for i, status := range []model.ProcessStatusType{model.ProcessStatusTypeRunning, ended, late} {
			if err := statuses.Create(context.Background(), &model.ProcessStatus{ProcessID: &proc.ID, Status: &status, Timestamp: ptr.To(int64(i))}); err != nil {
				t.Fatalf("create %s: %v", status, err)
			}
		}
		got, err := statuses.FindByProcessID(context.Background(), proc.ID)
		if err != nil {
			t.Fatal(err)
		}
		if len(got) != 2 || *got[1].Status != ended {
			t.Errorf("after %s then %s: %d statuses, want RUNNING then %s only", ended, late, len(got), ended)
		}
	}
}
