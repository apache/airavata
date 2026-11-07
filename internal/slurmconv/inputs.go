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

package slurmconv

import (
	"fmt"
	"regexp"
	"sort"
	"strings"
)

// InputTypes are the template input types a run section can reference.
var InputTypes = []string{"STRING", "INTEGER", "FLOAT", "BOOLEAN", "FILE", "FILE_LIST", "DIRECTORY"}

type InputSuggestion struct {
	Name        string `json:"name"`
	Type        string `json:"type"`
	Literal     string `json:"literal"`
	Description string `json:"description,omitempty"`
}

type AppliedInput struct {
	Name        string `json:"name"`
	Type        string `json:"type"`
	Description string `json:"description,omitempty"`
	Default     string `json:"default"`
	Occurrences int    `json:"occurrences"`
	Placeholder string `json:"placeholder"`
	Note        string `json:"note,omitempty"`
}

var inputNameRE = regexp.MustCompile(`^[A-Za-z_][A-Za-z0-9_]*$`)

// Segments returns the run section split into literal text and input references.
func (c *Conversion) Segments() []Segment {
	return append([]Segment(nil), c.segments...)
}

// Placeholder is the pongo2 expression a run section uses for an input of the given type.
func Placeholder(name, typ string) string {
	if typ == "FILE_LIST" {
		return `{{ inputs.` + name + `|join:" " }}`
	}
	return "{{ inputs." + name + "[0] }}"
}

// ApplyInputs turns hard-coded literals of the original body into template inputs, replacing any earlier application.
func ApplyInputs(c *Conversion, in []InputSuggestion) ([]AppliedInput, error) {
	valid := map[string]bool{}
	for _, t := range InputTypes {
		valid[t] = true
	}
	names := map[string]bool{}
	literals := map[string]int{}
	for i, s := range in {
		label := fmt.Sprintf("suggestion %d (%q)", i+1, s.Name)
		switch {
		case !inputNameRE.MatchString(s.Name):
			return nil, fmt.Errorf("%s: name must match %s", label, inputNameRE)
		case names[s.Name]:
			return nil, fmt.Errorf("%s: duplicate input name", label)
		case !valid[s.Type]:
			return nil, fmt.Errorf("%s: type %q is not one of %s", label, s.Type, strings.Join(InputTypes, ", "))
		case s.Literal == "":
			return nil, fmt.Errorf("%s: literal is empty", label)
		}
		if j, dup := literals[s.Literal]; dup {
			return nil, fmt.Errorf("%s: literal %q is already claimed by suggestion %d", label, s.Literal, j+1)
		}
		names[s.Name] = true
		literals[s.Literal] = i
	}

	order := make([]int, len(in))
	for i := range order {
		order[i] = i
	}
	sort.SliceStable(order, func(a, b int) bool { return len(in[order[a]].Literal) > len(in[order[b]].Literal) })

	body := c.body
	counts := make([]int, len(in))
	var segs []Segment
	var text strings.Builder
	for p := 0; p < len(body); {
		matched := -1
		if p == 0 || !tokenChar(body[p-1]) {
			for _, k := range order {
				lit := in[k].Literal
				end := p + len(lit)
				if strings.HasPrefix(body[p:], lit) && (end == len(body) || !tokenChar(body[end])) {
					matched = k
					break
				}
			}
		}
		if matched < 0 {
			text.WriteByte(body[p])
			p++
			continue
		}
		if text.Len() > 0 {
			segs = append(segs, Segment{Text: text.String()})
			text.Reset()
		}
		segs = append(segs, Segment{Input: in[matched].Name, InputType: in[matched].Type})
		counts[matched]++
		p += len(in[matched].Literal)
	}
	if text.Len() > 0 {
		segs = append(segs, Segment{Text: text.String()})
	}
	for i, s := range in {
		if counts[i] == 0 {
			return nil, fmt.Errorf("suggestion %d (%q): literal %q does not appear as a whole word in the run section", i+1, s.Name, s.Literal)
		}
	}

	out := make([]AppliedInput, len(in))
	for i, s := range in {
		out[i] = AppliedInput{Name: s.Name, Type: s.Type, Description: s.Description, Default: s.Literal,
			Occurrences: counts[i], Placeholder: Placeholder(s.Name, s.Type)}
		if s.Type == "FILE_LIST" {
			out[i].Note = "every file in the list is passed space-separated where the single literal was"
		}
	}
	c.segments = segs
	c.RunSection = templateSource(segs)
	return out, nil
}

func tokenChar(b byte) bool {
	return b >= 'a' && b <= 'z' || b >= 'A' && b <= 'Z' || b >= '0' && b <= '9' ||
		b == '_' || b == '.' || b == '/' || b == '-'
}

func templateSource(segs []Segment) string {
	var b strings.Builder
	for i, s := range segs {
		if s.Input != "" {
			b.WriteString(Placeholder(s.Input, s.InputType))
			continue
		}
		next := i+1 < len(segs) && segs[i+1].Input != ""
		b.WriteString(escapeTemplateText(s.Text, next))
	}
	return b.String()
}

// escapeTemplateText keeps pongo2 from reading {{, {% or {# in shell text, e.g. ${#arr[@]}.
func escapeTemplateText(s string, beforePlaceholder bool) string {
	var b strings.Builder
	for i := 0; i < len(s); i++ {
		if s[i] == '{' {
			opens := beforePlaceholder && i == len(s)-1
			if i+1 < len(s) && (s[i+1] == '{' || s[i+1] == '%' || s[i+1] == '#') {
				opens = true
			}
			if opens {
				b.WriteString(`{{ "{" }}`)
				continue
			}
		}
		b.WriteByte(s[i])
	}
	return b.String()
}

func previewText(segs []Segment) string {
	var b strings.Builder
	for _, s := range segs {
		if s.Input != "" {
			b.WriteString(Placeholder(s.Input, s.InputType))
		} else {
			b.WriteString(s.Text)
		}
	}
	return b.String()
}
