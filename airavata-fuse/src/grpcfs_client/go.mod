module client

go 1.23.0

replace grpcfs => ../grpcfs

require (
	github.com/jacobsa/fuse v0.0.0-20250702080931-3e9d24d5e3ff
	grpcfs v0.0.0-00010101000000-000000000000
)

require (
	golang.org/x/net v0.41.0 // indirect
	golang.org/x/sys v0.33.0 // indirect
	golang.org/x/text v0.26.0 // indirect
	google.golang.org/genproto/googleapis/rpc v0.0.0-20250603155806-513f23925822 // indirect
	google.golang.org/grpc v1.73.0 // indirect
	google.golang.org/protobuf v1.36.6 // indirect
)
