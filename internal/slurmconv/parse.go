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
	"strings"
)

// Option is one sbatch directive option, normalised to its long name.
type Option struct {
	Name     string `json:"name"`
	Value    string `json:"value,omitempty"`
	HasValue bool   `json:"hasValue"`
	Line     int    `json:"line"`
	Raw      string `json:"raw"`
	Flag     string `json:"flag"`
	Unknown  bool   `json:"unknown,omitempty"`
}

// Script is a parsed sbatch script.
type Script struct {
	Shebang string   `json:"shebang,omitempty"`
	Options []Option `json:"options"`
	Body    string   `json:"body"`
	// Ignored lists lines of #SBATCH directives after the first command, which sbatch ignores.
	Ignored []int `json:"ignored,omitempty"`
}

type argKind int

const (
	argNone argKind = iota
	argRequired
	argOptional
)

var longOptions = map[string]argKind{
	"account": argRequired, "acctg-freq": argRequired, "array": argRequired, "batch": argRequired,
	"bb": argRequired, "bbf": argRequired, "begin": argRequired, "chdir": argRequired,
	"cluster-constraint": argRequired, "clusters": argRequired, "comment": argRequired,
	"constraint": argRequired, "container": argRequired, "container-id": argRequired,
	"contiguous": argNone, "core-spec": argRequired, "cores-per-socket": argRequired,
	"cpu-freq": argRequired, "cpus-per-gpu": argRequired, "cpus-per-task": argRequired,
	"deadline": argRequired, "delay-boot": argRequired, "dependency": argRequired,
	"distribution": argRequired, "error": argRequired, "exclude": argRequired,
	"exclusive": argOptional, "export": argRequired, "export-file": argRequired,
	"extra": argRequired, "extra-node-info": argRequired, "get-user-env": argOptional,
	"gid": argRequired, "gpu-bind": argRequired, "gpu-freq": argRequired, "gpus": argRequired,
	"gpus-per-node": argRequired, "gpus-per-socket": argRequired, "gpus-per-task": argRequired,
	"gres": argRequired, "gres-flags": argRequired, "help": argNone, "hint": argRequired,
	"hold": argNone, "ignore-pbs": argNone, "input": argRequired, "job-name": argRequired,
	"kill-on-invalid-dep": argRequired, "licenses": argRequired, "mail-type": argRequired,
	"mail-user": argRequired, "mcs-label": argRequired, "mem": argRequired,
	"mem-bind": argRequired, "mem-per-cpu": argRequired, "mem-per-gpu": argRequired,
	"mincpus": argRequired, "network": argRequired, "nice": argOptional, "no-kill": argOptional,
	"no-requeue": argNone, "nodefile": argRequired, "nodelist": argRequired, "nodes": argRequired,
	"ntasks": argRequired, "ntasks-per-core": argRequired, "ntasks-per-gpu": argRequired,
	"ntasks-per-node": argRequired, "ntasks-per-socket": argRequired, "oom-kill-step": argOptional,
	"open-mode": argRequired, "output": argRequired, "overcommit": argNone,
	"oversubscribe": argNone, "parsable": argNone, "partition": argRequired, "power": argRequired,
	"prefer": argRequired, "priority": argRequired, "profile": argRequired,
	"propagate": argOptional, "qos": argRequired, "quiet": argNone, "reboot": argNone,
	"requeue": argNone, "reservation": argRequired, "segment": argRequired, "signal": argRequired,
	"sockets-per-node": argRequired, "spread-job": argNone, "stepmgr": argNone,
	"switches": argRequired, "test-only": argNone, "thread-spec": argRequired,
	"threads-per-core": argRequired, "time": argRequired, "time-min": argRequired,
	"tmp": argRequired, "tres-bind": argRequired, "tres-per-task": argRequired, "uid": argRequired,
	"usage": argNone, "use-min-nodes": argNone, "verbose": argNone, "version": argNone,
	"wait": argNone, "wait-all-nodes": argRequired, "wckey": argRequired, "wrap": argRequired,
}

var shortOptions = map[byte]string{
	'A': "account", 'a': "array", 'B': "extra-node-info", 'b': "begin", 'C': "constraint",
	'c': "cpus-per-task", 'D': "chdir", 'd': "dependency", 'e': "error", 'F': "nodefile",
	'G': "gpus", 'H': "hold", 'h': "help", 'i': "input", 'J': "job-name", 'k': "no-kill",
	'L': "licenses", 'M': "clusters", 'm': "distribution", 'N': "nodes", 'n': "ntasks",
	'O': "overcommit", 'o': "output", 'p': "partition", 'Q': "quiet", 'q': "qos",
	'S': "core-spec", 's': "oversubscribe", 't': "time", 'u': "usage", 'V': "version",
	'v': "verbose", 'W': "wait", 'w': "nodelist", 'x': "exclude",
}

// ShortFlag returns the single-letter form of a long option, if sbatch has one.
func ShortFlag(long string) (string, bool) {
	for k, v := range shortOptions {
		if v == long {
			return "-" + string(k), true
		}
	}
	return "", false
}

// Parse reads the #SBATCH directives of the leading comment block, the way sbatch does.
func Parse(text string) (*Script, error) {
	text = strings.ReplaceAll(text, "\r\n", "\n")
	lines := strings.Split(text, "\n")
	s := &Script{}

	start := 0
	if len(lines) > 0 && strings.HasPrefix(lines[0], "#!") {
		s.Shebang = lines[0]
		start = 1
	}
	bodyLine := start
	inHeader := true
	for i := start; i < len(lines); i++ {
		line := lines[i]
		trimmed := strings.TrimSpace(line)
		if inHeader && trimmed != "" && !strings.HasPrefix(trimmed, "#") {
			inHeader = false
		}
		if !isDirective(line) {
			continue
		}
		if !inHeader {
			s.Ignored = append(s.Ignored, i+1)
			continue
		}
		if strings.ContainsRune(line, '\r') {
			return nil, fmt.Errorf("line %d: carriage return inside an #SBATCH directive", i+1)
		}
		opts, err := parseDirective(line[len("#SBATCH"):], i+1, line)
		if err != nil {
			return nil, err
		}
		s.Options = append(s.Options, opts...)
		bodyLine = i + 1
	}
	s.Body = strings.Join(lines[bodyLine:], "\n")
	return s, nil
}

func isDirective(line string) bool {
	if !strings.HasPrefix(line, "#SBATCH") {
		return false
	}
	rest := line[len("#SBATCH"):]
	return rest == "" || rest[0] == ' ' || rest[0] == '\t'
}

func parseDirective(text string, lineNo int, raw string) ([]Option, error) {
	tokens, err := splitWords(text, true)
	if err != nil {
		return nil, fmt.Errorf("line %d: %w", lineNo, err)
	}
	var out []Option
	for i := 0; i < len(tokens); i++ {
		tok := tokens[i]
		mk := func(name, flag string) Option {
			return Option{Name: name, Line: lineNo, Raw: raw, Flag: flag}
		}
		switch {
		case strings.HasPrefix(tok, "--") && len(tok) > 2:
			name, value, hasEq := strings.Cut(tok[2:], "=")
			canonical, kind, known := lookupLong(name)
			o := mk(canonical, "--"+name)
			if !known {
				o.Unknown = true
				if hasEq {
					o.Value, o.HasValue = value, true
				} else if i+1 < len(tokens) && !strings.HasPrefix(tokens[i+1], "-") {
					i++
					o.Value, o.HasValue = tokens[i], true
				}
				out = append(out, o)
				continue
			}
			switch kind {
			case argNone:
				if hasEq {
					return nil, fmt.Errorf("line %d: option --%s takes no value", lineNo, canonical)
				}
			case argOptional:
				if hasEq {
					o.Value, o.HasValue = value, true
				}
			case argRequired:
				if hasEq {
					o.Value, o.HasValue = value, true
				} else {
					if i+1 >= len(tokens) {
						return nil, fmt.Errorf("line %d: option --%s requires a value", lineNo, canonical)
					}
					i++
					o.Value, o.HasValue = tokens[i], true
				}
			}
			out = append(out, o)
		case strings.HasPrefix(tok, "-") && len(tok) > 1 && tok[1] != '-':
			for j := 1; j < len(tok); j++ {
				name, ok := shortOptions[tok[j]]
				flag := "-" + string(tok[j])
				if !ok {
					o := mk(string(tok[j]), flag)
					o.Unknown = true
					if j+1 < len(tok) {
						o.Value, o.HasValue = tok[j+1:], true
					}
					out = append(out, o)
					break
				}
				o := mk(name, flag)
				kind := longOptions[name]
				if kind == argNone {
					out = append(out, o)
					continue
				}
				if j+1 < len(tok) {
					o.Value, o.HasValue = tok[j+1:], true
				} else if kind == argRequired {
					if i+1 >= len(tokens) {
						return nil, fmt.Errorf("line %d: option %s requires a value", lineNo, flag)
					}
					i++
					o.Value, o.HasValue = tokens[i], true
				}
				out = append(out, o)
				break
			}
		default:
			return nil, fmt.Errorf("line %d: unexpected argument %q in #SBATCH directive", lineNo, tok)
		}
	}
	return out, nil
}

func lookupLong(name string) (string, argKind, bool) {
	if kind, ok := longOptions[name]; ok {
		return name, kind, true
	}
	var matches []string
	for k := range longOptions {
		if strings.HasPrefix(k, name) {
			matches = append(matches, k)
		}
	}
	if len(matches) == 1 {
		return matches[0], longOptions[matches[0]], true
	}
	return name, argRequired, false
}

// splitWords splits a line into shell-like words; with stopAtComment an unquoted word starting with # ends it.
func splitWords(text string, stopAtComment bool) ([]string, error) {
	var words []string
	var cur strings.Builder
	inWord := false
	var quote byte
	for i := 0; i < len(text); i++ {
		c := text[i]
		if quote != 0 {
			if c == quote {
				quote = 0
			} else {
				cur.WriteByte(c)
			}
			continue
		}
		switch {
		case c == '"' || c == '\'':
			quote = c
			inWord = true
		case c == ' ' || c == '\t':
			if inWord {
				words = append(words, cur.String())
				cur.Reset()
				inWord = false
			}
		case c == '#' && !inWord && stopAtComment:
			return words, nil
		case c == '\\' && i+1 < len(text) && quote == 0:
			i++
			cur.WriteByte(text[i])
			inWord = true
		default:
			cur.WriteByte(c)
			inWord = true
		}
	}
	if quote != 0 {
		return nil, fmt.Errorf("unterminated %c quote", quote)
	}
	if inWord {
		words = append(words, cur.String())
	}
	return words, nil
}
