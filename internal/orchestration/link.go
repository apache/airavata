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

package orchestration

import (
	"context"
	"encoding/binary"
	"errors"
	"fmt"
	"io"
	"log/slog"
	"net"
	"strconv"
	"time"

	"github.com/coder/websocket"
	"github.com/hashicorp/yamux"

	internalptr "github.com/apache/airavata/internal/ptr"

	model "github.com/apache/airavata/api/process/model"
)

func (a *ExecutionEngine) AcceptLink(ctx context.Context, process *model.Process, control *websocket.Conn) {
	config := yamux.DefaultConfig()
	config.LogOutput = io.Discard
	mux, _ := yamux.Client(websocket.NetConn(context.Background(), control, websocket.MessageBinary), config)
	link := &Link{Session: mux, ws: control, Token: process.BatchProcess.Token}
	if previous, ok := a.links.Swap(process.ID, link); ok {
		_ = previous.(*Link).Close()
	}
	// Clients time walltime from BEGIN; job mail may never send it, and a link proves the job began.
	if process.State() == model.ProcessStatusTypeSubmitted {
		if err := a.batchStatus.Create(ctx, &model.BatchJobStatus{BatchProcessID: process.BatchProcess.ID, Status: model.BatchJobStatusBegin}); err != nil {
			slog.Error("Failed to record batch job begin", "processId", process.ID, "error", err)
		}
	}
	if process.State() != model.ProcessStatusTypeReady {
		a.recordStatus(ctx, process.ID, model.ProcessStatusTypeReady, "Linkspan has linked the job to the server")
	}
	<-link.CloseChan()
	a.links.CompareAndDelete(process.ID, link)
}

func (a *ExecutionEngine) Token(processID string) (string, bool) {
	if linked, ok := a.links.Load(processID); ok {
		return linked.(*Link).Token, true
	}
	return "", false
}

func (a *ExecutionEngine) DialLink(ctx context.Context, _, address string) (net.Conn, error) {
	processID, rawPort, _ := net.SplitHostPort(address)
	port, _ := strconv.ParseUint(rawPort, 10, 16)
	if linked, ok := a.links.Load(processID); ok {
		return linked.(*Link).Dial(ctx, uint16(port))
	}
	return nil, errors.New("the job has no link to the server")
}

func (a *ExecutionEngine) recordStatus(ctx context.Context, processID string, status model.ProcessStatusType, log string) {
	if err := a.processStatus.Create(ctx, &model.ProcessStatus{ProcessID: &processID, Status: &status, Log: &log, Timestamp: internalptr.To(time.Now().UnixMilli())}); err != nil {
		slog.Error("Failed to record process status", "processId", processID, "status", status, "error", err)
	}
}

const (
	release     = "0.23.0"
	linuxX86Sum = "2e53a32f5909e90795f0faf3e5f92433199423f8e850e83baf28a2a519bb2aba"
	linuxARMSum = "2a80173577b73174a5599b04c8508edea33ad17c33f766a9980aa9f9949cdbf3"
)

const BinaryPath = `"$HOME/.cybershuttle/bin/linkspan"`

const ProvisionScript = `linkspan=` + BinaryPath + `
installed=$("$linkspan" --version 2>/dev/null | head -1 | tr -d 'v \r')
if [ "$(printf '%s\n%s\n' "` + release + `" "$installed" | sort -V | head -1)" != "` + release + `" ]; then
  arch=$(uname -m | sed s/aarch64/arm64/)
  case "$arch" in x86_64) sum=` + linuxX86Sum + ` ;; arm64) sum=` + linuxARMSum + ` ;; *) sum= ;; esac
  check=sha256sum; command -v sha256sum >/dev/null || check="shasum -a 256"
  staged="$linkspan.$$"
  mkdir -p "$(dirname "$linkspan")" &&
    curl -fsSL "https://github.com/cyber-shuttle/linkspan/releases/download/v` + release + `/linkspan_Linux_$arch.tar.gz" -o "$staged.tar.gz" &&
    echo "$sum  $staged.tar.gz" | $check -c - >/dev/null &&
    tar -xzOf "$staged.tar.gz" linkspan > "$staged" &&
    [ -s "$staged" ] && chmod 700 "$staged" && mv -f "$staged" "$linkspan" ||
    { rm -f "$staged" "$staged.tar.gz"; echo "could not install a verified Linkspan ` + release + ` from GitHub" >&2; exit 1; }
  rm -f "$staged.tar.gz"
fi
`

type Link struct {
	*yamux.Session
	ws    *websocket.Conn
	Token string
}

// Close drops the socket rather than the session, whose own close waits on the peer.
func (l *Link) Close() error { return l.ws.CloseNow() }

func (l *Link) Dial(ctx context.Context, port uint16) (net.Conn, error) {
	stream, err := l.OpenStream()
	if err != nil {
		return nil, err
	}
	frame := binary.BigEndian.AppendUint16(nil, port)
	_ = stream.SetDeadline(time.Now().Add(30 * time.Second))
	if _, err = stream.Write(frame); err == nil {
		_, err = io.ReadFull(stream, frame[:1])
	}
	if err == nil && frame[0] != 1 {
		err = fmt.Errorf("the job's Linkspan has no server on port %d", port)
	}
	if err != nil {
		_ = stream.Close()
		return nil, err
	}
	_ = stream.SetDeadline(time.Time{})
	return stream, nil
}
