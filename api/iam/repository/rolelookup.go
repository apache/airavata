package iam

import (
	"context"
	"log/slog"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/role"

	model "github.com/apache/airavata/api/iam/model"
)

// DBRoleLookup resolves a caller's authorities from the user_roles table.
//
// It is the intended replacement for auth.MockRoleLookup: the schema already carries
// the rows, they just are not administered through any endpoint yet.
type DBRoleLookup struct {
	DB *gorm.DB
}

// Roles implements auth.RoleLookup.
//
// A caller with no rows — including one who was never registered — gets USER. Failing
// closed to the least privilege matters more here than reporting the error, since the
// alternative on a transient database fault would be to deny every request; USER is
// the same answer the mock gives an unknown caller.
func (l DBRoleLookup) Roles(ctx context.Context, username string) []string {
	var rows []model.UserRole
	if err := l.DB.WithContext(ctx).Where("user_id = ?", username).Find(&rows).Error; err != nil {
		slog.Error("failed to look up user roles; falling back to the default role",
			"username", username, "error", err)
		return []string{string(role.User)}
	}
	if len(rows) == 0 {
		return []string{string(role.User)}
	}
	roles := make([]string, 0, len(rows))
	for _, r := range rows {
		roles = append(roles, string(r.Role))
	}
	return roles
}
