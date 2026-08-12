package db

import (
	"fmt"
	"time"

	"gorm.io/driver/mysql"
	"gorm.io/gorm"
)

// Config describes the MariaDB connection, mirroring the spring.datasource.* and
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

// Open connects to MariaDB and configures the underlying connection pool.
func Open(cfg Config, opts ...gorm.Option) (*gorm.DB, error) {
	gdb, err := gorm.Open(mysql.Open(cfg.DSN), opts...)
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
