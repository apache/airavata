package pkg

import (
	protos "airavata-agent/protos"
	"google.golang.org/grpc"
)

type Stream = grpc.BidiStreamingClient[protos.AgentMessage, protos.ServerMessage]
