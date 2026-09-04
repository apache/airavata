package dto

import (
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/data/model"
)

// DataProductRequest is the create/update payload for a registered dataset.
//
// There is no owner field and no provision status: ownership comes from the access
// token, and the lifecycle state is the server's to move. A caller who could set
// either would be able to register a dataset as someone else's, or claim it was
// already provisioned.
type DataProductRequest struct {
	DataName        *string                `json:"dataName"`
	DataDescription *string                `json:"dataDescription"`
	IsFile          *bool                  `json:"isFile"`
	Path            *string                `json:"path"`
	DataStorageID   string                 `json:"dataStorageId"`
	DataStorageType *model.DataStorageType `json:"dataStorageType"`
}

// Validate implements httpx.Validator.
func (r *DataProductRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlankPtr("dataName", "Data name cannot be blank", r.DataName)
	c.NotNil("isFile", "isFile cannot be null", r.IsFile)
	c.NotBlankPtr("path", "Path cannot be blank", r.Path)
	c.NotBlank("dataStorageId", "Data storage id cannot be blank", r.DataStorageID)
	if r.DataStorageType != nil && !r.DataStorageType.Valid() {
		c.Add("dataStorageType", "Data storage type must be SCP")
	}
	return c.Fields()
}

// StorageType returns the requested storage type, defaulting to SCP — the only kind
// there is so far, and the only kind dataStorageId can resolve against.
func (r *DataProductRequest) StorageType() model.DataStorageType {
	if r.DataStorageType == nil {
		return model.DataStorageTypeSCP
	}
	return *r.DataStorageType
}

// ApplyDataProductRequest copies the mutable fields of a request onto an entity. The
// owner, the provision status and the creation time are never written from a request.
func ApplyDataProductRequest(dst *model.DataProduct, src *DataProductRequest) {
	dst.DataName = src.DataName
	dst.DataDescription = src.DataDescription
	if src.IsFile != nil {
		dst.IsFile = *src.IsFile
	}
	dst.Path = src.Path
	dst.DataStorageID = &src.DataStorageID
	dst.DataStorageType = src.StorageType()
}

// DataProductResponse is the read model for a registered dataset.
//
// Permission is what the calling principal may do with it — WRITE for the owner and
// for admins, otherwise whatever the strongest share reaching them grants. It is a
// property of the request rather than of the record, which is why it is not stored.
type DataProductResponse struct {
	DataID          string                 `json:"dataId"`
	DataName        *string                `json:"dataName"`
	DataDescription *string                `json:"dataDescription"`
	IsFile          bool                   `json:"isFile"`
	Path            *string                `json:"path"`
	ProvisionStatus *model.ProvisionStatus `json:"provisionStatus"`
	OwnerID         *string                `json:"ownerId"`
	DataStorageID   *string                `json:"dataStorageId"`
	DataStorageType model.DataStorageType  `json:"dataStorageType"`
	CreatedAt       int64                  `json:"createdAt"`

	Permission *string `json:"permission,omitempty"`
}

func ToDataProductResponse(p *model.DataProduct) DataProductResponse {
	return DataProductResponse{
		DataID:          p.ID,
		DataName:        p.DataName,
		DataDescription: p.DataDescription,
		IsFile:          p.IsFile,
		Path:            p.Path,
		ProvisionStatus: p.ProvisionStatus,
		OwnerID:         p.OwnerID,
		DataStorageID:   p.DataStorageID,
		DataStorageType: p.DataStorageType,
		CreatedAt:       p.CreatedAt,
	}
}

// ToDataProductResponseWith is ToDataProductResponse with the caller's effective
// permission attached.
func ToDataProductResponseWith(p *model.DataProduct, permission string) DataProductResponse {
	out := ToDataProductResponse(p)
	out.Permission = &permission
	return out
}

func ToDataProductResponses(in []model.DataProduct) []DataProductResponse {
	out := make([]DataProductResponse, 0, len(in))
	for i := range in {
		out = append(out, ToDataProductResponse(&in[i]))
	}
	return out
}

// DataProductGroupSharingRequest shares a product with a group.
type DataProductGroupSharingRequest struct {
	GroupID    string                       `json:"groupId"`
	Permission *model.DataProductPermission `json:"permission"`
}

// Validate implements httpx.Validator.
func (r *DataProductGroupSharingRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("groupId", "Group id cannot be blank", r.GroupID)
	validateProductPermission(&c, r.Permission)
	return c.Fields()
}

// Grant returns the permission to store, defaulting to READ. Read-only is the safe
// default for a share: widening it is a deliberate act.
func (r *DataProductGroupSharingRequest) Grant() model.DataProductPermission {
	return productGrantOrRead(r.Permission)
}

// DataProductUserSharingRequest shares a product with one user.
type DataProductUserSharingRequest struct {
	UserID     string                       `json:"userId"`
	Permission *model.DataProductPermission `json:"permission"`
}

// Validate implements httpx.Validator.
func (r *DataProductUserSharingRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("userId", "User id cannot be blank", r.UserID)
	validateProductPermission(&c, r.Permission)
	return c.Fields()
}

// Grant returns the permission to store, defaulting to READ.
func (r *DataProductUserSharingRequest) Grant() model.DataProductPermission {
	return productGrantOrRead(r.Permission)
}

// DataProductSharingUpdate changes what an existing product share grants. The subject
// is fixed at creation; only the permission is editable.
type DataProductSharingUpdate struct {
	Permission *model.DataProductPermission `json:"permission"`
}

// Validate implements httpx.Validator.
func (r *DataProductSharingUpdate) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotNil("permission", "Permission cannot be null", r.Permission)
	validateProductPermission(&c, r.Permission)
	return c.Fields()
}

// DataProductGroupSharingResponse is the read model for a group share.
type DataProductGroupSharingResponse struct {
	SharingID     string                       `json:"dataProductGroupSharingId"`
	DataProductID *string                      `json:"dataProductId"`
	GroupID       *string                      `json:"groupId"`
	Permission    *model.DataProductPermission `json:"permission"`
}

func ToDataProductGroupSharingResponse(s *model.DataProductGroupSharing) DataProductGroupSharingResponse {
	return DataProductGroupSharingResponse{
		SharingID:     s.ID,
		DataProductID: s.DataProductID,
		GroupID:       s.GroupID,
		Permission:    s.Permission,
	}
}

func ToDataProductGroupSharingResponses(in []model.DataProductGroupSharing) []DataProductGroupSharingResponse {
	out := make([]DataProductGroupSharingResponse, 0, len(in))
	for i := range in {
		out = append(out, ToDataProductGroupSharingResponse(&in[i]))
	}
	return out
}

// DataProductUserSharingResponse is the read model for a user share.
type DataProductUserSharingResponse struct {
	SharingID     string                       `json:"dataProductUserSharingId"`
	DataProductID *string                      `json:"dataProductId"`
	UserID        *string                      `json:"userId"`
	Permission    *model.DataProductPermission `json:"permission"`
}

func ToDataProductUserSharingResponse(s *model.DataProductUserSharing) DataProductUserSharingResponse {
	return DataProductUserSharingResponse{
		SharingID:     s.ID,
		DataProductID: s.DataProductID,
		UserID:        s.UserID,
		Permission:    s.Permission,
	}
}

func ToDataProductUserSharingResponses(in []model.DataProductUserSharing) []DataProductUserSharingResponse {
	out := make([]DataProductUserSharingResponse, 0, len(in))
	for i := range in {
		out = append(out, ToDataProductUserSharingResponse(&in[i]))
	}
	return out
}

func validateProductPermission(c *httpx.Constraints, p *model.DataProductPermission) {
	if p != nil && !p.Valid() {
		c.Add("permission", "Permission must be one of READ, WRITE")
	}
}

func productGrantOrRead(p *model.DataProductPermission) model.DataProductPermission {
	if p == nil {
		return model.DataProductPermissionRead
	}
	return *p
}
