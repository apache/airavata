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
	"crypto/sha256"
	"sync"
	"time"
)

// Defaults for the cache NewCILogonIntrospector builds.
const (
	DefaultClaimsCacheTTL        = 60 * time.Second
	DefaultClaimsCacheMaxEntries = 10000
)

// ClaimsCache remembers what CILogon said about a token, so a client making many
// requests with one token pays for the introspection and userinfo round trips once per
// TTL rather than on every request.
//
// It holds only the identity provider's claims. Roles, the user record's status and
// permissions are still read from the database on every request, so suspending a user
// or changing their roles takes effect on their next request as before. What the cache
// does delay is a revocation made at CILogon: a revoked token keeps working until its
// entry expires, at most TTL later.
//
// Entries are keyed by a SHA-256 of the token, so the tokens themselves are not kept in
// memory. Only successful introspections are stored; a rejected token is checked again
// every time. A nil *ClaimsCache is valid and caches nothing.
type ClaimsCache struct {
	ttl        time.Duration
	maxEntries int
	now        func() time.Time

	mu      sync.Mutex
	entries map[[sha256.Size]byte]claimsEntry
}

type claimsEntry struct {
	claims  map[string]any
	expires time.Time
}

// NewClaimsCache returns a cache keeping each entry for at most ttl, and never past the
// token's own exp claim, holding no more than maxEntries tokens at once.
func NewClaimsCache(ttl time.Duration, maxEntries int) *ClaimsCache {
	return &ClaimsCache{
		ttl:        ttl,
		maxEntries: maxEntries,
		now:        time.Now,
		entries:    make(map[[sha256.Size]byte]claimsEntry),
	}
}

// Get returns a copy of the claims cached for token, if there is a live entry.
func (c *ClaimsCache) Get(token string) (map[string]any, bool) {
	if c == nil {
		return nil, false
	}
	key := sha256.Sum256([]byte(token))
	c.mu.Lock()
	defer c.mu.Unlock()
	entry, ok := c.entries[key]
	if !ok {
		return nil, false
	}
	if !c.now().Before(entry.expires) {
		delete(c.entries, key)
		return nil, false
	}
	return copyClaims(entry.claims), true
}

// Put caches claims for token. An entry expires after the TTL or at the token's exp
// claim, whichever is sooner; a token already past its exp is not cached. When the cache
// is full even after dropping expired entries, the claims are simply not cached: the
// next request introspects again, which is slower but never wrong.
func (c *ClaimsCache) Put(token string, claims map[string]any) {
	if c == nil || c.ttl <= 0 {
		return
	}
	now := c.now()
	expires := now.Add(c.ttl)
	if exp, ok := claims["exp"].(float64); ok {
		if tokenExpiry := time.Unix(int64(exp), 0); tokenExpiry.Before(expires) {
			expires = tokenExpiry
		}
	}
	if !now.Before(expires) {
		return
	}

	key := sha256.Sum256([]byte(token))
	c.mu.Lock()
	defer c.mu.Unlock()
	if _, exists := c.entries[key]; !exists && len(c.entries) >= c.maxEntries {
		for k, e := range c.entries {
			if !now.Before(e.expires) {
				delete(c.entries, k)
			}
		}
		if len(c.entries) >= c.maxEntries {
			return
		}
	}
	c.entries[key] = claimsEntry{claims: copyClaims(claims), expires: expires}
}

// copyClaims is shallow: the top-level map is what callers might add to, and claim
// values are never modified in place.
func copyClaims(claims map[string]any) map[string]any {
	out := make(map[string]any, len(claims))
	for k, v := range claims {
		out[k] = v
	}
	return out
}
