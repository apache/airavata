
1. install go by following :
https://tecadmin.net/install-go-on-ubuntu/

2. make sure to remember the `gopath` directory you set

3. install thrift only from the repo by 'cloning' and 'following the README' in the follwing repo:
"https://github.com/apache/thrift"

4. Make sure that your `gopath` has all the dependency go-stubs:
   lets say that the gopath is  $HOME/Projects/Proj1/ :

4. create a directory named `src` under `$HOME/Projects/Proj1/`

5. copy  all the go stubs  in `thrift/lib/go/thrift` inside `src` folder of go-path'
6. go to `airavata/thrift-interface-descriptions/`
7. type `thrift -r --gen go airavata-apis/airavata_api.thrift` to genrate new folder go-gen
8. copy  all the go stubs genreted in the `go-gen` located at folder inside `src` folder of go-path

9. go to  `airavata/thrift-interface-descriptions/`:

10. run `*.go`

11. You will get the list of projects
