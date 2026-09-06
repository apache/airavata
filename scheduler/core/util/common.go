package types

// Common utility types

// PaginationRequest represents pagination parameters
type PaginationRequest struct {
	Limit  int `json:"limit" validate:"required,min=1,max=100"`
	Offset int `json:"offset" validate:"min=0"`
}

// PaginationResponse represents pagination metadata
type PaginationResponse struct {
	Limit      int  `json:"limit"`
	Offset     int  `json:"offset"`
	TotalCount int  `json:"totalCount"`
	HasMore    bool `json:"hasMore"`
}

// ValidationResult represents validation outcome
type ValidationResult struct {
	IsValid  bool     `json:"isValid"`
	Errors   []string `json:"errors,omitempty"`
	Warnings []string `json:"warnings,omitempty"`
}
