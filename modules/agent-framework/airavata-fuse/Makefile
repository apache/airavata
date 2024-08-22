all: init client server

init:
	rm -rf ./bin && mkdir ./bin

protoc: protoc_init protoc_run

protoc_init:
	go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest; \
	go install google.golang.org/protobuf/cmd/protoc-gen-go@latest

protoc_run:
	cd src && protoc --go_out=. --go-grpc_out=. proto/*.proto

client:
	cd src/grpcfs_client && go mod tidy && go build -o ../../bin .

server:
	cd src/grpcfs_server && go mod tidy && go build -o ../../bin .

run_server:
	bin/server

run_client:
	bin/client -mount $$PWD/tmp -serve $$PWD/data