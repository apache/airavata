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

package toolkit

import (
	"context"
	"fmt"
	"strings"

	"github.com/apache/airavata/internal/mcp/client"
)

// URIs of the airavata:// resources tools link to.
func ClusterURI(id string) string     { return "airavata://clusters/" + id }
func ApplicationURI(id string) string { return "airavata://applications/" + id }
func ProcessURI(id string) string     { return "airavata://processes/" + id }
func DataProductURI(id string) string { return "airavata://data-products/" + id }

func Str(p *string) string {
	if p == nil {
		return ""
	}
	return *p
}

func Contains(haystack, needle string) bool {
	return needle == "" || strings.Contains(strings.ToLower(haystack), strings.ToLower(needle))
}

func (d *Deps) Clusters(ctx context.Context) ([]client.SlurmClusterResponse, error) {
	r, err := client.JSON[[]client.SlurmClusterResponse](d.API.ListSlurmClustersWithResponse(ctx))
	if err != nil {
		return nil, err
	}
	return *r, nil
}

func (d *Deps) Cluster(ctx context.Context, id string) (*client.SlurmClusterResponse, error) {
	uid, err := client.ParseUUID("cluster_id", id)
	if err != nil {
		return nil, err
	}
	return client.JSON[client.SlurmClusterResponse](d.API.GetSlurmClusterWithResponse(ctx, uid))
}

// Partition finds a cluster's partition by name.
func Partition(c *client.SlurmClusterResponse, name string) *client.ClusterPartitionResponse {
	for i := range c.Partitions {
		if c.Partitions[i].Name == name {
			return &c.Partitions[i]
		}
	}
	return nil
}

// ClusterConfig is a login config with whether the caller owns it.
type ClusterConfig struct {
	client.SlurmClusterConfigResponse
	Owned bool
}

func (d *Deps) ClusterConfigs(ctx context.Context) ([]ClusterConfig, error) {
	mine, err := client.JSON[[]client.SlurmClusterConfigResponse](d.API.ListMySlurmClusterConfigsWithResponse(ctx))
	if err != nil {
		return nil, err
	}
	shared, err := client.JSON[[]client.SlurmClusterConfigResponse](d.API.ListSlurmClusterConfigsSharedWithMeWithResponse(ctx))
	if err != nil {
		return nil, err
	}
	var out []ClusterConfig
	for _, c := range *mine {
		out = append(out, ClusterConfig{c, true})
	}
	for _, c := range *shared {
		out = append(out, ClusterConfig{c, false})
	}
	return out, nil
}

func (d *Deps) ClusterConfig(ctx context.Context, id string) (*client.SlurmClusterConfigResponse, error) {
	uid, err := client.ParseUUID("cluster_config_id", id)
	if err != nil {
		return nil, err
	}
	return client.JSON[client.SlurmClusterConfigResponse](d.API.GetSlurmClusterConfigWithResponse(ctx, uid))
}

func (d *Deps) Templates(ctx context.Context) ([]client.TemplateResponse, error) {
	r, err := client.JSON[[]client.TemplateResponse](d.API.ListApplicationTemplatesWithResponse(ctx))
	if err != nil {
		return nil, err
	}
	return *r, nil
}

func (d *Deps) Template(ctx context.Context, id string) (*client.TemplateResponse, error) {
	uid, err := client.ParseUUID("template_id", id)
	if err != nil {
		return nil, err
	}
	return client.JSON[client.TemplateResponse](d.API.GetApplicationTemplateWithResponse(ctx, uid))
}

// Deployments lists deployments, of one template when templateID is set.
func (d *Deps) Deployments(ctx context.Context, templateID string) ([]client.BatchDeploymentResponse, error) {
	var params *client.ListBatchDeploymentsParams
	if templateID != "" {
		params = &client.ListBatchDeploymentsParams{TemplateId: &templateID}
	}
	r, err := client.JSON[[]client.BatchDeploymentResponse](d.API.ListBatchDeploymentsWithResponse(ctx, params))
	if err != nil {
		return nil, err
	}
	return *r, nil
}

func (d *Deps) Deployment(ctx context.Context, id string) (*client.BatchDeploymentResponse, error) {
	uid, err := client.ParseUUID("deployment_id", id)
	if err != nil {
		return nil, err
	}
	return client.JSON[client.BatchDeploymentResponse](d.API.GetBatchDeploymentWithResponse(ctx, uid))
}

// DataProduct is a data product with whether the caller owns it.
type DataProduct struct {
	client.DataProductResponse
	Owned bool
}

func (d *Deps) DataProducts(ctx context.Context) ([]DataProduct, error) {
	mine, err := client.JSON[[]client.DataProductResponse](d.API.ListMyDataProductsWithResponse(ctx))
	if err != nil {
		return nil, err
	}
	shared, err := client.JSON[[]client.DataProductResponse](d.API.ListDataProductsSharedWithMeWithResponse(ctx))
	if err != nil {
		return nil, err
	}
	var out []DataProduct
	for _, p := range *mine {
		out = append(out, DataProduct{p, true})
	}
	for _, p := range *shared {
		out = append(out, DataProduct{p, false})
	}
	return out, nil
}

func (d *Deps) DataProduct(ctx context.Context, id string) (*client.DataProductResponse, error) {
	uid, err := client.ParseUUID("data_product_id", id)
	if err != nil {
		return nil, err
	}
	return client.JSON[client.DataProductResponse](d.API.GetDataProductWithResponse(ctx, uid))
}

// Storage is an SCP data storage with whether the caller owns it.
type Storage struct {
	client.SCPDataStorageResponse
	Owned bool
}

func (d *Deps) Storages(ctx context.Context) ([]Storage, error) {
	mine, err := client.JSON[[]client.SCPDataStorageResponse](d.API.ListMySCPDataStoragesWithResponse(ctx))
	if err != nil {
		return nil, err
	}
	shared, err := client.JSON[[]client.SCPDataStorageResponse](d.API.ListSCPDataStoragesSharedWithMeWithResponse(ctx))
	if err != nil {
		return nil, err
	}
	var out []Storage
	for _, s := range *mine {
		out = append(out, Storage{s, true})
	}
	for _, s := range *shared {
		out = append(out, Storage{s, false})
	}
	return out, nil
}

func (d *Deps) Process(ctx context.Context, id string) (*client.ProcessResponse, error) {
	uid, err := client.ParseUUID("process_id", id)
	if err != nil {
		return nil, err
	}
	return client.JSON[client.ProcessResponse](d.API.GetProcessWithResponse(ctx, uid))
}

// ClusterNames maps cluster ids to names for display.
func (d *Deps) ClusterNames(ctx context.Context) (map[string]string, error) {
	clusters, err := d.Clusters(ctx)
	if err != nil {
		return nil, err
	}
	names := make(map[string]string, len(clusters))
	for _, c := range clusters {
		names[c.SlurmClusterId.String()] = c.ClusterName
	}
	return names, nil
}

// PartitionSummary is a partition's limits in tool-output form.
type PartitionSummary struct {
	Name            string  `json:"name"`
	PartitionID     string  `json:"partition_id"`
	Description     string  `json:"description,omitempty"`
	MaxRunTimeMin   *int32  `json:"max_run_time_minutes,omitempty"`
	MaxNodes        *int32  `json:"max_nodes,omitempty"`
	MaxProcessors   *int32  `json:"max_processors,omitempty"`
	CPUsPerNode     *int32  `json:"cpus_per_node,omitempty"`
	MaxMemoryMB     *int64  `json:"max_memory_mb,omitempty"`
	Gres            *string `json:"gres,omitempty"`
	DefaultWalltime *int64  `json:"default_walltime_minutes,omitempty"`
	IsDefault       bool    `json:"is_default,omitempty"`
}

func SummarizePartition(p client.ClusterPartitionResponse) PartitionSummary {
	return PartitionSummary{Name: p.Name, PartitionID: p.PartitionId.String(), Description: Str(p.Description),
		MaxRunTimeMin: p.MaxRunTime, MaxNodes: p.MaxNodes, MaxProcessors: p.MaxProcessors, CPUsPerNode: p.CpuPerNode,
		MaxMemoryMB: p.MaxMemory, Gres: p.Gres, DefaultWalltime: p.DefaultWalltime,
		IsDefault: p.IsDefaultQueue != nil && *p.IsDefaultQueue}
}

// NotFound turns a 404 into an error the model can act on.
func NotFound(err error, what, id string) error {
	if client.IsStatus(err, 404) {
		return fmt.Errorf("%s %s was not found, or you cannot see it; take ids from the list tools", what, id)
	}
	return err
}
