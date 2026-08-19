// Package application holds application-template and deployment entities.
package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"
)

// TemplateInputType is the declared type of a template input.
//
// Java stored this as an ORDINAL int (the field carried no @Enumerated annotation),
// which meant reordering the enum silently reinterpreted existing rows. It is stored
// by name here. The JSON representation is unchanged — Jackson already serialised
// these by name — so this is a storage fix, not an API change.
//
// Java: org.apache.airavata.application.model.template.ApplicationTemplateInputType
type TemplateInputType string

const (
	TemplateInputTypeString    TemplateInputType = "STRING"
	TemplateInputTypeInteger   TemplateInputType = "INTEGER"
	TemplateInputTypeFloat     TemplateInputType = "FLOAT"
	TemplateInputTypeBoolean   TemplateInputType = "BOOLEAN"
	TemplateInputTypeFile      TemplateInputType = "FILE"
	TemplateInputTypeFileList  TemplateInputType = "FILE_LIST"
	TemplateInputTypeDirectory TemplateInputType = "DIRECTORY"
)

// Valid reports whether t is a recognised TemplateInputType.
func (t TemplateInputType) Valid() bool {
	switch t {
	case TemplateInputTypeString, TemplateInputTypeInteger, TemplateInputTypeFloat,
		TemplateInputTypeBoolean, TemplateInputTypeFile, TemplateInputTypeFileList,
		TemplateInputTypeDirectory:
		return true
	}
	return false
}

// TemplateOutputType is the declared type of a template output. Stored by name for
// the same reason as TemplateInputType.
//
// Java: org.apache.airavata.application.model.template.ApplicationTemplateOutputType
type TemplateOutputType string

const (
	TemplateOutputTypeFile      TemplateOutputType = "FILE"
	TemplateOutputTypeFileList  TemplateOutputType = "FILE_LIST"
	TemplateOutputTypeDirectory TemplateOutputType = "DIRECTORY"
)

// Valid reports whether t is a recognised TemplateOutputType.
func (t TemplateOutputType) Valid() bool {
	switch t {
	case TemplateOutputTypeFile, TemplateOutputTypeFileList, TemplateOutputTypeDirectory:
		return true
	}
	return false
}

// Template describes an application's input/output contract, independent of where it
// runs.
//
// Inputs and outputs are owned by the template: created, replaced and deleted with
// it. Deployments are not — they outlive individual edits and are managed through
// their own endpoints, so deleting a template that still has deployments is refused
// with a conflict in the service layer.
//
// Java: org.apache.airavata.application.model.template.ApplicationTemplateEntity
type Template struct {
	ID string `gorm:"column:template_id;primaryKey;type:varchar(36)" json:"templateId"`

	TemplateName        *string `gorm:"column:template_name;type:varchar(255)" json:"templateName,omitempty"`
	TemplateDescription *string `gorm:"column:template_description;type:varchar(2048)" json:"templateDescription,omitempty"`

	Inputs  []TemplateInput  `gorm:"foreignKey:TemplateID;references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"inputs,omitempty"`
	Outputs []TemplateOutput `gorm:"foreignKey:TemplateID;references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"outputs,omitempty"`

	// The Java entity also mapped a deployments collection. It is omitted here: the
	// only thing that reads it is the "refuse to delete a template that still has
	// deployments" check, which is a count against the deployments table, and the
	// restricting foreign key is already declared on BatchDeployment.Template.
}

// TableName returns the table backing Template.
func (Template) TableName() string { return "application_templates" }

// BeforeCreate assigns a UUID when none was supplied.
func (t *Template) BeforeCreate(*gorm.DB) error {
	if t.ID == "" {
		t.ID = uuid.NewString()
	}
	return nil
}

// TemplateInput is one declared input of a Template.
//
// The (template_id, input_name) pair is unique. The Java @UniqueConstraint named the
// column "input_name" while Hibernate's identity naming strategy actually produced
// "inputName", so the constraint referenced a column that did not exist and never
// took effect. Under the normalised snake_case schema the name matches and the
// constraint is live — meaning duplicate input names within a template are now
// rejected, as was always intended.
//
// Java: org.apache.airavata.application.model.template.ApplicationTemplateInputEntity
type TemplateInput struct {
	ID string `gorm:"column:input_id;primaryKey;type:varchar(36)" json:"inputId"`

	// Owned by Template.Inputs, which declares the cascade; only the key is held here.
	TemplateID *string `gorm:"column:template_id;type:varchar(36);uniqueIndex:uk_template_input_name" json:"templateId,omitempty"`

	InputName        *string `gorm:"column:input_name;type:varchar(255);uniqueIndex:uk_template_input_name" json:"inputName,omitempty"`
	DisplayName      *string `gorm:"column:display_name;type:varchar(255)" json:"displayName,omitempty"`
	InputDescription *string `gorm:"column:input_description;type:varchar(2048)" json:"inputDescription,omitempty"`

	InputType *TemplateInputType `gorm:"column:input_type;type:varchar(32)" json:"inputType,omitempty"`

	IsRequired bool `gorm:"column:is_required;not null" json:"isRequired"`

	// DefaultValue is a JSON document, either {"value": "..."} for a single value or
	// {"values": [...]} for a list.
	DefaultValue *string `gorm:"column:default_value;type:text" json:"defaultValue,omitempty"`
}

// TableName returns the table backing TemplateInput.
func (TemplateInput) TableName() string { return "application_template_inputs" }

// BeforeCreate assigns a UUID when none was supplied.
func (i *TemplateInput) BeforeCreate(*gorm.DB) error {
	if i.ID == "" {
		i.ID = uuid.NewString()
	}
	return nil
}

// TemplateOutput is one declared output of a Template.
//
// Java: org.apache.airavata.application.model.template.ApplicationTemplateOutputEntity
type TemplateOutput struct {
	ID string `gorm:"column:output_id;primaryKey;type:varchar(36)" json:"outputId"`

	// Owned by Template.Outputs, which declares the cascade; only the key is held here.
	TemplateID *string `gorm:"column:template_id;type:varchar(36);index" json:"templateId,omitempty"`

	OutputType *TemplateOutputType `gorm:"column:output_type;type:varchar(32)" json:"outputType,omitempty"`

	OutputName        *string `gorm:"column:output_name;type:varchar(255)" json:"outputName,omitempty"`
	DisplayName       *string `gorm:"column:display_name;type:varchar(255)" json:"displayName,omitempty"`
	OutputDescription *string `gorm:"column:output_description;type:varchar(2048)" json:"outputDescription,omitempty"`
}

// TableName returns the table backing TemplateOutput.
func (TemplateOutput) TableName() string { return "application_template_outputs" }

// BeforeCreate assigns a UUID when none was supplied.
func (o *TemplateOutput) BeforeCreate(*gorm.DB) error {
	if o.ID == "" {
		o.ID = uuid.NewString()
	}
	return nil
}
