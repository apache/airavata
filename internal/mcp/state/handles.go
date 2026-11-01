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

package state

import (
	"crypto/rand"
	"encoding/base64"
	"errors"
	"fmt"
	"sync"
	"time"
)

// TTL is how long a proposal handle stays usable.
const TTL = 24 * time.Hour

var (
	// ErrUnknown covers unknown, expired, foreign and wrong-kind handles alike.
	ErrUnknown = errors.New("unknown or expired proposal handle: plan again to get a new one")
	// ErrUsed means the handle was already consumed by a confirmed call.
	ErrUsed = errors.New("this proposal was already carried out: plan again to do it a second time")
	// ErrBusy means another confirmed call is using the handle right now.
	ErrBusy = errors.New("this proposal is being carried out by another call")
)

type status int

const (
	open status = iota
	busy
	done
)

type entry struct {
	principal string
	kind      string
	payload   any
	expires   time.Time
	status    status
	progress  map[string]string
}

// Store keeps single-use proposal handles in memory, bound to the user that made them.
type Store struct {
	mu      sync.Mutex
	now     func() time.Time
	entries map[string]*entry
}

func NewStore() *Store {
	return &Store{now: time.Now, entries: map[string]*entry{}}
}

// Put records a proposal and returns its handle.
func (s *Store) Put(principal, kind string, payload any) (string, error) {
	b := make([]byte, 24)
	if _, err := rand.Read(b); err != nil {
		return "", err
	}
	h := kind + "_" + base64.RawURLEncoding.EncodeToString(b)
	s.mu.Lock()
	defer s.mu.Unlock()
	s.gc()
	s.entries[h] = &entry{principal: principal, kind: kind, payload: payload, expires: s.now().Add(TTL), progress: map[string]string{}}
	return h, nil
}

// Get returns a proposal that is still open, without consuming it.
func (s *Store) Get(principal, kind, handle string) (any, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	e, err := s.lookup(principal, kind, handle)
	if err != nil {
		return nil, err
	}
	switch e.status {
	case done:
		return nil, ErrUsed
	case busy:
		return nil, ErrBusy
	}
	return e.payload, nil
}

// Claim is a consumed-but-unfinished proposal. Exactly one of Finish or Release must be called.
type Claim struct {
	store  *Store
	handle string
	// Payload is the proposal, Progress what an earlier failed attempt already created.
	Payload  any
	Progress map[string]string
}

// Claim takes the handle for a confirmed call before anything is changed, so a replay fails.
func (s *Store) Claim(principal, kind, handle string) (*Claim, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	e, err := s.lookup(principal, kind, handle)
	if err != nil {
		return nil, err
	}
	switch e.status {
	case done:
		return nil, ErrUsed
	case busy:
		return nil, ErrBusy
	}
	e.status = busy
	progress := make(map[string]string, len(e.progress))
	for k, v := range e.progress {
		progress[k] = v
	}
	return &Claim{store: s, handle: handle, Payload: e.payload, Progress: progress}, nil
}

// Record keeps a created object's id so a retry after a failure resumes rather than duplicates.
func (c *Claim) Record(key, value string) {
	c.Progress[key] = value
	c.store.mu.Lock()
	defer c.store.mu.Unlock()
	if e, ok := c.store.entries[c.handle]; ok {
		e.progress[key] = value
	}
}

// Finish marks the proposal as carried out.
func (c *Claim) Finish() {
	c.store.mu.Lock()
	defer c.store.mu.Unlock()
	if e, ok := c.store.entries[c.handle]; ok {
		e.status = done
	}
}

// Release reopens the proposal after a failed attempt, keeping recorded progress.
func (c *Claim) Release() {
	c.store.mu.Lock()
	defer c.store.mu.Unlock()
	if e, ok := c.store.entries[c.handle]; ok && e.status == busy {
		e.status = open
	}
}

func (s *Store) lookup(principal, kind, handle string) (*entry, error) {
	e, ok := s.entries[handle]
	if !ok || e.principal != principal || e.kind != kind || !s.now().Before(e.expires) {
		return nil, fmt.Errorf("%w (expected a %s handle)", ErrUnknown, kind)
	}
	return e, nil
}

func (s *Store) gc() {
	now := s.now()
	for h, e := range s.entries {
		if !now.Before(e.expires) {
			delete(s.entries, h)
		}
	}
}
