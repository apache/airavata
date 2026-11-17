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

package auth

import (
	"testing"
	"time"
)

func cacheAt(now *time.Time, ttl time.Duration, max int) *ClaimsCache {
	c := NewClaimsCache(ttl, max)
	c.now = func() time.Time { return *now }
	return c
}

func TestClaimsCacheExpiresAfterTTL(t *testing.T) {
	now := time.Unix(1_000_000, 0)
	c := cacheAt(&now, time.Minute, 10)
	c.Put("tok", map[string]any{"username": "alice"})

	now = now.Add(59 * time.Second)
	if got, ok := c.Get("tok"); !ok || got["username"] != "alice" {
		t.Fatalf("Get before TTL = %v, %v; want alice, true", got, ok)
	}
	now = now.Add(time.Second)
	if _, ok := c.Get("tok"); ok {
		t.Fatal("Get at TTL hit; want miss")
	}
}

// An entry must never outlive the token it describes.
func TestClaimsCacheHonoursTokenExpiry(t *testing.T) {
	now := time.Unix(1_000_000, 0)
	c := cacheAt(&now, time.Hour, 10)
	c.Put("tok", map[string]any{"exp": float64(now.Unix() + 10)})

	now = now.Add(9 * time.Second)
	if _, ok := c.Get("tok"); !ok {
		t.Fatal("Get before exp missed; want hit")
	}
	now = now.Add(time.Second)
	if _, ok := c.Get("tok"); ok {
		t.Fatal("Get at exp hit; want miss")
	}

	c.Put("expired", map[string]any{"exp": float64(now.Unix() - 1)})
	if _, ok := c.Get("expired"); ok {
		t.Fatal("an already-expired token was cached")
	}
}

func TestClaimsCacheBounded(t *testing.T) {
	now := time.Unix(1_000_000, 0)
	c := cacheAt(&now, time.Minute, 2)
	c.Put("a", map[string]any{})
	c.Put("b", map[string]any{})
	c.Put("c", map[string]any{})
	if _, ok := c.Get("c"); ok {
		t.Fatal("cache grew past its bound")
	}

	// Once the old entries expire they make room.
	now = now.Add(time.Minute)
	c.Put("c", map[string]any{})
	if _, ok := c.Get("c"); !ok {
		t.Fatal("expired entries were not swept to make room")
	}
}

// A caller adding to the returned claims must not change what the next caller sees.
func TestClaimsCacheReturnsCopies(t *testing.T) {
	now := time.Unix(1_000_000, 0)
	c := cacheAt(&now, time.Minute, 10)
	original := map[string]any{"username": "alice"}
	c.Put("tok", original)
	original["username"] = "mallory"

	got, _ := c.Get("tok")
	got["username"] = "eve"
	again, _ := c.Get("tok")
	if again["username"] != "alice" {
		t.Fatalf("cached username = %v; want alice", again["username"])
	}
}

func TestNilClaimsCache(t *testing.T) {
	var c *ClaimsCache
	c.Put("tok", map[string]any{})
	if _, ok := c.Get("tok"); ok {
		t.Fatal("nil cache hit")
	}
}
