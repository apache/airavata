package common

import (
	"errors"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/httpx"
)

func NotFoundAs(err error, format string, args ...any) error {
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return httpx.NotFound(format, args...)
	}
	return err
}
