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
	"strconv"
	"strings"
)

// Render writes the conversion back out as an sbatch script, in the order Airavata writes directives.
func Render(c *Conversion) string {
	var b strings.Builder
	shebang := c.Shebang
	if shebang == "" {
		shebang = "#!/bin/bash"
	}
	b.WriteString(shebang + "\n")
	line := func(name, value string) {
		b.WriteString("#SBATCH --" + name + "=" + quoteValue(value) + "\n")
	}
	i32 := func(name string, v *int32) {
		if v != nil {
			line(name, strconv.Itoa(int(*v)))
		}
	}
	str := func(name string, v *string) {
		if v != nil {
			line(name, *v)
		}
	}
	jc := c.JobConfig
	if jc.WallTimeMinutes > 0 {
		line("time", formatWallTime(jc.WallTimeMinutes))
	}
	if strings.TrimSpace(jc.Allocation) != "" {
		line("account", strings.TrimSpace(jc.Allocation))
	}
	if c.Partition != "" {
		line("partition", c.Partition)
	}
	i32("nodes", jc.Nodes)
	i32("ntasks", jc.Ntasks)
	i32("ntasks-per-node", jc.NtasksPerNode)
	if jc.CpusPerTask != nil {
		i32("cpus-per-task", jc.CpusPerTask)
	} else {
		i32("cpus-per-task", jc.Cpus)
	}
	str("mem", jc.Mem)
	str("mem-per-cpu", jc.MemPerCpu)
	str("gres", jc.Gres)
	i32("gpus", jc.Gpus)
	i32("gpus-per-node", jc.GpusPerNode)
	str("mem-per-gpu", jc.MemPerGpu)
	str("cpus-per-gpu", jc.CpusPerGpu)
	str("constraint", jc.Constraints)
	for _, o := range c.Unsupported {
		name := "--" + o.Name
		if o.Unknown {
			name = o.Flag
		}
		if o.HasValue {
			if strings.HasPrefix(name, "--") {
				name += "="
			}
			name += quoteValue(o.Value)
		}
		b.WriteString("#SBATCH " + name + " # not registrable\n")
	}
	b.WriteString("\n")
	run := previewText(c.segments)
	if run != "" {
		b.WriteString(run + "\n")
	}
	return b.String()
}

func quoteValue(v string) string {
	if v == "" || strings.ContainsAny(v, " \t#'\"\\") {
		return strconv.Quote(v)
	}
	return v
}

type diffOp struct {
	kind byte
	a, b int
}

// Diff is a unified line diff with two lines of context; empty when the texts match.
func Diff(original, rendered string) string {
	a := splitLines(original)
	b := splitLines(rendered)
	ops := diffLines(a, b)

	const ctx = 2
	var out strings.Builder
	header := false
	for i := 0; i < len(ops); {
		if ops[i].kind == ' ' {
			i++
			continue
		}
		start := max(i-ctx, 0)
		end := i
		for {
			for end < len(ops) && ops[end].kind != ' ' {
				end++
			}
			next := end
			for next < len(ops) && ops[next].kind == ' ' {
				next++
			}
			if next < len(ops) && next-end <= 2*ctx {
				end = next
				continue
			}
			end = min(end+ctx, len(ops))
			break
		}
		if !header {
			out.WriteString("--- original\n+++ airavata\n")
			header = true
		}
		writeHunk(&out, ops[start:end], a, b)
		i = end
	}
	return out.String()
}

func writeHunk(out *strings.Builder, ops []diffOp, a, b []string) {
	aStart, bStart, aLen, bLen := -1, -1, 0, 0
	aFirst, bFirst := ops[0].a, ops[0].b
	for _, op := range ops {
		if op.kind != '+' {
			if aStart < 0 {
				aStart = op.a
			}
			aLen++
		}
		if op.kind != '-' {
			if bStart < 0 {
				bStart = op.b
			}
			bLen++
		}
	}
	if aStart < 0 {
		aStart = aFirst - 1
	}
	if bStart < 0 {
		bStart = bFirst - 1
	}
	fmt.Fprintf(out, "@@ -%s +%s @@\n", hunkRange(aStart, aLen), hunkRange(bStart, bLen))
	for _, op := range ops {
		switch op.kind {
		case '-':
			out.WriteString("-" + a[op.a] + "\n")
		case '+':
			out.WriteString("+" + b[op.b] + "\n")
		default:
			out.WriteString(" " + a[op.a] + "\n")
		}
	}
}

func hunkRange(start, n int) string {
	if n == 0 {
		return fmt.Sprintf("%d,0", start+1)
	}
	if n == 1 {
		return strconv.Itoa(start + 1)
	}
	return fmt.Sprintf("%d,%d", start+1, n)
}

func splitLines(s string) []string {
	s = strings.ReplaceAll(s, "\r\n", "\n")
	if s == "" {
		return nil
	}
	return strings.Split(strings.TrimSuffix(s, "\n"), "\n")
}

// diffLines walks an LCS table; op.a/op.b hold the next index into each side even for inserts and deletes.
func diffLines(a, b []string) []diffOp {
	n, m := len(a), len(b)
	lcs := make([][]int, n+1)
	for i := range lcs {
		lcs[i] = make([]int, m+1)
	}
	for i := n - 1; i >= 0; i-- {
		for j := m - 1; j >= 0; j-- {
			if a[i] == b[j] {
				lcs[i][j] = lcs[i+1][j+1] + 1
			} else {
				lcs[i][j] = max(lcs[i+1][j], lcs[i][j+1])
			}
		}
	}
	var ops []diffOp
	i, j := 0, 0
	for i < n || j < m {
		switch {
		case i < n && j < m && a[i] == b[j]:
			ops = append(ops, diffOp{' ', i, j})
			i++
			j++
		case j < m && (i == n || lcs[i][j+1] > lcs[i+1][j]):
			ops = append(ops, diffOp{'+', i, j})
			j++
		default:
			ops = append(ops, diffOp{'-', i, j})
			i++
		}
	}
	return ops
}
