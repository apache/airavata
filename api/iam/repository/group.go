// Package repository reads and writes users, roles, groups and group memberships.
package repository

import (
	"context"

	"gorm.io/gorm"
	"gorm.io/gorm/clause"

	model "github.com/apache/airavata/api/iam/model"
)

// GroupRepository reads and writes groups.
type GroupRepository struct{ db *gorm.DB }

// NewGroupRepository returns a repository backed by db.
func NewGroupRepository(db *gorm.DB) *GroupRepository { return &GroupRepository{db: db} }

// WithTx returns a repository bound to tx.
func (r *GroupRepository) WithTx(tx *gorm.DB) *GroupRepository { return &GroupRepository{db: tx} }

// FindAll returns every group.
func (r *GroupRepository) FindAll(ctx context.Context) ([]model.Group, error) {
	var out []model.Group
	err := r.db.WithContext(ctx).Find(&out).Error
	return out, err
}

// FindByID returns one group, or gorm.ErrRecordNotFound.
func (r *GroupRepository) FindByID(ctx context.Context, id string) (*model.Group, error) {
	var out model.Group
	if err := r.db.WithContext(ctx).First(&out, "group_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// FindVisibleTo returns the groups userID owns or holds a membership in.
//
// Inactive memberships count here: a suspended member can still see that the group
// exists, which is what makes "why can I no longer read anything shared with it?"
// answerable. What an inactive membership does not grant is reading through the
// group — that check lives in the service.
func (r *GroupRepository) FindVisibleTo(ctx context.Context, userID string) ([]model.Group, error) {
	var out []model.Group
	memberOf := r.db.Model(&model.GroupMember{}).Select("group_id").Where("user_id = ?", userID)
	err := r.db.WithContext(ctx).
		Where("user_id = ? OR group_id IN (?)", userID, memberOf).
		Find(&out).Error
	return out, err
}

// Save inserts or updates a group.
func (r *GroupRepository) Save(ctx context.Context, g *model.Group) error {
	return r.db.WithContext(ctx).Save(g).Error
}

// Delete removes a group; its membership rows go with it via the cascading constraint.
func (r *GroupRepository) Delete(ctx context.Context, g *model.Group) error {
	return r.db.WithContext(ctx).Delete(g).Error
}

// GroupMemberRepository reads and writes group memberships.
type GroupMemberRepository struct{ db *gorm.DB }

// NewGroupMemberRepository returns a repository backed by db.
func NewGroupMemberRepository(db *gorm.DB) *GroupMemberRepository {
	return &GroupMemberRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *GroupMemberRepository) WithTx(tx *gorm.DB) *GroupMemberRepository {
	return &GroupMemberRepository{db: tx}
}

// FindByGroupID returns every membership of one group.
func (r *GroupMemberRepository) FindByGroupID(ctx context.Context, groupID string) ([]model.GroupMember, error) {
	var out []model.GroupMember
	err := r.db.WithContext(ctx).Where("group_id = ?", groupID).Find(&out).Error
	return out, err
}

// FindByUserID returns every membership held by one user.
func (r *GroupMemberRepository) FindByUserID(ctx context.Context, userID string) ([]model.GroupMember, error) {
	var out []model.GroupMember
	err := r.db.WithContext(ctx).Where("user_id = ?", userID).Find(&out).Error
	return out, err
}

// FindByGroupIDAndUserID returns one membership, or gorm.ErrRecordNotFound.
func (r *GroupMemberRepository) FindByGroupIDAndUserID(ctx context.Context, groupID, userID string) (*model.GroupMember, error) {
	var out model.GroupMember
	err := r.db.WithContext(ctx).
		First(&out, "group_id = ? AND user_id = ?", groupID, userID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Save inserts or updates a membership.
//
// This is an explicit upsert rather than a plain Save. Save decides between an insert
// and an update by running the update first and inserting when it reports no rows
// affected — and MySQL reports rows *changed*, so re-submitting a membership with the
// values it already holds would fall through to an insert and fail on the primary key.
// The whole key is always known here, so stating the conflict is both cheaper and
// exact.
func (r *GroupMemberRepository) Save(ctx context.Context, m *model.GroupMember) error {
	return r.db.WithContext(ctx).Clauses(clause.OnConflict{
		Columns:   []clause.Column{{Name: "group_id"}, {Name: "user_id"}},
		UpdateAll: true,
	}).Create(m).Error
}

// Delete removes a membership.
//
// The where clause is spelled out because a composite primary key carried on the
// struct is the only thing that scopes the delete, and an empty field would otherwise
// widen it to every row that matches the rest.
func (r *GroupMemberRepository) Delete(ctx context.Context, m *model.GroupMember) error {
	return r.db.WithContext(ctx).
		Where("group_id = ? AND user_id = ?", m.GroupID, m.UserID).
		Delete(&model.GroupMember{}).Error
}
