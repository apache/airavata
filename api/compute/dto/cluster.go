package dto

import (
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/compute/model"
	creddto "github.com/apache/airavata/api/credentials/dto"
)

// ClusterRequest is the create/update payload for a cluster.
//
// The host name it used to carry is now an SSH endpoint of its own, named by id. The
// field is required for the same reason the host name was: a cluster nothing can log
// in to cannot be submitted to.
//
// Java: org.apache.airavata.compute.dto.ClusterRequestDto
type ClusterRequest struct {
	ClusterName        string  `json:"clusterName"`
	ClusterDescription *string `json:"clusterDescription"`
	SSHEndpointID      string  `json:"sshEndpointId"`
	SlurmHome          string  `json:"slurmHome"`
}

// Validate implements httpx.Validator.
func (r *ClusterRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("clusterName", "Cluster name cannot be blank", r.ClusterName)
	c.NotBlank("sshEndpointId", "SSH endpoint id cannot be blank", r.SSHEndpointID)
	c.NotBlank("slurmHome", "Slurm home cannot be blank", r.SlurmHome)
	return c.Fields()
}

// ClusterResponse is the read model for a cluster.
//
// The endpoint is inlined rather than left as a bare id: every caller that wants a
// cluster wants the host it lives on, and it is a small, non-secret record.
//
// Java: org.apache.airavata.compute.dto.ClusterResponseDto
type ClusterResponse struct {
	ClusterID          string                       `json:"clusterId"`
	ClusterName        string                       `json:"clusterName"`
	ClusterDescription *string                      `json:"clusterDescription"`
	SSHEndpointID      *string                      `json:"sshEndpointId"`
	SSHEndpoint        *creddto.SSHEndpointResponse `json:"sshEndpoint"`
	SlurmHome          string                       `json:"slurmHome"`
	Partitions         []ClusterPartitionResponse   `json:"partitions"`
}

// ApplyClusterRequest copies the mutable fields of a request onto an entity. The id
// and the partitions are never written from a request: partitions are managed through
// their own endpoints, and overwriting the collection here would risk removing rows
// the caller never mentioned. The SSH endpoint is resolved by the service, which is
// what turns an unknown id into a 404 rather than a dangling reference.
func ApplyClusterRequest(dst *model.Cluster, src *ClusterRequest) {
	dst.ClusterName = src.ClusterName
	dst.ClusterDescription = src.ClusterDescription
	dst.SlurmHome = src.SlurmHome
}

func ToClusterResponse(c *model.Cluster) ClusterResponse {
	partitions := make([]ClusterPartitionResponse, 0, len(c.Partitions))
	for i := range c.Partitions {
		partitions = append(partitions, ToClusterPartitionResponse(&c.Partitions[i]))
	}
	out := ClusterResponse{
		ClusterID:          c.ID,
		ClusterName:        c.ClusterName,
		ClusterDescription: c.ClusterDescription,
		SSHEndpointID:      c.SSHEndpointID,
		SlurmHome:          c.SlurmHome,
		Partitions:         partitions,
	}
	if c.SSHEndpoint != nil {
		endpoint := creddto.ToSSHEndpointResponse(c.SSHEndpoint)
		out.SSHEndpoint = &endpoint
	}
	return out
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
