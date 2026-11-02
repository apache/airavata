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

package service

import (
	"context"
	"fmt"
	"log/slog"
	"net/http"
	"path"
	"sort"
	"strconv"
	"strings"
	"time"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"
	"github.com/apache/airavata/internal/ssh"

	dto "github.com/apache/airavata/api/compute/dto"
	credsvc "github.com/apache/airavata/api/credentials/service"
)

// discoveryTimeout bounds one discovery. It is generous because a login made with an
// interactive credential waits for its owner to answer a password or OTP prompt.
const discoveryTimeout = 5 * time.Minute

// CommandRunner runs one command on a host, returning its stdout and stderr.
type CommandRunner func(ctx context.Context, dial ssh.SSHDialConfig, command, target string) (string, string, error)

// ClusterDiscoveryService reads what a Slurm cluster already knows off the cluster
// itself — its partitions, and the allocations a login may charge — so that neither
// has to be retyped into a registration.
type ClusterDiscoveryService struct {
	keys    *credsvc.KeyAccess
	configs *ConfigAccess
	run     CommandRunner
}

// NewClusterDiscoveryService returns a discovery service logging in through sshCtx,
// sharing its connection pool and interactive auth store with job submission.
func NewClusterDiscoveryService(keys *credsvc.KeyAccess, configs *ConfigAccess, sshCtx *ssh.GlobalSSHContext) *ClusterDiscoveryService {
	return &ClusterDiscoveryService{
		keys:    keys,
		configs: configs,
		run: func(ctx context.Context, dial ssh.SSHDialConfig, command, target string) (string, string, error) {
			return ssh.RunSSHCommand(ctx, dial, sshCtx, command, target)
		},
	}
}

// WithRunner returns a copy of s running commands through run, for tests.
func (s *ClusterDiscoveryService) WithRunner(run CommandRunner) *ClusterDiscoveryService {
	return &ClusterDiscoveryService{keys: s.keys, configs: s.configs, run: run}
}

// DiscoverPartitions logs in to the head node and returns its partitions, in the shape
// a cluster registration takes them.
//
// It is administrative, like registering the cluster it serves, and it logs in only
// under a key the caller owns: it is a way to make the server open an SSH session to
// a host of the caller's choosing, which nobody else's key may be lent to.
func (s *ClusterDiscoveryService) DiscoverPartitions(ctx context.Context, req *dto.PartitionDiscoveryRequest) (*dto.PartitionDiscoveryResponse, error) {
	principal, err := auth.RequireAdmin(ctx)
	if err != nil {
		return nil, err
	}
	key, err := s.keys.RequireOwned(ctx, req.SSHCredentialID)
	if err != nil {
		return nil, err
	}

	ctx, cancel := context.WithTimeout(ctx, discoveryTimeout)
	defer cancel()

	port := req.HeadnodePort
	if port == 0 {
		port = 22
	}
	dial := ssh.NewSSHDialConfig(req.HeadnodeHost, port, req.LoginUser, *key, principal.Name)
	target := fmt.Sprintf("ssh %s@%s:%d", req.LoginUser, req.HeadnodeHost, port)

	scontrol := slurmCommand(req.SlurmHome, "scontrol") + " show partition -o"
	stdout, stderr, err := s.run(ctx, dial, scontrol, target)
	if err != nil {
		return nil, discoveryFailed("Partition", err, stderr)
	}
	partitions := parseScontrolPartitions(stdout)
	if len(partitions) == 0 {
		return nil, httpx.Conflict("scontrol on %s reported no partitions", req.HeadnodeHost)
	}

	// sinfo adds what scontrol reports only as partition totals — CPUs, memory and GRES
	// per node. It is a refinement, so failing to run it costs those fields, not the
	// discovery. The pooled connection is reused, so an interactive login is not asked
	// to answer twice.
	sinfo := slurmCommand(req.SlurmHome, "sinfo") + " -h -a -o " + ssh.ShellQuote("%R|%c|%m|%G")
	if out, _, err := s.run(ctx, dial, sinfo, target); err == nil {
		applySinfo(partitions, out)
	} else {
		slog.Warn("sinfo failed during partition discovery; per-node details omitted", "host", req.HeadnodeHost, "error", err)
	}

	out := make([]dto.ClusterPartitionRequest, 0, len(partitions))
	for _, p := range partitions {
		out = append(out, p.request())
	}
	return &dto.PartitionDiscoveryResponse{Partitions: out}, nil
}

// slurmCommand names a Slurm binary, under slurmHome when the cluster sets one — the
// same rule job submission follows for sbatch, so a discovery that works means a
// submission will find its binaries too.
func slurmCommand(slurmHome *string, name string) string {
	if slurmHome != nil && strings.TrimSpace(*slurmHome) != "" {
		return ssh.ShellQuote(path.Join(strings.TrimSpace(*slurmHome), name))
	}
	return name
}

// discoveryFailed reports a login or command that did not work. Unlike a data
// transfer, the reason is returned: the caller is an admin probing a host with their
// own key, and "command not found" or a refused key is exactly what they need to see.
func discoveryFailed(what string, err error, stderr string) *httpx.Error {
	msg := err.Error()
	if strings.Contains(stderr, "command not found") || strings.Contains(stderr, "No such file") {
		msg = "Slurm commands were not found on the head node; set the cluster's Slurm home to the directory holding them (" + msg + ")"
	}
	return &httpx.Error{Status: http.StatusBadGateway, Message: what + " discovery failed: " + msg, Err: err}
}

// discovered accumulates what is known of one partition across scontrol and sinfo.
type discovered struct {
	name          string
	isDefault     bool
	maxRunTime    *int32
	defaultWall   *int64
	maxNodes      *int32
	maxProcessors *int32
	maxMemory     *int64
	cpuPerNode    *int32
	gres          *string
	nodes         *string
	totalCPUs     int64
	totalNodes    int64
}

func (d *discovered) request() dto.ClusterPartitionRequest {
	isDefault := d.isDefault
	cpuPerNode := d.cpuPerNode
	// Without sinfo, an even split of the partition's CPUs is the best per-node figure.
	if cpuPerNode == nil && d.totalNodes > 0 && d.totalCPUs > 0 {
		cpuPerNode = int32Ptr(d.totalCPUs / d.totalNodes)
	}
	return dto.ClusterPartitionRequest{
		Name:            d.name,
		MaxRunTime:      d.maxRunTime,
		DefaultWalltime: d.defaultWall,
		MaxNodes:        d.maxNodes,
		MaxProcessors:   d.maxProcessors,
		MaxMemory:       d.maxMemory,
		CPUPerNode:      cpuPerNode,
		Gres:            d.gres,
		Nodes:           d.nodes,
		IsDefaultQueue:  &isDefault,
	}
}

// parseScontrolPartitions reads `scontrol show partition -o`: one partition per line,
// as space-separated Key=Value pairs. Partitions come back in the order scontrol lists
// them.
func parseScontrolPartitions(out string) []*discovered {
	var parts []*discovered
	for _, line := range strings.Split(out, "\n") {
		fields := scontrolFields(line)
		name := fields["PartitionName"]
		if name == "" {
			continue
		}
		d := &discovered{name: name, isDefault: strings.EqualFold(fields["Default"], "YES")}
		if m, ok := slurmMinutes(fields["MaxTime"]); ok {
			d.maxRunTime = int32Ptr(m)
		}
		if m, ok := slurmMinutes(fields["DefaultTime"]); ok {
			v := m
			d.defaultWall = &v
		}
		d.totalNodes, _ = strconv.ParseInt(fields["TotalNodes"], 10, 64)
		d.totalCPUs, _ = strconv.ParseInt(fields["TotalCPUs"], 10, 64)
		// An unlimited per-job node count is still bounded by the partition's size.
		if n, err := strconv.ParseInt(fields["MaxNodes"], 10, 64); err == nil {
			d.maxNodes = int32Ptr(n)
		} else if d.totalNodes > 0 {
			d.maxNodes = int32Ptr(d.totalNodes)
		}
		if d.totalCPUs > 0 {
			d.maxProcessors = int32Ptr(d.totalCPUs)
		}
		if mem, err := strconv.ParseInt(fields["MaxMemPerNode"], 10, 64); err == nil && mem > 0 {
			d.maxMemory = &mem
		}
		if nodes := fields["Nodes"]; nodes != "" && !strings.EqualFold(nodes, "(null)") {
			d.nodes = &nodes
		}
		parts = append(parts, d)
	}
	return parts
}

func scontrolFields(line string) map[string]string {
	fields := map[string]string{}
	for _, tok := range strings.Fields(line) {
		if k, v, ok := strings.Cut(tok, "="); ok {
			fields[k] = v
		}
	}
	return fields
}

// applySinfo folds `sinfo -h -a -o '%R|%c|%m|%G'` into parts. sinfo prints a row per
// partition per node configuration, so a partition of mixed nodes has several; the
// largest node is reported, being what a single-node job could be granted.
func applySinfo(parts []*discovered, out string) {
	byName := make(map[string]*discovered, len(parts))
	for _, p := range parts {
		byName[p.name] = p
	}
	gres := map[string]map[string]bool{}
	for _, line := range strings.Split(out, "\n") {
		cols := strings.Split(strings.TrimSpace(line), "|")
		if len(cols) != 4 {
			continue
		}
		p := byName[cols[0]]
		if p == nil {
			continue
		}
		if cpus, ok := leadingInt(cols[1]); ok && (p.cpuPerNode == nil || int64(*p.cpuPerNode) < cpus) {
			p.cpuPerNode = int32Ptr(cpus)
		}
		// scontrol's MaxMemPerNode is a limit; where none is set, the node size is.
		if mem, ok := leadingInt(cols[2]); ok && mem > 0 && (p.maxMemory == nil || *p.maxMemory < mem) {
			p.maxMemory = &mem
		}
		for _, g := range strings.Split(cols[3], ",") {
			// gpu:a100:4(S:0-1) — the socket binding is not part of a request.
			g, _, _ = strings.Cut(strings.TrimSpace(g), "(")
			if g != "" && g != "null" {
				if gres[p.name] == nil {
					gres[p.name] = map[string]bool{}
				}
				gres[p.name][g] = true
			}
		}
	}
	for name, set := range gres {
		list := make([]string, 0, len(set))
		for g := range set {
			list = append(list, g)
		}
		sort.Strings(list)
		joined := strings.Join(list, ",")
		byName[name].gres = &joined
	}
}

// slurmMinutes parses a Slurm duration — "minutes", "minutes:seconds",
// "hours:minutes:seconds", "days-hours", "days-hours:minutes" or
// "days-hours:minutes:seconds" — into whole minutes. UNLIMITED, NONE and INFINITE are
// not a duration.
func slurmMinutes(s string) (int64, bool) {
	s = strings.TrimSpace(s)
	if s == "" {
		return 0, false
	}
	var days int64
	if d, rest, ok := strings.Cut(s, "-"); ok {
		n, err := strconv.ParseInt(d, 10, 64)
		if err != nil {
			return 0, false
		}
		days, s = n, rest
	}
	parts := strings.Split(s, ":")
	nums := make([]int64, len(parts))
	for i, p := range parts {
		n, err := strconv.ParseInt(p, 10, 64)
		if err != nil {
			return 0, false
		}
		nums[i] = n
	}

	var h, m int64
	switch {
	case days > 0 && len(nums) == 1: // days-hours
		h = nums[0]
	case days > 0 && len(nums) >= 2: // days-hours:minutes[:seconds]
		h, m = nums[0], nums[1]
	case len(nums) == 1: // minutes
		m = nums[0]
	case len(nums) == 2: // minutes:seconds
		m = nums[0]
	case len(nums) == 3: // hours:minutes:seconds
		h, m = nums[0], nums[1]
	default:
		return 0, false
	}
	return days*24*60 + h*60 + m, true
}

// leadingInt reads the number sinfo prints, which carries a "+" when node sizes vary.
func leadingInt(s string) (int64, bool) {
	s = strings.TrimSpace(s)
	end := 0
	for end < len(s) && s[end] >= '0' && s[end] <= '9' {
		end++
	}
	if end == 0 {
		return 0, false
	}
	n, err := strconv.ParseInt(s[:end], 10, 64)
	return n, err == nil
}

func int32Ptr(n int64) *int32 {
	v := int32(n)
	return &v
}

// DiscoverAllocations returns the Slurm accounts the config's login may charge, with
// the partitions and QOS each is limited to.
//
// It runs under a cluster config rather than a raw key, since an allocation belongs to
// a login on one cluster — which is exactly what a config is. Anyone who may submit
// under the config may ask: READ on it, as for submission. A config with an
// interactive key asks the caller to answer its prompts, as a run submitted under it
// would.
func (s *ClusterDiscoveryService) DiscoverAllocations(ctx context.Context, configID string) (*dto.AllocationDiscoveryResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	config, err := s.configs.RequireUsable(ctx, configID)
	if err != nil {
		return nil, err
	}
	if config.SlurmCluster == nil || config.SSHCredential == nil {
		return nil, httpx.Conflict("Slurm cluster config %s names no cluster or no SSH credential to log in with", configID)
	}

	ctx, cancel := context.WithTimeout(ctx, discoveryTimeout)
	defer cancel()

	cluster := config.SlurmCluster
	dial := ssh.NewSSHDialConfig(cluster.HeadnodeHost, cluster.HeadnodePort, config.LoginUser, *config.SSHCredential, principal.Name)
	target := fmt.Sprintf("ssh %s@%s:%d", config.LoginUser, cluster.HeadnodeHost, cluster.HeadnodePort)
	user := ssh.ShellQuote(config.LoginUser)

	// sacctmgr knows each association with its partition and QOS limits. Some sites
	// close it to ordinary users, so sshare — which lists the same accounts without the
	// limits — is the fallback.
	sacctmgr := slurmCommand(cluster.SlurmHome, "sacctmgr") + " -nP show associations where user=" + user + " format=Account,Partition,QOS"
	stdout, stderr, err := s.run(ctx, dial, sacctmgr, target)
	allocations := parseAssociations(stdout)
	if err != nil || len(allocations) == 0 {
		sshare := slurmCommand(cluster.SlurmHome, "sshare") + " -h -P -U -u " + user + " -o Account"
		out, shareStderr, shareErr := s.run(ctx, dial, sshare, target)
		if shareErr != nil {
			if err != nil {
				// Report the first failure: it is the one with the limits, and the
				// fallback most likely failed for the same reason.
				return nil, discoveryFailed("Allocation", err, stderr)
			}
			return nil, discoveryFailed("Allocation", shareErr, shareStderr)
		}
		allocations = parseShareAccounts(out)
	}
	if len(allocations) == 0 {
		return &dto.AllocationDiscoveryResponse{Allocations: []dto.Allocation{}}, nil
	}

	// Descriptions are what tell "tg-bio210001" from "tg-che220002"; a site that will
	// not show them costs only the descriptions.
	names := make([]string, len(allocations))
	for i, a := range allocations {
		names[i] = a.Account
	}
	describe := slurmCommand(cluster.SlurmHome, "sacctmgr") + " -nP show account where name=" +
		ssh.ShellQuote(strings.Join(names, ",")) + " format=Account,Descr"
	if out, _, err := s.run(ctx, dial, describe, target); err == nil {
		applyDescriptions(allocations, out)
	}
	return &dto.AllocationDiscoveryResponse{Allocations: allocations}, nil
}

// parseAssociations reads `sacctmgr -nP show associations format=Account,Partition,QOS`,
// one association per line. An account appears once per partition it is limited to; an
// empty partition means it may use any. Accounts keep the order sacctmgr lists them in.
func parseAssociations(out string) []dto.Allocation {
	var order []string
	byAccount := map[string]*dto.Allocation{}
	anyPartition := map[string]bool{}
	for _, line := range strings.Split(out, "\n") {
		cols := strings.Split(strings.TrimSpace(line), "|")
		if len(cols) < 3 || cols[0] == "" {
			continue
		}
		account := strings.TrimSpace(cols[0])
		a := byAccount[account]
		if a == nil {
			a = &dto.Allocation{Account: account, Partitions: []string{}, QOS: []string{}}
			byAccount[account] = a
			order = append(order, account)
		}
		if p := strings.TrimSpace(cols[1]); p == "" {
			anyPartition[account] = true
		} else {
			a.Partitions = appendUnique(a.Partitions, p)
		}
		for _, q := range strings.Split(cols[2], ",") {
			if q = strings.TrimSpace(q); q != "" {
				a.QOS = appendUnique(a.QOS, q)
			}
		}
	}
	result := make([]dto.Allocation, 0, len(order))
	for _, name := range order {
		a := byAccount[name]
		// An association with no partition opens all of them, which the per-partition
		// rows beside it do not narrow.
		if anyPartition[name] {
			a.Partitions = []string{}
		}
		result = append(result, *a)
	}
	return result
}

// parseShareAccounts reads `sshare -h -P -U -o Account`, one account per line.
func parseShareAccounts(out string) []dto.Allocation {
	var allocations []dto.Allocation
	seen := map[string]bool{}
	for _, line := range strings.Split(out, "\n") {
		account := strings.TrimSpace(strings.Split(line, "|")[0])
		if account == "" || seen[account] {
			continue
		}
		seen[account] = true
		allocations = append(allocations, dto.Allocation{Account: account, Partitions: []string{}, QOS: []string{}})
	}
	return allocations
}

// applyDescriptions folds `sacctmgr -nP show account format=Account,Descr` into
// allocations. Slurm lowercases account names, so they are matched without case.
func applyDescriptions(allocations []dto.Allocation, out string) {
	descr := map[string]string{}
	for _, line := range strings.Split(out, "\n") {
		if account, d, ok := strings.Cut(strings.TrimSpace(line), "|"); ok {
			descr[strings.ToLower(account)] = strings.TrimSpace(d)
		}
	}
	for i := range allocations {
		// A description equal to the name says nothing; sacctmgr fills one in so.
		if d := descr[strings.ToLower(allocations[i].Account)]; d != "" && !strings.EqualFold(d, allocations[i].Account) {
			allocations[i].Description = &d
		}
	}
}

func appendUnique(list []string, v string) []string {
	for _, x := range list {
		if x == v {
			return list
		}
	}
	return append(list, v)
}
