package dto

import (
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/compute/model"
)

// ClusterRequest is the create/update payload for a cluster.
//
// Java: org.apache.airavata.compute.dto.ClusterRequestDto
type ClusterRequest struct {
	ClusterName        string  `json:"clusterName"`
	ClusterDescription *string `json:"clusterDescription"`
	HostName           string  `json:"hostName"`
	SlurmHome          string  `json:"slurmHome"`
}

// Validate implements httpx.Validator.
func (r *ClusterRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("clusterName", "Cluster name cannot be blank", r.ClusterName)
	c.NotBlank("hostName", "Host name cannot be blank", r.HostName)
	c.NotBlank("slurmHome", "Slurm home cannot be blank", r.SlurmHome)
	return c.Fields()
}

// ClusterResponse is the read model for a cluster.
//
// Java: org.apache.airavata.compute.dto.ClusterResponseDto
type ClusterResponse struct {
	ClusterID          string                     `json:"clusterId"`
	ClusterName        string                     `json:"clusterName"`
	ClusterDescription *string                    `json:"clusterDescription"`
	HostName           string                     `json:"hostName"`
	SlurmHome          string                     `json:"slurmHome"`
	Partitions         []ClusterPartitionResponse `json:"partitions"`
}

// applyClusterRequest copies the mutable fields of a request onto an entity. The id
// and the partitions are never written from a request: partitions are managed through
// their own endpoints, and overwriting the collection here would risk removing rows
// the caller never mentioned.
func ApplyClusterRequest(dst *model.Cluster, src *ClusterRequest) {
	dst.ClusterName = src.ClusterName
	dst.ClusterDescription = src.ClusterDescription
	dst.HostName = src.HostName
	dst.SlurmHome = src.SlurmHome
}

func ToClusterResponse(c *model.Cluster) ClusterResponse {
	partitions := make([]ClusterPartitionResponse, 0, len(c.Partitions))
	for i := range c.Partitions {
		partitions = append(partitions, ToClusterPartitionResponse(&c.Partitions[i]))
	}
	return ClusterResponse{
		ClusterID:          c.ID,
		ClusterName:        c.ClusterName,
		ClusterDescription: c.ClusterDescription,
		HostName:           c.HostName,
		SlurmHome:          c.SlurmHome,
		Partitions:         partitions,
	}
}

// ClusterPartitionRequest is the create/update payload for a partition.
//
// Java: org.apache.airavata.compute.dto.ClusterPartitionRequestDto
type ClusterPartitionRequest struct {
	Name             string  `json:"name"`
	Description      *string `json:"description"`
	MaxRunTime       *int32  `json:"maxRunTime"`
	MaxNodes         *int32  `json:"maxNodes"`
	MaxProcessors    *int32  `json:"maxProcessors"`
	MaxJobsInQueue   *int32  `json:"maxJobsInQueue"`
	MaxMemory        *int64  `json:"maxMemory"`
	CPUPerNode       *int32  `json:"cpuPerNode"`
	DefaultNodeCount *int32  `json:"defaultNodeCount"`
	DefaultCPUCount  *int32  `json:"defaultCpuCount"`
	DefaultWalltime  *int64  `json:"defaultWalltime"`
	Gres             *string `json:"gres"`
	Nodes            *string `json:"nodes"`
	IsDefaultQueue   *bool   `json:"isDefaultQueue"`
	IsCheckpointable *bool   `json:"isCheckpointable"`
}

// Validate implements httpx.Validator.
func (r *ClusterPartitionRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("name", "Partition name cannot be blank", r.Name)
	return c.Fields()
}

// ClusterPartitionResponse is the read model for a partition.
//
// Java: org.apache.airavata.compute.dto.ClusterPartitionResponseDto
type ClusterPartitionResponse struct {
	PartitionID      string  `json:"partitionId"`
	ClusterID        *string `json:"clusterId"`
	Name             string  `json:"name"`
	Description      *string `json:"description"`
	MaxRunTime       *int32  `json:"maxRunTime"`
	MaxNodes         *int32  `json:"maxNodes"`
	MaxProcessors    *int32  `json:"maxProcessors"`
	MaxJobsInQueue   *int32  `json:"maxJobsInQueue"`
	MaxMemory        *int64  `json:"maxMemory"`
	CPUPerNode       *int32  `json:"cpuPerNode"`
	DefaultNodeCount *int32  `json:"defaultNodeCount"`
	DefaultCPUCount  *int32  `json:"defaultCpuCount"`
	DefaultWalltime  *int64  `json:"defaultWalltime"`
	Gres             *string `json:"gres"`
	Nodes            *string `json:"nodes"`
	IsDefaultQueue   *bool   `json:"isDefaultQueue"`
	IsCheckpointable *bool   `json:"isCheckpointable"`
}

func ApplyClusterPartitionRequest(dst *model.ClusterPartition, src *ClusterPartitionRequest) {
	dst.Name = src.Name
	dst.Description = src.Description
	dst.MaxRunTime = src.MaxRunTime
	dst.MaxNodes = src.MaxNodes
	dst.MaxProcessors = src.MaxProcessors
	dst.MaxJobsInQueue = src.MaxJobsInQueue
	dst.MaxMemory = src.MaxMemory
	dst.CPUPerNode = src.CPUPerNode
	dst.DefaultNodeCount = src.DefaultNodeCount
	dst.DefaultCPUCount = src.DefaultCPUCount
	dst.DefaultWalltime = src.DefaultWalltime
	dst.Gres = src.Gres
	dst.Nodes = src.Nodes
	dst.IsDefaultQueue = src.IsDefaultQueue
	dst.IsCheckpointable = src.IsCheckpointable
}

func ToClusterPartitionResponse(p *model.ClusterPartition) ClusterPartitionResponse {
	return ClusterPartitionResponse{
		PartitionID:      p.ID,
		ClusterID:        p.ClusterID,
		Name:             p.Name,
		Description:      p.Description,
		MaxRunTime:       p.MaxRunTime,
		MaxNodes:         p.MaxNodes,
		MaxProcessors:    p.MaxProcessors,
		MaxJobsInQueue:   p.MaxJobsInQueue,
		MaxMemory:        p.MaxMemory,
		CPUPerNode:       p.CPUPerNode,
		DefaultNodeCount: p.DefaultNodeCount,
		DefaultCPUCount:  p.DefaultCPUCount,
		DefaultWalltime:  p.DefaultWalltime,
		Gres:             p.Gres,
		Nodes:            p.Nodes,
		IsDefaultQueue:   p.IsDefaultQueue,
		IsCheckpointable: p.IsCheckpointable,
	}
}

// ClusterCredentialRequest is the create/update payload for a binding.
//
// There is no owner field, and that is the point: the owning user comes from the
// access token, so a caller cannot create a binding on someone else's behalf.
//
// Java: org.apache.airavata.compute.dto.ClusterCredentialRequestDto
type ClusterCredentialRequest struct {
	ClusterID       string `json:"clusterId"`
	SSHCredentialID string `json:"sshCredentialId"`
}

// Validate implements httpx.Validator.
func (r *ClusterCredentialRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("clusterId", "Cluster id cannot be blank", r.ClusterID)
	c.NotBlank("sshCredentialId", "SSH credential id cannot be blank", r.SSHCredentialID)
	return c.Fields()
}

// ClusterCredentialResponse is the read model for a binding.
//
// Java: org.apache.airavata.compute.dto.ClusterCredentialResponseDto
type ClusterCredentialResponse struct {
	ClusterCredentialID string  `json:"clusterCredentialId"`
	ClusterID           *string `json:"clusterId"`
	SSHCredentialID     *string `json:"sshCredentialId"`
	UserID              *string `json:"userId"`
}

func ToClusterCredentialResponse(c *model.ClusterCredential) ClusterCredentialResponse {
	return ClusterCredentialResponse{
		ClusterCredentialID: c.ID,
		ClusterID:           c.ClusterID,
		SSHCredentialID:     c.SSHCredentialID,
		UserID:              c.OwnerID,
	}
}

func ToClusterCredentialResponses(in []model.ClusterCredential) []ClusterCredentialResponse {
	out := make([]ClusterCredentialResponse, 0, len(in))
	for i := range in {
		out = append(out, ToClusterCredentialResponse(&in[i]))
	}
	return out
}
