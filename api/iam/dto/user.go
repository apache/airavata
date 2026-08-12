package dto

import (
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/iam/model"
)

// UserRegistration is the create/update payload for a user.
//
// Java: org.apache.airavata.iam.dto.UserRegistrationDto
type UserRegistration struct {
	UserID     string            `json:"userId"`
	Email      *string           `json:"email"`
	FirstName  *string           `json:"firstName"`
	LastName   *string           `json:"lastName"`
	AuthMethod *model.AuthMethod `json:"authMethod"`
}

// Validate implements httpx.Validator.
func (r *UserRegistration) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("userId", "User ID cannot be blank", r.UserID)
	c.Email("email", "Email should be valid", r.Email)
	c.NotBlankPtr("firstName", "First name cannot be blank", r.FirstName)
	c.NotBlankPtr("lastName", "Last name cannot be blank", r.LastName)
	return c.Fields()
}

// UserResponse is the read model for a user.
//
// It deliberately omits email, auth method and roles — the Java response DTO exposes
// only these five fields, and widening it here would leak more about an account than
// the API ever has.
//
// Java: org.apache.airavata.iam.dto.UserResponseDto
type UserResponse struct {
	UserID    string            `json:"userId"`
	Status    *model.UserStatus `json:"status"`
	FirstName *string           `json:"firstName"`
	LastName  *string           `json:"lastName"`
	CreatedAt int64             `json:"createdAt"`
}

func ToUserResponse(u *model.User) UserResponse {
	return UserResponse{
		UserID:    u.ID,
		Status:    u.Status,
		FirstName: u.FirstName,
		LastName:  u.LastName,
		CreatedAt: u.CreatedAt,
	}
}
