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

package toolkit

import (
	"encoding/base64"
	"errors"
	"strconv"
)

const (
	DefaultLimit = 25
	MaxLimit     = 100
)

// PageArgs are the pagination arguments every list tool takes.
type PageArgs struct {
	Cursor string `json:"cursor,omitempty" jsonschema:"opaque cursor from a previous call's next_cursor"`
	Limit  int    `json:"limit,omitempty" jsonschema:"items per page, default 25, at most 100"`
}

// PageInfo is returned by every list tool.
type PageInfo struct {
	NextCursor string `json:"next_cursor,omitempty"`
	HasMore    bool   `json:"has_more"`
	Total      int    `json:"total"`
}

// Paginate returns one page of items.
func Paginate[T any](items []T, args PageArgs) ([]T, PageInfo, error) {
	limit := args.Limit
	if limit <= 0 {
		limit = DefaultLimit
	}
	limit = min(limit, MaxLimit)
	offset := 0
	if args.Cursor != "" {
		raw, err := base64.RawURLEncoding.DecodeString(args.Cursor)
		if err == nil {
			offset, err = strconv.Atoi(string(raw))
		}
		if err != nil || offset < 0 {
			return nil, PageInfo{}, errors.New("invalid cursor: pass next_cursor exactly as returned")
		}
	}
	if offset > len(items) {
		offset = len(items)
	}
	end := min(offset+limit, len(items))
	info := PageInfo{Total: len(items), HasMore: end < len(items)}
	if info.HasMore {
		info.NextCursor = base64.RawURLEncoding.EncodeToString([]byte(strconv.Itoa(end)))
	}
	page := items[offset:end]
	if page == nil {
		page = []T{}
	}
	return page, info, nil
}
