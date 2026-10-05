package model

type AccessPermission string

const (
	AccessPermissionRead  AccessPermission = "READ"
	AccessPermissionWrite AccessPermission = "WRITE"
	AccessPermissionNone  AccessPermission = ""
)

func (p AccessPermission) Valid() bool {
	switch p {
	case AccessPermissionRead, AccessPermissionWrite, AccessPermissionNone:
		return true
	}
	return false
}

func (current AccessPermission) Allows(want AccessPermission) bool {
	if current == AccessPermissionNone || want == AccessPermissionNone {
		return false
	}
	return current == AccessPermissionWrite || current == want
}
