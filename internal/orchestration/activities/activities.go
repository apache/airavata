// Package activities holds the units of work a workflow schedules: everything that
// touches the outside world, and so everything that cannot live in workflow code.
//
// Every activity is a method on Activities rather than a package-level function. An
// activity needs the services, and go-workflows serializes both workflow and activity
// arguments to the backend as payloads — a service holding a *gorm.DB cannot be passed
// in as one. The struct carries them out of band instead: RegisterActivity(a) walks the
// exported methods and registers each under its bare name, and a call site naming the
// method value (acts.CopyData) resolves to that same name.
package activities

import (
	"github.com/apache/airavata/internal/app"
)

// Activities is the set of activities, bound to the services they act through.
type Activities struct{ svcs *app.Services }

// New returns the activity set for svcs.
func New(svcs *app.Services) *Activities { return &Activities{svcs: svcs} }
