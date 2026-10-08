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

	"gorm.io/gorm"

	"github.com/apache/airavata/api/compute/repository"
	iamrepo "github.com/apache/airavata/api/iam/repository"
	sharingmodel "github.com/apache/airavata/api/sharing/model"
	sharingrepo "github.com/apache/airavata/api/sharing/repository"
	sharingsvc "github.com/apache/airavata/api/sharing/service"
)

// configGuard shares Slurm cluster configs.
//
// A config names who may submit jobs as a particular account on a particular machine,
// which is why control over its share list stays with the owner: a grantee may use the
// account, not hand it on.
type configGuard struct{ configAccess }

func (g configGuard) ResourceType() sharingmodel.ResourceType {
	return sharingmodel.ResourceTypeSlurmClusterConfig
}

func (g configGuard) Label() string { return "Slurm cluster config" }

func (g configGuard) RequireControlled(ctx context.Context, id string) ([]string, error) {
	config, err := g.requireConfig(ctx, id)
	if err != nil {
		return nil, err
	}
	if err := g.requireControl(ctx, config); err != nil {
		return nil, err
	}
	if config.OwnerID == nil {
		return nil, nil
	}
	return []string{*config.OwnerID}, nil
}

// NewSlurmClusterConfigSharingService returns the sharing service for cluster configs.
func NewSlurmClusterConfigSharingService(
	db *gorm.DB,
	configs *repository.SlurmClusterConfigRepository,
	sharing *sharingrepo.Repository,
	groups *iamrepo.GroupRepository,
	users *iamrepo.UserRepository,
	members *iamrepo.GroupMemberRepository,
) *sharingsvc.Service {
	guard := configGuard{configAccess{
		access:  sharingsvc.NewAccess(members),
		configs: configs,
		sharing: sharing,
	}}
	return sharingsvc.NewService(guard, db, sharing, groups, users)
}
