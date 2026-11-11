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

// Package execution holds what the workflows and the tasks they run share: the context
// handed from one task to the next, and the lookups every task starts from.
package execution

import (
	"context"
	"fmt"

	model "github.com/apache/airavata/api/process/model"
	processrepo "github.com/apache/airavata/api/process/repository"
	"github.com/apache/airavata/internal/config"
)

type GlobalJobConfigs struct {
	MailUser string
}

// Context is handed from one task of a workflow to the next. It crosses the workflow
// backend between tasks, so everything in it must survive a round trip through JSON.
type Context struct {
	GlobalJobConfigs *GlobalJobConfigs      `json:"globalJobConfigs,omitempty"`
	Data             map[string]interface{} `json:"data,omitempty"`
	SomeData         string                 `json:"someData,omitempty"`
}

// set records a value under key for the rest of the run to read.
//
// The map is created on demand rather than assumed: an activity is handed a context
// rebuilt from what crossed the workflow backend, so one that writes cannot count on
// finding the map the workflow started with.
func (c *Context) set(key string, value interface{}) {
	if c.Data == nil {
		c.Data = make(map[string]interface{})
	}
	c.Data[key] = value
}

// LoadGlobalJobConfigs reads the settings every job of the platform is submitted with.
func LoadGlobalJobConfigs() (*GlobalJobConfigs, error) {
	cfg, err := config.FetchSystemConfigs()
	if err != nil {
		return nil, err
	}
	return &GlobalJobConfigs{
		MailUser: cfg.EmailMonitorAddress,
	}, nil
}

func Stopped(ctx context.Context, processes *processrepo.ProcessRepository, processID string) bool {
	stopped, err := processes.HasStatus(context.WithoutCancel(ctx), processID, model.ProcessStatusTypeStopped)
	return err == nil && stopped
}

// FindBatchProcess returns a process that carries a batch section, which is the only
// kind a batch task can act on.
func FindBatchProcess(ctx context.Context, processes *processrepo.ProcessRepository, processID string) (*model.Process, error) {
	process, err := processes.FindByID(ctx, processID)
	if err != nil {
		return nil, err
	}
	if process.BatchProcess == nil {
		return nil, fmt.Errorf("process %s carries no batch process section", processID)
	}
	return process, nil
}
