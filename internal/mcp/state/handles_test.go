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
	"errors"
	"testing"
	"time"
)

func TestHandleIsBoundToUserAndKind(t *testing.T) {
	s := NewStore()
	h, err := s.Put("cilogon:alice", "run", "plan")
	if err != nil {
		t.Fatal(err)
	}
	if _, err := s.Get("cilogon:bob", "run", h); !errors.Is(err, ErrUnknown) {
		t.Errorf("another user read the handle: %v", err)
	}
	if _, err := s.Get("cilogon:alice", "app", h); !errors.Is(err, ErrUnknown) {
		t.Errorf("the handle was accepted as another kind: %v", err)
	}
	if v, err := s.Get("cilogon:alice", "run", h); err != nil || v != "plan" {
		t.Errorf("Get = %v, %v", v, err)
	}
}

func TestClaimedHandleCannotBeReplayed(t *testing.T) {
	s := NewStore()
	h, _ := s.Put("cilogon:alice", "run", "plan")
	c, err := s.Claim("cilogon:alice", "run", h)
	if err != nil {
		t.Fatal(err)
	}
	if _, err := s.Claim("cilogon:alice", "run", h); !errors.Is(err, ErrBusy) {
		t.Errorf("a concurrent claim succeeded: %v", err)
	}
	c.Finish()
	if _, err := s.Claim("cilogon:alice", "run", h); !errors.Is(err, ErrUsed) {
		t.Errorf("a finished handle was claimed again: %v", err)
	}
}

func TestReleasedClaimKeepsProgress(t *testing.T) {
	s := NewStore()
	h, _ := s.Put("cilogon:alice", "run", "plan")
	c, _ := s.Claim("cilogon:alice", "run", h)
	c.Record("process_id", "p1")
	c.Release()
	c2, err := s.Claim("cilogon:alice", "run", h)
	if err != nil {
		t.Fatal(err)
	}
	if c2.Progress["process_id"] != "p1" {
		t.Errorf("progress after release = %v", c2.Progress)
	}
}

func TestHandlesExpire(t *testing.T) {
	s := NewStore()
	h, _ := s.Put("cilogon:alice", "run", "plan")
	s.now = func() time.Time { return time.Now().Add(TTL + time.Second) }
	if _, err := s.Get("cilogon:alice", "run", h); !errors.Is(err, ErrUnknown) {
		t.Errorf("an expired handle was usable: %v", err)
	}
}
