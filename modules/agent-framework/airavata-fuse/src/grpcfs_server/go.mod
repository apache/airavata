module server

go 1.23.0

toolchain go1.24.4

replace grpcfs => ../grpcfs

require (
	golang.org/x/sys v0.33.0
	google.golang.org/grpc v1.73.0
	google.golang.org/protobuf v1.36.6
	grpcfs v0.0.0-00010101000000-000000000000
)

require (
	golang.org/x/net v0.41.0 // indirect
	golang.org/x/text v0.26.0 // indirect
	google.golang.org/genproto/googleapis/rpc v0.0.0-20250603155806-513f23925822 // indirect
)
