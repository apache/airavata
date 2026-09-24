package orchestration

import (
	"errors"
	"fmt"
	"regexp"
	"strings"

	model "github.com/apache/airavata/api/process/model"
)

type BatchJobNotification struct {
	JobID   string
	JobName string
	Status  model.BatchJobStatusType
}

var ErrNotBatchJobNotification = errors.New("not a slurm job notification")

// batchJobSubject matches the subject SLURM builds for a job notification:
//
//	Slurm Job_id=127 Name=456dec76-12cc-4d90-a622-977a6715a89b Began, Queued time 00:00:00
//	Slurm Job_id=127 Name=456dec76-12cc-4d90-a622-977a6715a89b Failed, Run time 00:00:01, FAILED, ExitCode 2
var batchJobSubject = regexp.MustCompile(`(?i)^slurm\s+job_id=(\S+)\s+name=(\S+)\s+(.+)$`)

var slurmJobEvents = map[string]model.BatchJobStatusType{
	"began":                     model.BatchJobStatusBegin,
	"ended":                     model.BatchJobStatusEnded,
	"failed":                    model.BatchJobStatusFailed,
	"requeued":                  model.BatchJobStatusRequeued,
	"staged out":                model.BatchJobStatusStageOut,
	"invalid dependency":        model.BatchJobStatusInvalidDepend,
	"reached time limit":        model.BatchJobStatusTimeLimit,
	"reached 90% of time limit": model.BatchJobStatusTimeLimit90,
	"reached 80% of time limit": model.BatchJobStatusTimeLimit80,
	"reached 50% of time limit": model.BatchJobStatusTimeLimit50,
}

// parseBatchJobSubject reads the job, and what happened to it, out of a notification's
// subject.
func parseBatchJobSubject(subject string) (BatchJobNotification, error) {
	match := batchJobSubject.FindStringSubmatch(strings.TrimSpace(subject))
	if match == nil {
		return BatchJobNotification{}, fmt.Errorf("%w: %q", ErrNotBatchJobNotification, subject)
	}

	event := match[3]
	if comma := strings.Index(event, ","); comma >= 0 {
		event = event[:comma]
	}
	event = strings.TrimSpace(event)

	status, ok := slurmJobEvents[strings.ToLower(event)]
	if !ok {
		return BatchJobNotification{}, fmt.Errorf("unrecognised job event %q in subject %q", event, subject)
	}

	return BatchJobNotification{
		JobID:   match[1],
		JobName: match[2],
		Status:  status,
	}, nil
}
