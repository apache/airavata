package domain

// Request DTOs for all use cases

// Resource management requests

// CreateComputeResourceRequest represents a request to create a compute resource
type CreateComputeResourceRequest struct {
	Name         string                 `json:"name" validate:"required"`
	Type         ComputeResourceType    `json:"type" validate:"required"`
	Endpoint     string                 `json:"endpoint" validate:"required"`
	OwnerID      string                 `json:"ownerId" validate:"required"`
	CostPerHour  float64                `json:"costPerHour" validate:"min=0"`
	MaxWorkers   int                    `json:"maxWorkers" validate:"min=1"`
	Capabilities map[string]interface{} `json:"capabilities,omitempty"`
	Metadata     map[string]interface{} `json:"metadata,omitempty"`
}

// CreateStorageResourceRequest represents a request to create a storage resource
type CreateStorageResourceRequest struct {
	Name          string                 `json:"name" validate:"required"`
	Type          StorageResourceType    `json:"type" validate:"required"`
	Endpoint      string                 `json:"endpoint" validate:"required"`
	OwnerID       string                 `json:"ownerId" validate:"required"`
	TotalCapacity *int64                 `json:"totalCapacity,omitempty" validate:"omitempty,min=0"`
	Region        string                 `json:"region,omitempty"`
	Zone          string                 `json:"zone,omitempty"`
	Metadata      map[string]interface{} `json:"metadata,omitempty"`
}

// ListResourcesRequest represents a request to list resources
type ListResourcesRequest struct {
	Type   string `json:"type,omitempty"`   // compute, storage, or empty for all
	Status string `json:"status,omitempty"` // active, inactive, error, or empty for all
	Limit  int    `json:"limit,omitempty" validate:"min=1,max=1000"`
	Offset int    `json:"offset,omitempty" validate:"min=0"`
}

// GetResourceRequest represents a request to get a resource
type GetResourceRequest struct {
	ResourceID string `json:"resourceId" validate:"required"`
}

// UpdateResourceRequest represents a request to update a resource
type UpdateResourceRequest struct {
	ResourceID string                 `json:"resourceId" validate:"required"`
	Status     *ResourceStatus        `json:"status,omitempty"`
	Metadata   map[string]interface{} `json:"metadata,omitempty"`
}

// DeleteResourceRequest represents a request to delete a resource
type DeleteResourceRequest struct {
	ResourceID string `json:"resourceId" validate:"required"`
	Force      bool   `json:"force,omitempty"`
}

// Experiment management requests

// CreateExperimentRequest represents a request to create an experiment
type CreateExperimentRequest struct {
	Name            string                 `json:"name" validate:"required"`
	Description     string                 `json:"description,omitempty"`
	ProjectID       string                 `json:"projectId" validate:"required"`
	CommandTemplate string                 `json:"commandTemplate" validate:"required"`
	OutputPattern   string                 `json:"outputPattern,omitempty"`
	Parameters      []ParameterSet         `json:"parameters" validate:"required"`
	Requirements    *ResourceRequirements  `json:"requirements,omitempty"`
	Constraints     *ExperimentConstraints `json:"constraints,omitempty"`
	Metadata        map[string]interface{} `json:"metadata,omitempty"`
}

// GetExperimentRequest represents a request to get an experiment
type GetExperimentRequest struct {
	ExperimentID string `json:"experimentId" validate:"required"`
	IncludeTasks bool   `json:"includeTasks,omitempty"`
}

// ListExperimentsRequest represents a request to list experiments
type ListExperimentsRequest struct {
	ProjectID string `json:"projectId,omitempty"`
	OwnerID   string `json:"ownerId,omitempty"`
	Status    string `json:"status,omitempty"`
	Limit     int    `json:"limit,omitempty" validate:"min=1,max=1000"`
	Offset    int    `json:"offset,omitempty" validate:"min=0"`
}

// UpdateExperimentRequest represents a request to update an experiment
type UpdateExperimentRequest struct {
	ExperimentID string                 `json:"experimentId" validate:"required"`
	Description  *string                `json:"description,omitempty"`
	Constraints  *ExperimentConstraints `json:"constraints,omitempty"`
	Metadata     map[string]interface{} `json:"metadata,omitempty"`
}

// DeleteExperimentRequest represents a request to delete an experiment
type DeleteExperimentRequest struct {
	ExperimentID string `json:"experimentId" validate:"required"`
	Force        bool   `json:"force,omitempty"`
}

// SubmitExperimentRequest represents a request to submit an experiment for execution
type SubmitExperimentRequest struct {
	ExperimentID string `json:"experimentId" validate:"required"`
	Priority     int    `json:"priority,omitempty" validate:"min=1,max=10"`
	DryRun       bool   `json:"dryRun,omitempty"`
}

// Response DTOs for all use cases

// Resource management responses

// CreateComputeResourceResponse represents the response to creating a compute resource
type CreateComputeResourceResponse struct {
	Resource *ComputeResource `json:"resource"`
	Success  bool             `json:"success"`
	Message  string           `json:"message,omitempty"`
}

// CreateStorageResourceResponse represents the response to creating a storage resource
type CreateStorageResourceResponse struct {
	Resource *StorageResource `json:"resource"`
	Success  bool             `json:"success"`
	Message  string           `json:"message,omitempty"`
}

// ListResourcesResponse represents the response to listing resources
type ListResourcesResponse struct {
	Resources []interface{} `json:"resources"` // Can be ComputeResource or StorageResource
	Total     int           `json:"total"`
	Limit     int           `json:"limit"`
	Offset    int           `json:"offset"`
}

// GetResourceResponse represents the response to getting a resource
type GetResourceResponse struct {
	Resource interface{} `json:"resource"` // Can be ComputeResource or StorageResource
	Success  bool        `json:"success"`
	Message  string      `json:"message,omitempty"`
}

// UpdateResourceResponse represents the response to updating a resource
type UpdateResourceResponse struct {
	Resource interface{} `json:"resource"` // Can be ComputeResource or StorageResource
	Success  bool        `json:"success"`
	Message  string      `json:"message,omitempty"`
}

// DeleteResourceResponse represents the response to deleting a resource
type DeleteResourceResponse struct {
	Success bool   `json:"success"`
	Message string `json:"message,omitempty"`
}

// Experiment management responses

// CreateExperimentResponse represents the response to creating an experiment
type CreateExperimentResponse struct {
	Experiment *Experiment `json:"experiment"`
	Success    bool        `json:"success"`
	Message    string      `json:"message,omitempty"`
}

// GetExperimentResponse represents the response to getting an experiment
type GetExperimentResponse struct {
	Experiment *Experiment `json:"experiment"`
	Tasks      []*Task     `json:"tasks,omitempty"`
	Success    bool        `json:"success"`
	Message    string      `json:"message,omitempty"`
}

// ListExperimentsResponse represents the response to listing experiments
type ListExperimentsResponse struct {
	Experiments []*Experiment `json:"experiments"`
	Total       int           `json:"total"`
	Limit       int           `json:"limit"`
	Offset      int           `json:"offset"`
}

// UpdateExperimentResponse represents the response to updating an experiment
type UpdateExperimentResponse struct {
	Experiment *Experiment `json:"experiment"`
	Success    bool        `json:"success"`
	Message    string      `json:"message,omitempty"`
}

// DeleteExperimentResponse represents the response to deleting an experiment
type DeleteExperimentResponse struct {
	Success bool   `json:"success"`
	Message string `json:"message,omitempty"`
}

// SubmitExperimentResponse represents the response to submitting an experiment
type SubmitExperimentResponse struct {
	Experiment *Experiment `json:"experiment"`
	Tasks      []*Task     `json:"tasks"`
	Success    bool        `json:"success"`
	Message    string      `json:"message,omitempty"`
}
