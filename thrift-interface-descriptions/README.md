1. https://github.com/satyamsah/airavata/wiki (Links to an external site.)Links to an external site.  link for wiki :

 I am working on developing a new feature inside airavata that is is to develop a new  go client. It will give new capability inside airavata to server new users who want to interact with airavata using an application written in go .

 

 

2. pull request for airavata core repo -  (https://github.com/apache/airavata (Links to an external site.)Links to an external site. ):

pull request number(119): https://github.com/apache/airavata/pull/119 (Links to an external site.)Links to an external site.


3. Jira story:

https://issues.apache.org/jira/browse/AIRAVATA-2522 (Links to an external site.)Links to an external site.



### GO client to get all the project under airavata

##### the main task is to replicate the task of Marcus: https://github.com/machristie/airavata-python3-client  

1) clone this repostory
2) install `go` by following the link : https://tecadmin.net/install-go-on-ubuntu/
   
   2) 1) for example, lets say that the gopath is `$HOME/Projects/Proj1/`
   2) 2) make sure to note down the `gopath` directory you set

3)  Thrift repo(external and separate). Keep this part external and separate from airavata directory. Install thrift by  'cloning' and 'following the README' in the following repo: 
  `"https://github.com/apache/thrift"`

4) This is gopath directory: create a directory named src under `$HOME/Projects/Proj1/` (which is gopath)

5) cd `src` and `mkdir -p git.apache.org/thrift.git/lib/go/thrift` to create subfolders 'git.apache.org/thrift.git/lib/go/thrift'

6) copy all the go stubs located inside `thrift/lib/go/thrift` to `src` subfolder of `git.apache.org/thrift.git/lib/go/thrift`

7) Airavata-repo: go to `airavata/thrift-interface-descriptions/`

8) type `thrift -r --gen go airavata-apis/airavata_api.thrift` to generate new folder `go-gen`

9) copy all the go stubs generated in the `go-gen` folder to `src` folder of go-path

10) go to `airavata/thrift-interface-descriptions/`:

11) type `chmod 777 build.sh`

12) type `./build.sh <your-testdrive-dev-username> <your-testdrive-dev-password>`. If you don't have account , contact me. I will share my credentials.

13) You will get the list of projects in json format. Copy the json output

14) Use any of the json formatter to see the result.
