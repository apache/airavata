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

package workflows

import (
	"testing"

	"github.com/cschleiden/go-workflows/registry"
)

// The backend refers to a workflow or activity by name alone, so each must register,
// under the name a running instance was started with.
func TestRegister(t *testing.T) {
	w := New(nil, nil, nil, nil, nil, nil, nil, nil)
	r := registry.New()
	if err := w.Register(r); err != nil {
		t.Fatal(err)
	}
	for _, name := range []string{"HandleBatchJobSubmission", "HandleBatchJobCompletion"} {
		if _, err := r.GetWorkflow(name); err != nil {
			t.Errorf("workflow %s: %v", name, err)
		}
	}
	for _, name := range []string{"CopyData", "SubmitBatchJob", "MonitorBatchJob", "CompleteProcess"} {
		if _, err := r.GetActivity(name); err != nil {
			t.Errorf("activity %s: %v", name, err)
		}
	}
}
