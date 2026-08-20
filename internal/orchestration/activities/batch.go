package activities

import (
	"context"
	model "github.com/apache/airavata/api/process/model"
	"log/slog"
)

type SubmitBatchJobParameters struct {
	ProcessID      string
	ExecutablePath string
	Arguments      []string
	Environment    map[string]string
}

func SubmitBatchJob(ctx context.Context, batchJobProcess model.BatchJobProcess) (int, error) {
	slog.Info("Submitting batch job ...")
	return 0, nil
}

func CancelBatchJob(ctx context.Context, batchJobProcess model.BatchJobProcess) (int, error) {
	slog.Info("Cancelling batch job ...")
	return 0, nil
}

func MonitorBatchJob(ctx context.Context, batchJobProcess model.BatchJobProcess) (int, error) {
	slog.Info("Monitoring batch job ...")
	return 0, nil
}
