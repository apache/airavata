/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
 */

package policy

import (
	"bytes"
	"errors"
	"fmt"
	"io"
	"io/fs"
	"os"
	"path/filepath"
	"slices"
	"strings"

	"gopkg.in/yaml.v3"
)

const (
	Compute      = "compute"
	Applications = "applications"
	Data         = "data"
	Processes    = "processes"
	Sharing      = "sharing"
	Admin        = "admin"
)

// AllToolsets lists every toolset in registration order.
var AllToolsets = []string{Compute, Applications, Data, Processes, Sharing, Admin}

// DefaultToolsets are enabled when --toolsets is not given; sharing and admin are opt-in.
var DefaultToolsets = []string{Compute, Applications, Data, Processes}

// Limits are local caps checked before a launch confirmation is shown. Zero means no cap.
type Limits struct {
	MaxWalltimeMinutes    int64   `yaml:"max_walltime_minutes"`
	MaxCoreHoursPerLaunch float64 `yaml:"max_core_hours_per_launch"`
	MaxGPUHoursPerLaunch  float64 `yaml:"max_gpu_hours_per_launch"`
}

// File is the policy file. It can only add restrictions.
type File struct {
	Deny    []string `yaml:"deny"`
	Confirm []string `yaml:"confirm"`
	Limits  Limits   `yaml:"limits"`
}

// Policy is the effective set of restrictions for one server.
type Policy struct {
	AllowWrite bool
	Admin      bool
	Lockdown   bool
	Toolsets   []string
	// SkipConfirmation runs writes without an elicitation form, leaving approval to the client.
	SkipConfirmation bool
	File             File
}

// Toolset reports whether the named toolset is enabled.
func (p *Policy) Toolset(name string) bool {
	if name == Admin {
		return p.Admin
	}
	return slices.Contains(p.Toolsets, name)
}

// Exposed reports whether a tool is listed: never if denied, and writes only with --allow-write.
func (p *Policy) Exposed(tool string, readOnly bool) bool {
	if slices.Contains(p.File.Deny, tool) {
		return false
	}
	return readOnly || p.AllowWrite
}

// NeedsConfirmation reports whether a call must be confirmed by the user first.
func (p *Policy) NeedsConfirmation(tool string, readOnly bool) bool {
	return !readOnly || slices.Contains(p.File.Confirm, tool)
}

// Validate rejects unknown toolsets and combinations the plan forbids.
func (p *Policy) Validate(transport string) error {
	for _, t := range p.Toolsets {
		if !slices.Contains(AllToolsets, t) {
			return fmt.Errorf("unknown toolset %q (known: %s)", t, strings.Join(AllToolsets, ", "))
		}
		if t == Admin {
			return errors.New("the admin toolset is enabled with --admin, not --toolsets")
		}
	}
	if p.Admin && transport != "stdio" {
		return errors.New("--admin is only available on the stdio transport")
	}
	if p.SkipConfirmation && len(p.File.Confirm) > 0 {
		return errors.New("the policy file's confirm list needs elicitation; it cannot be used with --unsafe-skip-confirmation")
	}
	l := p.File.Limits
	if l.MaxWalltimeMinutes < 0 || l.MaxCoreHoursPerLaunch < 0 || l.MaxGPUHoursPerLaunch < 0 {
		return errors.New("policy limits must not be negative")
	}
	return nil
}

// DefaultPath is $XDG_CONFIG_HOME/airavata-mcp/policy.yaml (or the OS equivalent).
func DefaultPath() string {
	dir, err := os.UserConfigDir()
	if err != nil {
		return ""
	}
	return filepath.Join(dir, "airavata-mcp", "policy.yaml")
}

// LoadFile reads a policy file. A missing file at the default path is not an error.
func LoadFile(path string, explicit bool) (File, error) {
	var f File
	if path == "" {
		return f, nil
	}
	data, err := os.ReadFile(path)
	if err != nil {
		if errors.Is(err, fs.ErrNotExist) && !explicit {
			return f, nil
		}
		return f, fmt.Errorf("reading policy file: %w", err)
	}
	dec := yaml.NewDecoder(bytes.NewReader(data))
	dec.KnownFields(true)
	if err := dec.Decode(&f); err != nil && !errors.Is(err, io.EOF) {
		return f, fmt.Errorf("parsing policy file %s: %w", path, err)
	}
	return f, nil
}

// Estimate is the requested size of one launch.
type Estimate struct {
	WalltimeMinutes int64
	CoreHours       float64
	GPUHours        float64
}

// CheckLimits returns one message per local cap the estimate exceeds.
func (p *Policy) CheckLimits(e Estimate) []string {
	var out []string
	l := p.File.Limits
	if l.MaxWalltimeMinutes > 0 && e.WalltimeMinutes > l.MaxWalltimeMinutes {
		out = append(out, fmt.Sprintf("walltime %d min exceeds the local cap of %d min", e.WalltimeMinutes, l.MaxWalltimeMinutes))
	}
	if l.MaxCoreHoursPerLaunch > 0 && e.CoreHours > l.MaxCoreHoursPerLaunch {
		out = append(out, fmt.Sprintf("%.1f requested core-hours exceed the local cap of %.1f", e.CoreHours, l.MaxCoreHoursPerLaunch))
	}
	if l.MaxGPUHoursPerLaunch > 0 && e.GPUHours > l.MaxGPUHoursPerLaunch {
		out = append(out, fmt.Sprintf("%.1f requested GPU-hours exceed the local cap of %.1f", e.GPUHours, l.MaxGPUHoursPerLaunch))
	}
	return out
}

// ParseList splits a comma-separated flag value.
func ParseList(s string) []string {
	var out []string
	for _, part := range strings.Split(s, ",") {
		if part = strings.TrimSpace(part); part != "" {
			out = append(out, part)
		}
	}
	return out
}
