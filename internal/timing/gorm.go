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

package timing

import (
	"time"

	"gorm.io/gorm"
)

const (
	gormPlugin   = "airavata:timing"
	gormStartKey = "airavata:timing:start"
)

// RegisterGorm makes every statement gdb runs with a recorder on its context count as
// one database round trip on that recorder.
//
// Each callback brackets only the core step of its chain (gorm:query, gorm:create, …),
// so a query's preloads, which gorm runs as statements of their own, are counted
// separately rather than folded into the parent's time. The measured time includes
// waiting for a pooled connection and dialing a new one, which is part of what a
// request pays for the database.
func RegisterGorm(gdb *gorm.DB) error {
	return gdb.Use(gormTiming{})
}

type gormTiming struct{}

func (gormTiming) Name() string { return gormPlugin }

func (gormTiming) Initialize(gdb *gorm.DB) error {
	cb := gdb.Callback()
	register := []func() error{
		func() error { return cb.Query().Before("gorm:query").Register(gormPlugin+":query_before", before) },
		func() error {
			return cb.Query().After("gorm:query").Before("gorm:preload").Register(gormPlugin+":query_after", after)
		},
		func() error { return cb.Create().Before("gorm:create").Register(gormPlugin+":create_before", before) },
		func() error { return cb.Create().After("gorm:create").Register(gormPlugin+":create_after", after) },
		func() error { return cb.Update().Before("gorm:update").Register(gormPlugin+":update_before", before) },
		func() error { return cb.Update().After("gorm:update").Register(gormPlugin+":update_after", after) },
		func() error { return cb.Delete().Before("gorm:delete").Register(gormPlugin+":delete_before", before) },
		func() error { return cb.Delete().After("gorm:delete").Register(gormPlugin+":delete_after", after) },
		func() error { return cb.Row().Before("gorm:row").Register(gormPlugin+":row_before", before) },
		func() error { return cb.Row().After("gorm:row").Register(gormPlugin+":row_after", after) },
		func() error { return cb.Raw().Before("gorm:raw").Register(gormPlugin+":raw_before", before) },
		func() error { return cb.Raw().After("gorm:raw").Register(gormPlugin+":raw_after", after) },
	}
	for _, r := range register {
		if err := r(); err != nil {
			return err
		}
	}
	return nil
}

func before(tx *gorm.DB) {
	if FromContext(tx.Statement.Context) == nil {
		return
	}
	tx.InstanceSet(gormStartKey, time.Now())
}

func after(tx *gorm.DB) {
	rec := FromContext(tx.Statement.Context)
	if rec == nil {
		return
	}
	v, ok := tx.InstanceGet(gormStartKey)
	if !ok {
		return
	}
	if began, ok := v.(time.Time); ok {
		rec.AddDB(time.Since(began))
	}
}
