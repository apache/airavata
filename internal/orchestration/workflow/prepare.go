// Package workflow holds the durable orchestration: what runs, in what order, and what
// happens when a step fails.
package workflow

import (
	"context"
	workflow "github.com/cschleiden/go-workflows/workflow"

	model "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/internal/app"
	"github.com/apache/airavata/internal/orchestration/activities"
	"log/slog"
)

type Workflows struct {
	svscs *app.Services
	acts  *activities.Activities
}

// NewWorkflows returns the workflow set scheduling acts.
func NewWorkflows(svcs *app.Services, acts *activities.Activities) *Workflows {
	return &Workflows{svscs: svcs, acts: acts}
}

func (w *Workflows) HandleProcessExecution(ctx workflow.Context, processID string) error {
	ctxInt := context.Background()
	dsts, err := w.svscs.DataStagingTask.ListForProcess(ctxInt, processID)
	if err != nil {
		slog.Error("Failed to list data staging tasks", "processId", processID, "error", err)
		return err
	}

	jsts, err := w.svscs.JobSubmissionTask.ListForProcess(ctxInt, processID)
	if err != nil {
		slog.Error("Failed to list job submission tasks", "processId", processID, "error", err)
		return err
	}

	jmts, err := w.svscs.JobMonitoringTask.ListForProcess(ctxInt, processID)
	if err != nil {
		slog.Error("Failed to list job monitoring tasks", "processId", processID, "error", err)
		return err
	}

	currentOrder := 0
	isPending := true

	for isPending {
		isPending = false
		for _, dst := range dsts {
			if dst.TaskOrder != nil && *dst.TaskOrder == currentOrder {
				workflow.ExecuteActivity[int](
					ctx, workflow.ActivityOptions{RetryOptions: retryOptions(*dst.OnFailure, dst.RetryCount)}, w.acts.CopyData, processID, dst.ID)
			} else if dst.TaskOrder != nil && *dst.TaskOrder > currentOrder {
				isPending = true
			}
		}

		for _, jst := range jsts {
			if jst.TaskOrder != nil && *jst.TaskOrder == currentOrder {
				workflow.ExecuteActivity[int](
					ctx, workflow.ActivityOptions{RetryOptions: retryOptions(*jst.OnFailure, jst.RetryCount)}, w.acts.SubmitBatchJob, processID, jst.ID)
			} else if jst.TaskOrder != nil && *jst.TaskOrder > currentOrder {
				isPending = true
			}
		}

		for _, jmt := range jmts {
			if jmt.TaskOrder != nil && *jmt.TaskOrder == currentOrder {
				workflow.ExecuteActivity[int](
					ctx, workflow.ActivityOptions{RetryOptions: retryOptions(*jmt.OnFailure, jmt.RetryCount)}, w.acts.MonitorBatchJob, processID, jmt.ID)
			} else if jmt.TaskOrder != nil && *jmt.TaskOrder > currentOrder {
				isPending = true
			}
		}

		currentOrder += 1

		if !isPending {
			slog.Info("All tasks completed for process", "processId", processID)
		}
	}

	return nil
}

func retryOptions(onFailure model.OnFailureAction, retryCount *int) workflow.RetryOptions {
	opts := workflow.RetryOptions{MaxAttempts: 1}
	if onFailure != model.OnFailureActionRetry {
		return opts
	}
	// retryCount counts retries, so the first run is one attempt on top of it.
	opts.MaxAttempts = 1 + *retryCount
	opts.BackoffCoefficient = workflow.DefaultRetryOptions.BackoffCoefficient
	return opts
}
