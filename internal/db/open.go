package db

import (
	"fmt"
	"time"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/schema"
)

// Config describes the PostgreSQL connection, mirroring the spring.datasource.* and
// spring.datasource.hikari.* properties of the Java service.
type Config struct {
	DSN string

	MaxOpenConns    int
	MaxIdleConns    int
	ConnMaxLifetime time.Duration
}

// DefaultConfig returns the pool settings the Java service used for its Hikari pool:
// a maximum of 20 connections with 2 kept idle.
func DefaultConfig(dsn string) Config {
	return Config{
		DSN:             dsn,
		MaxOpenConns:    20,
		MaxIdleConns:    2,
		ConnMaxLifetime: 30 * time.Minute,
	}
}

// NamingStrategy is the identifier naming every connection uses.
//
// The only departure from GORM's default is the length cap. GORM truncates a generated
// index or constraint name to 64 characters and appends a hash; PostgreSQL truncates
// identifiers at 63 bytes. Left at the default, the longest names this schema produces
// — the sharing tables' foreign keys — would be silently shortened by the server, and
// two names that differ only in their final character would collide. Capping at 63
// keeps the name GORM generates and the name the server stores identical.
var NamingStrategy = schema.NamingStrategy{IdentifierMaxLength: 63}

// Open connects to PostgreSQL and configures the underlying connection pool.
func Open(cfg Config, opts ...gorm.Option) (*gorm.DB, error) {
	opts = append([]gorm.Option{&gorm.Config{NamingStrategy: NamingStrategy}}, opts...)

	gdb, err := gorm.Open(postgres.Open(cfg.DSN), opts...)
	if err != nil {
		return nil, fmt.Errorf("connect to database: %w", err)
	}

	sqlDB, err := gdb.DB()
	if err != nil {
		return nil, fmt.Errorf("access underlying sql.DB: %w", err)
	}
	sqlDB.SetMaxOpenConns(cfg.MaxOpenConns)
	sqlDB.SetMaxIdleConns(cfg.MaxIdleConns)
	sqlDB.SetConnMaxLifetime(cfg.ConnMaxLifetime)

	return gdb, nil
}
