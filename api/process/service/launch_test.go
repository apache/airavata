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

package service

import (
	"context"
	"testing"

	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"github.com/apache/airavata/internal/db"

	appdto "github.com/apache/airavata/api/application/dto"
	appmodel "github.com/apache/airavata/api/application/model"
	computedto "github.com/apache/airavata/api/compute/dto"
	datamodel "github.com/apache/airavata/api/data/model"
	"github.com/apache/airavata/api/process/dto"
	procmodel "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/api/process/repository"
)

// An output tracked in place is registered as a product at the path the job writes to,
// and like every product with a location it gets the primary replica mirroring it.
func TestTrackedInPlaceOutputHasAPrimaryReplica(t *testing.T) {
	// Foreign keys stay off: the rows a launch normally hangs off — the process, the
	// owner, the cluster config — are beside the point here.
	gdb, err := gorm.Open(sqlite.Open("file::memory:"), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		t.Fatalf("open sqlite: %v", err)
	}
	if err := db.AutoMigrate(gdb); err != nil {
		t.Fatalf("automigrate: %v", err)
	}
	s := &LaunchService{db: gdb, dataStagingTasks: repository.NewDataStagingTaskRepository(gdb)}

	fileType := appmodel.TemplateOutputTypeFile
	track := procmodel.TrackInPlace
	owner := "owner-1"
	template := &appdto.TemplateResponse{Outputs: []appdto.TemplateOutputDTO{
		{OutputID: "out-1", OutputName: "result.txt", OutputType: &fileType},
	}}
	batch := &dto.BatchProcessResponse{OutputMappings: []dto.OutputMapping{
		{TemplateOutputMappingID: "map-1", TemplateOutputID: "out-1", Value: &track},
	}}
	config := &computedto.SlurmClusterConfigResponse{SlurmClusterConfigID: "config-1"}

	if err := s.createOutputStagingTasks(context.Background(), "process-1", &owner, "/scratch/run", batch, template, config); err != nil {
		t.Fatalf("createOutputStagingTasks: %v", err)
	}

	var products []datamodel.DataProduct
	if err := gdb.Preload("Replicas").Find(&products).Error; err != nil {
		t.Fatalf("read products: %v", err)
	}
	if len(products) != 1 {
		t.Fatalf("products = %d, want the one tracked output", len(products))
	}
	p := products[0]
	if len(p.Replicas) != 1 {
		t.Fatalf("replicas = %+v, want exactly the primary", p.Replicas)
	}
	r := p.Replicas[0]
	if !r.IsPrimary || r.ID != p.ID || r.DataStorageType != datamodel.DataStorageTypeHPC ||
		r.DataStorageID != "config-1" || r.Path != "/scratch/run/result.txt" || r.CreatedAt != p.CreatedAt {
		t.Errorf("primary replica = %+v, want it to mirror product %s on HPC config-1 at /scratch/run/result.txt", r, p.ID)
	}
}
