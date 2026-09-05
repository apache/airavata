package dto

import (
	"strconv"

	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/compute/model"
)

// SlurmClusterRequest is the create/update payload for a cluster.
//
// It describes the machine only. The account a job submits under, the key it presents
// and the directory it works beneath are a SlurmClusterConfig, so registering a
// cluster never puts anyone's credentials into the catalogue.
type SlurmClusterRequest struct {
	ClusterName        string  `json:"clusterName"`
	ClusterDescription *string `json:"clusterDescription"`

	HeadnodeHost string `json:"headnodeHost"`
	HeadnodePort int    `json:"headnodePort"`

	// DataHost and DataPort are the optional separate endpoint for data movement. A
	// cluster that names neither stages through its head node.
	DataHost *string `json:"dataHost"`
	DataPort *int    `json:"dataPort"`

	// Partitions are the partitions to carve the cluster into as it is registered, so
	// a cluster whose layout is already known arrives complete rather than needing a
	// follow-up call per partition.
	//
	// Accepted on create only. An update that carries them is rejected rather than
	// obeyed: the set has no ids in it, so there would be nothing to match an incoming
	// partition to an existing row by, and replacing the collection wholesale would
	// delete partitions the caller never mentioned. Adding, changing and removing them
	// afterwards is what /api/v1/slurm-clusters/{slurmClusterId}/partitions is for.
	Partitions []ClusterPartitionRequest `json:"partitions"`
}

// Validate implements httpx.Validator.
func (r *SlurmClusterRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("clusterName", "Cluster name cannot be blank", r.ClusterName)
	c.NotBlank("headnodeHost", "Headnode host cannot be blank", r.HeadnodeHost)
	if r.HeadnodePort <= 0 || r.HeadnodePort > 65535 {
		c.Add("headnodePort", "Headnode port must be between 1 and 65535")
	}
	// The data endpoint is optional as a whole, but half of one is a configuration
	// error rather than a default: a host with no port has nothing to connect to.
	if r.DataPort != nil && (*r.DataPort <= 0 || *r.DataPort > 65535) {
		c.Add("dataPort", "Data port must be between 1 and 65535")
	}
	for i := range r.Partitions {
		c.Nested(indexed("partitions", i), &r.Partitions[i])
	}
	return c.Fields()
}

func indexed(field string, i int) string {
	return field + "[" + strconv.Itoa(i) + "]"
}

// SlurmClusterResponse is the read model for a cluster.
type SlurmClusterResponse struct {
	SlurmClusterID     string  `json:"slurmClusterId"`
	ClusterName        string  `json:"clusterName"`
	ClusterDescription *string `json:"clusterDescription"`

	HeadnodeHost string `json:"headnodeHost"`
	HeadnodePort int    `json:"headnodePort"`

	DataHost *string `json:"dataHost"`
	DataPort *int    `json:"dataPort"`

	Partitions []ClusterPartitionResponse `json:"partitions"`
}

// ApplySlurmClusterRequest copies the mutable fields of a request onto an entity. The
// id and the partitions are never written from a request: partitions are managed
// through their own endpoints, and overwriting the collection here would risk removing
// rows the caller never mentioned.
//
// A create request's inline partitions are written by the service through the
// partition repository, one row at a time, for that same reason — this function stays
// out of the collection on every path.
func ApplySlurmClusterRequest(dst *model.SlurmCluster, src *SlurmClusterRequest) {
	dst.ClusterName = src.ClusterName
	dst.ClusterDescription = src.ClusterDescription
	dst.HeadnodeHost = src.HeadnodeHost
	dst.HeadnodePort = src.HeadnodePort
	dst.DataHost = src.DataHost
	dst.DataPort = src.DataPort
}

func ToSlurmClusterResponse(c *model.SlurmCluster) SlurmClusterResponse {
	partitions := make([]ClusterPartitionResponse, 0, len(c.Partitions))
	for i := range c.Partitions {
		partitions = append(partitions, ToClusterPartitionResponse(&c.Partitions[i]))
	}
	return SlurmClusterResponse{
		SlurmClusterID:     c.ID,
		ClusterName:        c.ClusterName,
		ClusterDescription: c.ClusterDescription,
		HeadnodeHost:       c.HeadnodeHost,
		HeadnodePort:       c.HeadnodePort,
		DataHost:           c.DataHost,
		DataPort:           c.DataPort,
		Partitions:         partitions,
	}
}

func ToSlurmClusterResponses(in []model.SlurmCluster) []SlurmClusterResponse {
	out := make([]SlurmClusterResponse, 0, len(in))
	for i := range in {
		out = append(out, ToSlurmClusterResponse(&in[i]))
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
