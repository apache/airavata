module client

go 1.22.5

replace grpcfs => ../grpcfs

require (
	github.com/jacobsa/fuse v0.0.0-20240626143436-8a36813dc074
	grpcfs v0.0.0-00010101000000-000000000000
)

require (
	golang.org/x/net v0.25.0 // indirect
	golang.org/x/sys v0.22.0 // indirect
	golang.org/x/text v0.15.0 // indirect
	google.golang.org/genproto/googleapis/rpc v0.0.0-20240528184218-531527333157 // indirect
	google.golang.org/grpc v1.65.0 // indirect
	google.golang.org/protobuf v1.34.2 // indirect
)
